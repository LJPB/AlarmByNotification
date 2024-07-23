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
import me.ljpb.alarmbynotification.data.NotificationRepositoryInterface
import me.ljpb.alarmbynotification.data.room.AlarmInfoEntity
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
    
    // ダイアログの表示状態
    var isShow by mutableStateOf(false)
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
        isShow = true
    }

    fun hiddenDialog() {
        isShow = false
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
            notificationRepository.insertNotification(notification)
            setAddedItemId(alarmId)
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