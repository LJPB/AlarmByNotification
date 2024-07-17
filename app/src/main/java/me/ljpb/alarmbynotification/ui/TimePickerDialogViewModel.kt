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
import me.ljpb.alarmbynotification.data.NotificationEntity
import me.ljpb.alarmbynotification.data.NotificationRepositoryInterface
import me.ljpb.alarmbynotification.data.TimeType
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

    // タイマーにセットする時間を管理する
    @OptIn(ExperimentalMaterial3Api::class)
    lateinit var timerState: TimePickerState
        private set

    // ダイアログの表示状態
    var isShow by mutableStateOf(false)
        private set
    var isAlarm by mutableStateOf(true)
        private set

    init {
        setAlarmInitialState()
        setTimerInitialState()
    }

    fun showAlarmDialog(hour: Int? = null, min: Int? = null, is24Hour: Boolean? = null) {
        isShow = true
        isAlarm = true
        setAlarmInitialState(hour = hour, min = min, is24Hour = is24Hour)
    }

    fun showTimerDialog(hour: Int? = null, min: Int? = null, is24Hour: Boolean? = null) {
        isShow = true
        isAlarm = false
        setAlarmInitialState(hour = hour, min = min, is24Hour = is24Hour)
    }

    fun hiddenDialog() {
        isShow = false
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun add(setAddedItemToTmp: (Long) -> Unit) {
        val type: TimeType
        val triggerTimeSeconds: Long
        val currentDateTime = ZonedDateTime.now()

        if (isAlarm) {
            type = TimeType.Alarm

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
            triggerTimeSeconds = currentDateTime
                .plusMinutes(addMinutes)
                .minusSeconds(currentDateTime.second.toLong())  // 秒は無視する
                .minusNanos(currentDateTime.nano.toLong())
                .toEpochSecond()
        
        } else {
            type = TimeType.Timer
            triggerTimeSeconds = currentDateTime
                .plusHours(timerState.hour.toLong())
                .plusMinutes(timerState.minute.toLong())
                .toEpochSecond()
        }

        val notifyId = UUID.randomUUID().hashCode()
        val notification = NotificationEntity(
            notifyId = notifyId,
            title = "",
            text = "",
            triggerTimeMilliSeconds = triggerTimeSeconds * 1000,
            type = type,
        )
        setAddedItemToTmp(triggerTimeSeconds * 1000)
        viewModelScope.launch {
            repository.insertNotification(notification)
        }
    }

    /**
     * 表示中のダイアログの切り替え
     */
    @OptIn(ExperimentalMaterial3Api::class)
    fun changeDialog() {
        if (isAlarm) {
            setTimerStateFromAlarmStateBasedCurrentTime()
            isAlarm = false
        } else {
            isAlarm = true
            setAlarmStateFromTimerStateBasedCurrentTime()
        }
    }

    /** 現在時刻を基準にアラームにセットした時刻をタイマーの残り時間に変換してtimerStateにセットする
     * たとえば現在10:00で，アラームに11:30とセットしていた場合，90分(1時間30分)に変換してセットする
     */
    @OptIn(ExperimentalMaterial3Api::class)
    private fun setTimerStateFromAlarmStateBasedCurrentTime(alarmState: TimePickerState = this.alarmState) =
        setTimerStateFromAlarmStateBasedCurrentTime(
            alarmState.hour,
            alarmState.minute,
        )

    /** 現在時刻を基準にアラームにセットした時刻をタイマーの残り時間に変換してtimerStateにセットする
     * たとえば現在10:00で，アラームに11:30とセットしていた場合，90分(1時間30分)に変換してセットする
     * @param alarmHour アラームにセットした時刻の時間
     * @param alarmMin アラームにセットした時刻の分
     */
    private fun setTimerStateFromAlarmStateBasedCurrentTime(alarmHour: Int, alarmMin: Int) {
        val currentTime = LocalTime.now()
        val currentTimeAsMinute = currentTime.hour * 60 + currentTime.minute
        val alarmTimeAsMinute = alarmHour * 60 + alarmMin

        // 現在の時刻とアラームにセットした時刻の差
        val diffTimeAsMinute = if (currentTimeAsMinute > alarmTimeAsMinute) {
            // 「現在時刻 > アラームにセットした時刻」ならアラームにセットした時刻は翌日の時刻
            val diff = currentTimeAsMinute - alarmTimeAsMinute
            24 * 60 - diff
        } else {
            alarmTimeAsMinute - currentTimeAsMinute
        }
        setTimerInitialState(
            hour = diffTimeAsMinute / 60,
            min = diffTimeAsMinute % 60,
        )
    }

    /** 現在時刻を基準にタイマーにセットした時間をアラームの時刻に変換してalarmStateにセットする
     * たとえば現在10:00で，タイマーに90分(1時間30分)とセットしていた場合，11:30に変換してセットする
     * @param is24Hour 24時間表示かどうか, null(未指定)なら端末の設定による
     */
    @OptIn(ExperimentalMaterial3Api::class)
    private fun setAlarmStateFromTimerStateBasedCurrentTime(
        timePickerState: TimePickerState = this.timerState,
        is24Hour: Boolean? = null
    ) =
        setAlarmStateFromTimerStateBasedCurrentTime(
            timerHour = timePickerState.hour,
            timerMin = timePickerState.minute,
            is24Hour = is24Hour
        )

    /** 現在時刻を基準にタイマーにセットした時間をアラームの時刻に変換してalarmStateにセットする
     * たとえば現在10:00で，タイマーに90分(1時間30分)とセットしていた場合，11:30に変換してセットする
     * @param timerHour タイマーにセットした時間
     * @param timerMin タイマーにセットした分
     * @param is24Hour 24時間表示かどうか, null(未指定)なら端末の設定による
     */
    private fun setAlarmStateFromTimerStateBasedCurrentTime(
        timerHour: Int,
        timerMin: Int,
        is24Hour: Boolean? = null
    ) {
        val currentTime = LocalTime.now()
        val currentTimeAsMinute = currentTime.hour * 60 + currentTime.minute
        val timerTimeAsMinute = timerHour * 60 + timerMin
        var alarmTimeAsMinute = currentTimeAsMinute + timerTimeAsMinute

        // 現在の時刻 + タイマーにセットした時間
        alarmTimeAsMinute = if (alarmTimeAsMinute >= 24 * 60) {
            // タイマーにセットした時間が経過すると23:59を超える場合
            alarmTimeAsMinute - (24 * 60)
        } else {
            alarmTimeAsMinute
        }
        setAlarmInitialState(
            hour = alarmTimeAsMinute / 60,
            min = alarmTimeAsMinute % 60,
            is24Hour = is24Hour
        )
    }

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

    @OptIn(ExperimentalMaterial3Api::class)
    private fun setTimerInitialState(hour: Int? = null, min: Int? = null) {
        val initialHour: Int
        val initialMinute: Int
        if (hour == null || min == null) {
            initialHour = 0
            initialMinute = 0
        } else {
//            if (hour > 23 || min > 59) throw IllegalArgumentException("Timer Initial Hour : $hour, Min : $min")
            initialHour = hour
            initialMinute = min
        }
        timerState = TimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )
    }

}