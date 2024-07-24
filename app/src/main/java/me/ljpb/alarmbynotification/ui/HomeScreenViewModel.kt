package me.ljpb.alarmbynotification.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ljpb.alarmbynotification.Utility.getMilliSecondsOfNextTime
import me.ljpb.alarmbynotification.data.AlarmInfo
import me.ljpb.alarmbynotification.data.AlarmRepository
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

    val notificationList: StateFlow<List<NotificationEntity>> =
        notificationRepository
            .getAllNotifications()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000L),
                initialValue = listOf()
            )

    var isShowTitleInputDialog by mutableStateOf(false)
        private set

    var isShowChangeTimeDialog by mutableStateOf(false)
        private set

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
            viewModelScope.launch {
                // セットしたアラームのタイトルを変更したとき，通知のタイトルも変える
                val notify = notificationList.value.find { it.alarmId == selectedAlarm!!.id }
                if (notify != null) {
                    notificationRepository.updateNotification(
                        (notify as NotificationEntity).copy(
                            notifyName = name
                        )
                    )
                }
            }
            viewModelScope.launch {
                // アラームデータベースの更新
                alarmRepository.update(selectedAlarm!!.toAlarmInfoEntity().copy(name = name))
            }
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

    fun changeEnableTo(enabled: Boolean): HomeScreenViewModel {
        if (selectedAlarm != null) {
            val alarmId = selectedAlarm!!.id
            val zoneId = selectedAlarm!!.zoneId
            val name = selectedAlarm!!.name
            if (enabled) { // アラームを有効にした場合
                // TODO: TimePickerDialogViewModelのaddと同じ処理だからまとめる
                val triggerTimeMilliSeconds = getMilliSecondsOfNextTime(
                    selectedAlarm!!.hour, selectedAlarm!!.min, ZonedDateTime.now()
                )
                val notifyId = UUID.randomUUID().hashCode()
                viewModelScope.launch {
                    val notification = NotificationEntity(
                        notifyId = notifyId,
                        alarmId = alarmId,
                        triggerTimeMilliSeconds = triggerTimeMilliSeconds,
                        notifyName = name,
                        zoneId = zoneId
                    )
                    notificationRepository.insertNotification(notification)
                }
            } else { // アラームを無効にした場合
                val targetNotify = notificationList.value.find { it.alarmId == alarmId }
                if (targetNotify == null) return this
                viewModelScope.launch {
                    notificationRepository.deleteNotification(targetNotify as NotificationEntity)
                }
            }
        }
        return this
    }

    /**
     * 選択したアラームを解除する
     */
    fun releaseSelectedAlarm() {
        selectedAlarm = null
    }

    fun delete(): HomeScreenViewModel {
        if (selectedAlarm != null) {
            viewModelScope.launch {
                alarmRepository.delete(selectedAlarm!!.id)
            }
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

    fun getNotifyOfSelectedAlarm(): NotificationEntity? {
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
