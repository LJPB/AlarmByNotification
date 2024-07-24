package me.ljpb.alarmbynotification.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.ljpb.alarmbynotification.Utility.getMilliSecondsOfNextTime
import me.ljpb.alarmbynotification.Utility.getZoneId
import me.ljpb.alarmbynotification.data.AlarmRepositoryInterface
import me.ljpb.alarmbynotification.data.NotificationInfoInterface
import me.ljpb.alarmbynotification.data.NotificationRepositoryInterface
import me.ljpb.alarmbynotification.data.room.AlarmInfoEntity
import me.ljpb.alarmbynotification.data.room.AlarmInfoInterface
import me.ljpb.alarmbynotification.data.room.NotificationEntity
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.UUID

class TimePickerDialogViewModel(
    private val notificationRepository: NotificationRepositoryInterface,
    private val alarmRepository: AlarmRepositoryInterface,
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

    init {
        setAlarmInitialState(true)
    }

    fun showAlarmDialog(hour: Int? = null, min: Int? = null, is24Hour: Boolean) {
        if (hour != null && min != null) {
            setAlarmState(hour, min, is24Hour)
        } else {
            setAlarmInitialState(is24Hour)
        }
        isShowAddDialog = true
    }

    fun hiddenDialog() {
        isShowAddDialog = false
    }
    
    fun showUpdateDialog(alarm: AlarmInfoInterface, is24Hour: Boolean) {
        setAlarmState(alarm.hour, alarm.min, is24Hour)
        isShowUpdateDialog = true
    }
    
    fun hiddenUpdateDialog() {
        isShowUpdateDialog = false
    }
    

    @OptIn(ExperimentalMaterial3Api::class)
    fun add(setAddedItemId: (Long) -> Unit) {
        val zoneId = getZoneId()
        val alarmInfoEntity = AlarmInfoEntity(
            hour = alarmState.hour,
            min = alarmState.minute,
            zoneId = zoneId,
        )
        val triggerTimeMilliSeconds = getMilliSecondsOfNextTime(
            alarmInfoEntity.hour,
            alarmInfoEntity.min,
            ZonedDateTime.now()
        )
        val notifyId = UUID.randomUUID().hashCode()
        viewModelScope.launch {
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
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun updateTime(targetAlarm: AlarmInfoInterface?, targetNotify: NotificationInfoInterface?) {
        if (targetAlarm != null) {
            val timeZone = getZoneId()
            val hour = alarmState.hour
            val min = alarmState.minute
            viewModelScope.launch {
                alarmRepository.update(
                    AlarmInfoEntity(
                        id = targetAlarm.id,
                        name = targetAlarm.name,
                        hour = hour,
                        min = min,
                        zoneId = timeZone
                    )
                )
            }
            
            if (targetNotify != null) { // 時間変更対象となるアラームを有効化していた場合
                viewModelScope.launch {
                    notificationRepository.updateNotification(
                        NotificationEntity(
                            notifyId = targetNotify.notifyId,
                            alarmId = targetNotify.alarmId,
                            triggerTimeMilliSeconds = getMilliSecondsOfNextTime(
                                hour,
                                min,
                                ZonedDateTime.now()
                            ),
                            notifyName = targetNotify.notifyName,
                            zoneId = timeZone,
                        )
                    )
                }
            }
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