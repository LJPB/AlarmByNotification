package me.ljpb.alarmbynotification.ui.theme

import android.app.Application
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import java.time.LocalTime

class TimePickerDialogViewModel(app: Application) : AndroidViewModel(app) {
    @OptIn(ExperimentalMaterial3Api::class)
    lateinit var timePickerState: TimePickerState
        private set
    var isShow by mutableStateOf(false)
    
    init {
        setTime()
    }

    fun showDialog(
        hour: Int? = null,
        min: Int? = null,
        is24Hour: Boolean? = null
    ) {
        isShow = true
        setTime(
            hour = hour,
            min = min,
            is24Hour = is24Hour
        )
    }

    fun hiddenDialog() {
        isShow = false
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun setTime(
        hour: Int? = null,
        min: Int? = null,
        is24Hour: Boolean? = null
    ) {
        val currentTime = LocalTime.now()
        val initialHour: Int
        val initialMinute: Int
        val initialIs24Hour: Boolean
        if (hour == null || min == null || is24Hour == null) {
            initialHour = currentTime.hour
            initialMinute = currentTime.minute
            initialIs24Hour = android.text.format.DateFormat.is24HourFormat(
                getApplication<Application>().applicationContext
            )
        } else {
            initialHour = hour
            initialMinute = min
            initialIs24Hour = is24Hour
        }
        timePickerState = TimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = initialIs24Hour
        )
    }
    
}