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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ljpb.alarmbynotification.data.NotificationRepositoryInterface
import me.ljpb.alarmbynotification.data.TimeData
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

// 現在時刻を取得するための更新間隔
const val DELAY_TIME: Long = 100L

class HomeScreenViewModel(private val repository: NotificationRepositoryInterface) : ViewModel() {
    private val _currentDateTime = MutableStateFlow(LocalDateTime.now())
    val currentDateTime: StateFlow<LocalDateTime> = _currentDateTime.asStateFlow()

    val setTimeList: StateFlow<List<TimeData>> = repository
        .getAllNotifications()
        .map { notificationEntities ->
            if (notificationEntities.isNullOrEmpty()) return@map listOf()
            notificationEntities.map { notification ->
                val zonedDateTime = Instant.ofEpochSecond(notification.triggerTimeMilliSeconds)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                TimeData(
                    id = notification.notifyId,
                    name = notification.title,
                    type = notification.type,
                    finishDateTime = zonedDateTime
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000L),
            initialValue = listOf()
        )

    var titleInputDialogIsShow by mutableStateOf(false)
        private set
    var timeUpdateDialogIsShow by mutableStateOf(false)
        private set
    
    private var selectedTimeDate: TimeData? = null
    
    fun showTitleInputDialog(timeData: TimeData) {
        titleInputDialogIsShow = true
        selectedTimeDate = timeData
    }
    
    fun hiddenTitleInputDialog() {
        titleInputDialogIsShow = false
        selectedTimeDate = null
    }
    
    fun getDefaultTitle(): String {
        if (selectedTimeDate == null) {
            hiddenTitleInputDialog()
            return ""
        }
        return selectedTimeDate!!.name
    }
    
    fun showTimeUpdateDialog(timeData: TimeData) {
        timeUpdateDialogIsShow = true
        selectedTimeDate = timeData
    }
    
    fun hiddenTimeUpdateDialog() {
        timeUpdateDialogIsShow = false
        selectedTimeDate = null
    }

    fun setTitle(title: String) {
        if (selectedTimeDate == null) return
        viewModelScope.launch {
            val notify = repository
                .getNotification(selectedTimeDate!!.id)
                .firstOrNull()
            if (notify != null) {
                val newNotify = notify.copy(title = title)
                repository.updateNotification(newNotify)
            }
        }
    }

    fun changeTime() {

    }

    fun delete(timeData: TimeData) {
        viewModelScope.launch {
            val notify = repository
                .getNotification(timeData.id)
                .firstOrNull()
            if (notify != null) {
                repository.deleteNotification(notify)
            }
        }
    }
    
    suspend fun updateCurrentDateTime() {
        while (true) {
            _currentDateTime.update { LocalDateTime.now() }
            delay(DELAY_TIME)
        }
    }

    
}