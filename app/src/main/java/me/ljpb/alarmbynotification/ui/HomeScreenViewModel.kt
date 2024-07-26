package me.ljpb.alarmbynotification.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID

// 現在時刻を取得するための更新間隔
const val DELAY_TIME: Long = 100L

val INITIAL_ID: Long? = null

class HomeScreenViewModel(
    private val alarmRepository: AlarmRepository,
    private val notificationRepository: NotificationRepositoryInterface,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val _currentDateTime = MutableStateFlow(LocalDateTime.now())
    val currentDateTime: StateFlow<LocalDateTime> = _currentDateTime.asStateFlow()

    val alarmList: StateFlow<List<AlarmInfoInterface>> =
        alarmRepository.getAllItemOrderByTimeAsc().map { alarmList ->
            if (alarmList.isNullOrEmpty()) return@map listOf()
            alarmList.map { alarm -> alarm.toAlarmInfoInterface() }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000L),
            initialValue = listOf()
        )

    val notificationList: StateFlow<List<NotificationInfoInterface>> =
        notificationRepository.getAllNotifications().map { notification ->
            if (notification.isNullOrEmpty()) return@map listOf()
            notification.map { it }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000L),
            initialValue = listOf()
        )

    var isShowTitleInputDialog by mutableStateOf(false)
        private set

    var isShowChangeTimeDialog by mutableStateOf(false)
        private set

    var nowProcessing by mutableStateOf(false)
        private set

    private var alarmTrash: Pair<AlarmInfoInterface, Boolean>? = null

    // アラームリストでタップしたアラーム
    private var selectedAlarm: AlarmInfoInterface? = null

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

    /**
     * アラームを選択するときに呼び出すメソッド
     */
    fun selectAlarm(alarm: AlarmInfoInterface): HomeScreenViewModel {
        selectedAlarm = alarm
        return this
    }

    fun showTitleInputDialog(): HomeScreenViewModel {
        isShowTitleInputDialog = true
        return this
    }

    fun hiddenTitleInputDialog(): HomeScreenViewModel {
        isShowTitleInputDialog = false
        return this
    }

    fun updateAlarmName(name: String): HomeScreenViewModel {
        if (selectedAlarm != null) {
            nowProcessing = true
            val deferred = viewModelScope.async {
                // セットしたアラームのタイトルを変更したとき，通知のタイトルも変える
                val notify = notificationList.value.find { it.alarmId == selectedAlarm!!.id }
                if (notify != null && alarmRepository.getItem(notify.alarmId)
                        .firstOrNull() != null
                ) {
                    notificationRepository.updateNotification(
                        (notify as NotificationEntity).copy(
                            notifyName = name
                        )
                    )
                }
                alarmRepository.update(selectedAlarm!!.toAlarmInfoEntity().copy(name = name))
                return@async false
            }
            viewModelScope.launch { nowProcessing = deferred.await() }
        }
        return this
    }

    fun getSelectedAlarm(): AlarmInfoInterface? {
        return selectedAlarm
    }

    fun getSelectedAlarmName(): String {
        if (selectedAlarm != null) {
            selectedAlarm!!.name
        }
        return ""
    }

    fun undoDelete() {
        val tmp = popTrash()
        tmp ?: return
        val deferred = viewModelScope.async {
            nowProcessing = true
            alarmRepository.insert(tmp.first.toAlarmInfoEntity())
            selectAlarm(tmp.first)
            changeEnableTo(tmp.second)
            releaseSelectedAlarm()
            return@async false
        }
        viewModelScope.launch { nowProcessing = deferred.await() }
    }

    fun putTrash(alarm: AlarmInfoInterface): HomeScreenViewModel {
        val enabled = notificationList.value.find { it.alarmId == alarm.id } != null
        alarmTrash = Pair(alarm, enabled)
        return this
    }

    fun popTrash(): Pair<AlarmInfoInterface, Boolean>? {
        val tmp = alarmTrash
        alarmTrash = null
        return tmp
    }

    fun changeEnableTo(enabled: Boolean): HomeScreenViewModel {
        if (selectedAlarm != null) {
            nowProcessing = true
            val alarmId = selectedAlarm!!.id
            val zoneId = selectedAlarm!!.zoneId
            val name = selectedAlarm!!.name
            val deferred = viewModelScope.async {
                if (enabled) { // アラームを有効にした場合
                    // TODO: TimePickerDialogViewModelのaddと同じ処理だからまとめる
                    val triggerTimeMilliSeconds = getMilliSecondsOfNextTime(
                        selectedAlarm!!.hour, selectedAlarm!!.min, ZonedDateTime.now()
                    )
                    val notifyId = UUID.randomUUID().hashCode()
                    val notification = NotificationEntity(
                        notifyId = notifyId,
                        alarmId = alarmId,
                        triggerTimeMilliSeconds = triggerTimeMilliSeconds,
                        notifyName = name,
                        zoneId = zoneId
                    )
                    if (alarmRepository.getItem(notification.alarmId).firstOrNull() != null) {
                        notificationRepository.insertNotification(notification)
                    }
                } else { // アラームを無効にした場合
                    val targetNotify = notificationList.value.find { it.alarmId == alarmId }
                    if (targetNotify == null) return@async false
                    notificationRepository.deleteNotification(targetNotify as NotificationEntity)
                }
                return@async false
            }
            viewModelScope.launch { nowProcessing = deferred.await() }
        }
        return this
    }

    /**
     * 選択したアラームを解除する
     */
    private fun releaseSelectedAlarm() {
        selectedAlarm = null
    }

    fun delete(): HomeScreenViewModel {
        if (selectedAlarm != null) {
            nowProcessing = true
            val deferred = viewModelScope.async {
                alarmRepository.delete(selectedAlarm!!.id)
                return@async false
            }
            viewModelScope.launch { nowProcessing = deferred.await() }
        }
        return this
    }

    fun showPermissionDialog() {
        viewModelScope.launch {
            preferencesRepository.showedPermissionDialog()
        }
    }

    suspend fun updateCurrentDateTime() {
        while (true) {
            _currentDateTime.update { LocalDateTime.now() }
            delay(DELAY_TIME)
        }
    }

    fun getNotifyOfSelectedAlarm(): NotificationInfoInterface? {
        if (selectedAlarm != null) {
            return notificationList.value.find { it.alarmId == selectedAlarm!!.id }
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
