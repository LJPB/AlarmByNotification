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

    val alarmList: StateFlow<List<AlarmInfo>> = alarmRepository
        .getAllItemOrderByTimeAsc()
        .map { alarmList ->
            if (alarmList.isNullOrEmpty()) return@map listOf()
            alarmList.map { alarm -> alarm.toAlarmInfo() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000L),
            initialValue = listOf()
        )

    val notificationList: StateFlow<List<NotificationInfoInterface>> = notificationRepository
        .getAllNotifications()
        .map { notification ->
            if (notification.isNullOrEmpty()) return@map listOf()
            notification.map { it }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000L),
            initialValue = listOf()
        )

    var isShowTitleInputDialog by mutableStateOf(false)
        private set

    // アラームリストでタップしたアラーム
    private var selectedAlarm: AlarmInfo? = null

    // 新たに追加したアラームのID
    private var idOfAddedAlarm by mutableStateOf<Long?>(INITIAL_ID)

    // 通知権限の許可取得ダイアログが一度表示されたかどうか
    var isShowedPermissionDialog: StateFlow<Boolean> =
        preferencesRepository.isShowedPermissionDialog
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000L),
                initialValue = true
            )

    fun isEnabled(alarmId: Long): Boolean {
        notificationList.value.forEach { notification ->
            if (notification.alarmId == alarmId) return true // 関数から抜けてる
        }
        return false
    }

    /**
     * アラームを選択するときに呼び出すメソッド
     */
    fun selectAlarm(alarm: AlarmInfoInterface): HomeScreenViewModel {
        selectedAlarm = alarm as AlarmInfo
        return this
    }

    fun showTitleInputDialog() {
        isShowTitleInputDialog = true
    }

    fun hiddenTitleInputDialog(): HomeScreenViewModel {
        isShowTitleInputDialog = false
        return this
    }

    fun setAlarmName(name: String): HomeScreenViewModel {
        if (selectedAlarm != null) {
            viewModelScope.launch {
                alarmRepository.update(selectedAlarm!!.toAlarmInfoEntity().copy(name = name))
            }
        }
        return this
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
                    selectedAlarm!!.hour,
                    selectedAlarm!!.min,
                    ZonedDateTime.now()
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
            } else {
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

private fun AlarmInfoEntity.toAlarmInfo(): AlarmInfo {
    return AlarmInfo(
        id = this.id,
        hour = this.hour,
        min = this.min,
        name = this.name,
        zoneId = this.zoneId
    )
}

private fun AlarmInfo.toAlarmInfoEntity(): AlarmInfoEntity {
    return AlarmInfoEntity(
        id = this.id,
        hour = this.hour,
        min = this.min,
        name = this.name,
        zoneId = this.zoneId
    )
}