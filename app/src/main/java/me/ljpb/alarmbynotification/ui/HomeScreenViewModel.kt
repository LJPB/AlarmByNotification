package me.ljpb.alarmbynotification.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.ljpb.alarmbynotification.Utility
import me.ljpb.alarmbynotification.Utility.getMilliSecondsOfNextTime
import me.ljpb.alarmbynotification.data.AlarmInfo
import me.ljpb.alarmbynotification.data.AlarmRepository
import me.ljpb.alarmbynotification.data.NotificationInfoInterface
import me.ljpb.alarmbynotification.data.NotificationRepositoryInterface
import me.ljpb.alarmbynotification.data.UserPreferencesRepository
import me.ljpb.alarmbynotification.data.room.AlarmInfoEntity
import me.ljpb.alarmbynotification.data.room.AlarmInfoInterface
import me.ljpb.alarmbynotification.data.room.NotificationEntity
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

val INITIAL_ALARM = AlarmInfo(-1, -1, -1, "", "")
val INITIAL_NOTIFY = NotificationEntity(-1, -1, -1, "", "")
val INITIAL_ALARM_LIST = listOf(INITIAL_ALARM)
val INITIAL_NOTIFY_LIST = listOf(INITIAL_NOTIFY)
val INITIAL_ID: Long? = null

class HomeScreenViewModel(
    private val alarmRepository: AlarmRepository,
    private val notificationRepository: NotificationRepositoryInterface,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    val alarmList: StateFlow<List<AlarmInfoInterface>> =
        alarmRepository.getAllItemOrderByTimeAsc().map { alarmList ->
            if (alarmList.isNullOrEmpty()) return@map listOf()
            alarmList.map { alarm -> alarm.toAlarmInfoInterface() }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000L),
            initialValue = INITIAL_ALARM_LIST
        )

    val notificationList: StateFlow<List<NotificationInfoInterface>> =
        notificationRepository.getAllNotifications().map { notification ->
            if (notification.isNullOrEmpty()) return@map listOf()
            notification.map { it }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000L),
            initialValue = INITIAL_NOTIFY_LIST
        )

    var isShowTitleInputDialog by mutableStateOf(false)
        private set

    var isShowChangeTimeDialog by mutableStateOf(false)
        private set

    var nowProcessing by mutableStateOf(false)
        private set

    private var alarmTrash: Pair<AlarmInfoInterface, NotificationInfoInterface>? = null

    // アラームリストでタップしたアラーム
    private var selectedAlarm: AlarmInfoInterface = INITIAL_ALARM

    // 新たに追加したアラームのID
    private var idOfAddedAlarm by mutableStateOf<Long?>(INITIAL_ID)

    // 通知権限の許可取得ダイアログが一度表示されたかどうか
    var isShowedPermissionDialog: StateFlow<Boolean> =
        preferencesRepository.isShowedPermissionDialog.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000L),
            initialValue = true
        )

    var showDialog by mutableStateOf(false)
        private set

    fun showDialog(): HomeScreenViewModel {
        showDialog = true
        return this
    }

    fun hiddenDialog(): HomeScreenViewModel {
        showDialog = false
        return this
    }

    fun showTitleInputDialog(): HomeScreenViewModel {
        isShowTitleInputDialog = true
        showDialog()
        return this
    }

    fun hiddenTitleInputDialog(): HomeScreenViewModel {
        isShowTitleInputDialog = false
        hiddenDialog()
        return this
    }

    /**
     * アラームを選択するときに呼び出すメソッド
     */
    fun selectAlarm(alarm: AlarmInfoInterface): HomeScreenViewModel {
        selectedAlarm = alarm
        return this
    }

    fun updateAlarmName(name: String): HomeScreenViewModel {
        if (selectedAlarm != INITIAL_ALARM) {
            locked {
                nowProcessing = true
                val alarm = getSelectedAlarmFromDb()
                if (alarm == null) {
                    nowProcessing = false
                    hiddenDialog()
                    hiddenTitleInputDialog()
                    return@locked
                }
                val targetNotify = getAlarmNotifyFromList(alarm.id)
                if (targetNotify == null) {
                    nowProcessing = false
                    hiddenDialog()
                    hiddenTitleInputDialog()
                    return@locked
                }
                val deferred = async {
                    // セットしたアラームのタイトルを変更したとき，通知のタイトルも変える
                    notificationRepository.updateNotification(
                        (targetNotify as NotificationEntity).copy(
                            notifyName = name
                        )
                    )
                    alarmRepository.update(alarm.toAlarmInfoEntity().copy(name = name))
                    return@async false
                } // async
                viewModelScope.launch { nowProcessing = deferred.await() }
            } // locked
        } // if
        return this
    }

    fun getSelectedAlarm(): AlarmInfoInterface {
        return selectedAlarm
    }

    fun getSelectedAlarmName(): String {
        return selectedAlarm.name
    }

    fun undoDelete(undoSuccessAction: (Boolean) -> Unit) {
        val tmp = popTrash()
        if (tmp == null) {
            undoSuccessAction(false)
            return
        }
        val deferred = viewModelScope.async {
            nowProcessing = true
            selectAlarm(tmp.first)
            if (tmp.second != INITIAL_NOTIFY) {
                notificationRepository.insertNotification(tmp.second as NotificationEntity)
            }
            alarmRepository.insert(tmp.first.toAlarmInfoEntity())
            releaseSelectedAlarm()
            undoSuccessAction(true)
            return@async false
        }
        viewModelScope.launch { nowProcessing = deferred.await() }
    }

    private fun putTrash(alarm: AlarmInfoInterface, notify: NotificationInfoInterface) {
        alarmTrash = Pair(alarm, notify)
    }

    fun popTrash(): Pair<AlarmInfoInterface, NotificationInfoInterface>? {
        val tmp = alarmTrash
        alarmTrash = null
        return tmp
    }

    fun changeEnableTo(
        enabled: Boolean,
        changeEnableAction: (Boolean) -> Unit
    ): HomeScreenViewModel {
        if (selectedAlarm != INITIAL_ALARM) {
            locked {
                nowProcessing = true
                val alarm = getSelectedAlarmFromDb()
                if (alarm == null) {
                    nowProcessing = false
                    return@locked
                }
                val targetNotify = getAlarmNotifyFromList(alarm.id)
                val deferred = async {
                    if (enabled) { // アラームを有効にした場合
                        val triggerTimeMilliSeconds = getMilliSecondsOfNextTime(
                            alarm.hour,
                            alarm.min,
                            ZonedDateTime.now(ZoneId.of(alarm.zoneId))
                        )
                        val notifyId = UUID.randomUUID().hashCode()
                        val notification = NotificationEntity(
                            notifyId = targetNotify?.notifyId
                                ?: notifyId, // なぜかtargetNotifyが存在していた場合はupdateする
                            alarmId = alarm.id,
                            triggerTimeMilliSeconds = triggerTimeMilliSeconds,
                            notifyName = alarm.name,
                            zoneId = alarm.zoneId
                        )
                        if (targetNotify == null) {
                            notificationRepository.insertNotification(notification)
                        } else {
                            notificationRepository.updateNotification(notification)
                        }
                        changeEnableAction(true)
                    } else { // アラームを無効にした場合
                        if (targetNotify == null) return@async false // そもそもセットされていなかった場合
                        notificationRepository.deleteNotification(targetNotify as NotificationEntity)
                        changeEnableAction(false)
                    }
                    return@async false
                } // async
                viewModelScope.launch { nowProcessing = deferred.await() }
            } // locked
        } // if
        return this
    }

    /**
     * 選択したアラームを解除する
     */
    private fun releaseSelectedAlarm() {
        selectedAlarm = INITIAL_ALARM
    }

    fun delete(deleteAction: () -> Unit): HomeScreenViewModel {
        locked {
            nowProcessing = true
            val deferred = viewModelScope.async {
                alarmRepository.delete(selectedAlarm.id) // これを最後に書くと，アラームのSwitchがOffになってから(アラームが)画面(LazyColumn)から消える
                val targetNotify = getNotifyOfSelectedAlarm()
                if (targetNotify != null) {  // アラームを有効化していた
                    putTrash(selectedAlarm, targetNotify)
                    notificationRepository.deleteNotification(targetNotify as NotificationEntity)
                }else {
                    putTrash(selectedAlarm, INITIAL_NOTIFY)
                }
                releaseSelectedAlarm()
                deleteAction()
                return@async false
            }
            viewModelScope.launch { nowProcessing = deferred.await() }
        }
        return this
    }

    fun showPermissionDialog() {
        viewModelScope.launch {
            preferencesRepository.showedPermissionDialog()
            showDialog()
        }
    }

    fun getNotifyOfSelectedAlarm(): NotificationInfoInterface? {
        if (selectedAlarm != INITIAL_ALARM) {
            return getAlarmNotifyFromList(selectedAlarm.id)
        }
        return null
    }

// === 以下，追加したアラームへスクロールするための機能 ===
    /**
     * 新たに追加したアラームのID(DBのプライマリキー)を保存
     */
    fun setAddItemId(id: Long) {
        idOfAddedAlarm = id
    }

    fun initAddItemId() {
        idOfAddedAlarm = INITIAL_ID
    }

    /**
     * アラームを追加した後，アラームを削除する場合，idOfAddedAlarmが変更されてなかった場合，idOfAddedAlarmの位置へスクロールしてしまう
     * これを防ぐために，idOfAddedAlarmが初期値かどうかを判定して，初期値ならばスクロールしないようにする
     */
    fun isScroll(): Boolean {
        return idOfAddedAlarm != INITIAL_ID
    }

    /**
     * 新たに追加したアラームのリスト内でのIndexを返す
     */
    fun getAddedItemIndex(): Int {
        if (idOfAddedAlarm == INITIAL_ID) return 0
        val tmpList = alarmList.value
        for (i in tmpList.indices) {
            if (tmpList[i].id == idOfAddedAlarm) {
                return i
            }
        }
        return 0
    }
// === 以上，追加したアラームへスクロールするための機能 ===

    suspend fun resettingNotify(context: Context) {
        Utility.resettingNotify(context, notificationRepository)
    }

    private suspend fun getSelectedAlarmFromDb(): AlarmInfoInterface? {
        return alarmRepository.getItem(selectedAlarm.id).firstOrNull()
    }

    private fun getAlarmNotifyFromList(alarmId: Long): NotificationInfoInterface? {
        return notificationList.value.find { it.alarmId == alarmId }
    }

    private val scope = viewModelScope
    private val mutex = Mutex()
    // lockedで囲まれた異なる処理が同時に行われることはない
    // これにより，アラームの削除ボタンと有効化を同時に押すことによる，「deleteしたのにchangeEnable(true)」となる事態を避ける
    private fun locked(block: suspend CoroutineScope.() -> Unit) {
        scope.launch {
            mutex.withLock {
                block()
            }
        }
    }
}


// === 以下クラス外 ===
private fun AlarmInfoEntity.toAlarmInfoInterface(): AlarmInfoInterface {
    return AlarmInfo(
        id = this.id, hour = this.hour, min = this.min, name = this.name, zoneId = this.zoneId
    )
}

private fun AlarmInfoInterface.toAlarmInfoEntity(): AlarmInfoEntity {
    return AlarmInfoEntity(
        id = this.id, hour = this.hour, min = this.min, name = this.name, zoneId = this.zoneId
    )
}
