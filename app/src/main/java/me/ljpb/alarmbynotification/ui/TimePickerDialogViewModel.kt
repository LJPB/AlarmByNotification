package me.ljpb.alarmbynotification.ui

import android.app.Application
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.ljpb.alarmbynotification.Utility.localDateTimeToFormattedTime
import me.ljpb.alarmbynotification.data.NotificationInfoInterface
import me.ljpb.alarmbynotification.data.NotificationRepositoryInterface
import me.ljpb.alarmbynotification.data.room.NotificationEntity
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.UUID

class TimePickerDialogViewModel(
    private val repository: NotificationRepositoryInterface,
    app: Application,
) : AndroidViewModel(app) {
    // アラームにセットする時刻を管理する
    @OptIn(ExperimentalMaterial3Api::class)
    lateinit var alarmState: TimePickerState
        private set

    // ダイアログの表示状態
    var isShow by mutableStateOf(false)
        private set

    init {
        setAlarmInitialState()
    }

    fun showAlarmDialog(hour: Int? = null, min: Int? = null, is24Hour: Boolean? = null) {
        isShow = true
        setAlarmInitialState(hour = hour, min = min, is24Hour = is24Hour)
    }


    fun hiddenDialog() {
        isShow = false
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun add(setAddedItemToTmp: (NotificationInfoInterface) -> Unit) {
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
    }

    /**
     * 追加するアラームの時刻を管理するプロパティを初期値に戻す
     */
    @OptIn(ExperimentalMaterial3Api::class)
    private fun setAlarmInitialState(
        hour: Int? = null,
        min: Int? = null,
        is24Hour: Boolean? = null,
    ) {
        val currentTime = LocalTime.now()
        val initialHour: Int
        val initialMinute: Int
        val initialIs24Hour = is24Hour ?: android.text.format.DateFormat.is24HourFormat(
            getApplication<Application>().applicationContext
        )
        if (hour == null || min == null) {
            initialHour = currentTime.hour
            initialMinute = currentTime.minute
        } else {
            initialHour = hour
            initialMinute = min
        }
        alarmState = TimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = initialIs24Hour
        )
    }

}