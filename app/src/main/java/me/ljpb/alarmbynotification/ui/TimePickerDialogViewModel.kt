package me.ljpb.alarmbynotification.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.ljpb.alarmbynotification.Utility.getZoneId
import me.ljpb.alarmbynotification.data.AlarmRepositoryInterface
import me.ljpb.alarmbynotification.data.NotificationRepositoryInterface
import me.ljpb.alarmbynotification.data.room.AlarmInfoEntity
import java.time.LocalTime

class TimePickerDialogViewModel(
    private val repository: NotificationRepositoryInterface,
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
        viewModelScope.launch {
            val id = alarmRepository.insert(alarmInfoEntity)
            setAddedItemId(id)
        }
        /*
        val triggerTimeSeconds: Long
        val currentDateTime = ZonedDateTime.now()
        val zoneId = currentDateTime.zone.id
        val text: String
        val setTimeAsMinute = alarmState.hour * 60 + alarmState.minute
        val currentTimeAsMinute = currentDateTime.hour * 60 + currentDateTime.minute

        // セットした時刻と(日付を無視した)現在時刻の差
        val dif = setTimeAsMinute - currentTimeAsMinute

        val addMinutes = if (dif > 0) {
            // セットした時刻 - (日付を無視した)現在時刻 > 0なら，セットした時刻は現在と同じ日付の時刻ということ
            // よって差分を足せば，現在の日付でセットした時刻を得られる
            dif.toLong()
        } else if (dif < 0) {
            // セットした時刻 - (日付を無視した)現在時刻 < 0なら，セットした時刻は現在の翌日の時刻ということ
            // よって，現在の1日後から差分の絶対値を引けば(difは負だから+difする)，現在の翌日でセットした時刻を得られる
            (24 * 60 + dif).toLong()
        } else {
            0  // todo
        }

        val triggerDateTime = currentDateTime
            .plusMinutes(addMinutes)
            .minusSeconds(currentDateTime.second.toLong())  // 秒は無視する
            .minusNanos(currentDateTime.nano.toLong())

        text = localDateTimeToFormattedTime(
            localDateTime = triggerDateTime.toLocalDateTime(),
            isFormat24 = alarmState.is24hour,
            displaySecond = false
        )
        triggerTimeSeconds = triggerDateTime.toEpochSecond()

        val notifyId = UUID.randomUUID().hashCode()
        val notification = NotificationEntity(
            notifyId = notifyId,
            title = "",
            text = text,
            triggerTimeMilliSeconds = triggerTimeSeconds * 1000,
            zoneId = zoneId
        )
        setAddedItemToTmp(notification.copy())
        viewModelScope.launch {
            repository.insertNotification(notification)
        }
         */
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