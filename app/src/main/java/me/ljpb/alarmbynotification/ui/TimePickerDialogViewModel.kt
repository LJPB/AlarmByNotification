package me.ljpb.alarmbynotification.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.ljpb.alarmbynotification.Utility.getMilliSecondsOfNextTime
import me.ljpb.alarmbynotification.Utility.getZoneId
import me.ljpb.alarmbynotification.data.AlarmRepositoryInterface
import me.ljpb.alarmbynotification.data.NotificationInfoInterface
import me.ljpb.alarmbynotification.data.NotificationRepositoryInterface
import me.ljpb.alarmbynotification.data.UserPreferencesRepository
import me.ljpb.alarmbynotification.data.room.AlarmInfoEntity
import me.ljpb.alarmbynotification.data.room.AlarmInfoInterface
import me.ljpb.alarmbynotification.data.room.NotificationEntity
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

class TimePickerDialogViewModel(
    private val notificationRepository: NotificationRepositoryInterface,
    private val alarmRepository: AlarmRepositoryInterface,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {
    // アラームにセットする時刻を管理する
    @OptIn(ExperimentalMaterial3Api::class)
    lateinit var alarmState: TimePickerState
        private set

    // 新しくアラームを追加するためのダイアログの表示状態
    var isShowAddDialog by mutableStateOf(false)
        private set

    // 既存のアラームの時間を変更すためのダイアログの表示状態
    var isShowUpdateDialog by mutableStateOf(false)
        private set

    var nowProcessing by mutableStateOf(false)
        private set

    // TimePickerDialogダイアログで最後に表示した時間選択コンポーネントが
    // TimePickerかTimeInputか
    val isTimePicker: StateFlow<Boolean> = preferencesRepository.isTimePicker.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(2_000L),
        initialValue = true
    )

    init {
        setAlarmInitialState(true)
    }

    fun showAlarmAddDialog(
        hour: Int? = null,
        min: Int? = null,
        is24Hour: Boolean,
        showAction: () -> Unit
    ) {
        showAction()
        if (hour != null && min != null) {
            setAlarmState(hour, min, is24Hour)
        } else {
            setAlarmInitialState(is24Hour)
        }
        isShowAddDialog = true
    }

    fun hiddenAlarmAddDialog(hiddenAction: () -> Unit) {
        hiddenAction()
        isShowAddDialog = false
    }

    fun showUpdateDialog(alarm: AlarmInfoInterface, is24Hour: Boolean, showAction: () -> Unit) {
        setAlarmState(alarm.hour, alarm.min, is24Hour)
        isShowUpdateDialog = true
        showAction()
    }

    fun hiddenUpdateDialog(hiddenAction: () -> Unit) {
        hiddenAction()
        isShowUpdateDialog = false
    }

    /**
     *  TimePickerDialogで最後に表示した時間選択コンポーネントの種類を[isTimePicker]がtrueならTimePicker, falseならTimeInputとして記憶する
     */
    fun setRecentlyComponentIsTimePicker(isTimePicker: Boolean) = viewModelScope.launch {
        preferencesRepository.recentlyIsTimePicker(isTimePicker)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun add(setAddedItemId: (Long) -> Unit) {
        // TODO: alarmDBにnotifyに対応するalarmが存在するかをチェックする 
        nowProcessing = true
        val zoneId = getZoneId()
        val alarmInfoEntity = AlarmInfoEntity(
            hour = alarmState.hour,
            min = alarmState.minute,
            zoneId = zoneId,
        )
        val triggerTimeMilliSeconds = getMilliSecondsOfNextTime(
            alarmInfoEntity.hour,
            alarmInfoEntity.min,
            ZonedDateTime.now(ZoneId.of(zoneId))
        )
        val notifyId = UUID.randomUUID().hashCode()
        val deferred = viewModelScope.async {
            val alarmId = alarmRepository.insert(alarmInfoEntity)
            val notification = NotificationEntity(
                notifyId = notifyId,
                alarmId = alarmId,
                triggerTimeMilliSeconds = triggerTimeMilliSeconds,
                notifyName = "",
                zoneId = zoneId
            )
            setAddedItemId(alarmId)
            notificationRepository.insertNotification(notification)
            return@async false
        }
        viewModelScope.launch { nowProcessing = deferred.await() }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun updateTime(targetAlarm: AlarmInfoInterface?, targetNotify: NotificationInfoInterface?, updatedAction: (Boolean) -> Unit) {
        if (targetAlarm != null) {
            nowProcessing = true
            val timeZone = targetAlarm.zoneId
            val hour = alarmState.hour
            val min = alarmState.minute
            val deferred = viewModelScope.async {
                alarmRepository.update(
                    AlarmInfoEntity(
                        id = targetAlarm.id,
                        name = targetAlarm.name,
                        hour = hour,
                        min = min,
                        zoneId = timeZone
                    )
                )
                if (targetNotify != null && alarmRepository.getItem(targetAlarm.id)
                        .firstOrNull() != null
                ) { // 時間変更対象となるアラームを有効化していた場合
                    notificationRepository.updateNotification(
                        NotificationEntity(
                            notifyId = targetNotify.notifyId,
                            alarmId = targetNotify.alarmId,
                            triggerTimeMilliSeconds = getMilliSecondsOfNextTime(
                                hour,
                                min,
                                ZonedDateTime.now(ZoneId.of(timeZone))
                            ),
                            notifyName = targetNotify.notifyName,
                            zoneId = timeZone,
                        )
                    )
                    updatedAction(true)
                } else {
                    updatedAction(false)
                }
                return@async false
            }
            viewModelScope.launch { nowProcessing = deferred.await() }
        }
    }

    /**
     * 追加するアラームの時刻を管理するプロパティを初期値(呼び出した時点での時刻)に戻す
     */
    private fun setAlarmInitialState(is24Hour: Boolean) {
        val currentTime = LocalTime.now()
        val initialHour = currentTime.hour
        val initialMinute = currentTime.minute
        setAlarmState(
            hour = initialHour,
            min = initialMinute,
            is24 = is24Hour
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun setAlarmState(hour: Int, min: Int, is24: Boolean) {
        alarmState = TimePickerState(
            initialHour = hour,
            initialMinute = min,
            is24Hour = is24
        )
    }


}