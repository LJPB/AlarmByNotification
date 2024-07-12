package me.ljpb.alarmbynotification.ui

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

    val setTimeIsEmpty: StateFlow<Boolean> = repository
        .count()
        .map { it != 0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000L),
            initialValue = true
        )
    
    val setTimeList: StateFlow<List<TimeData>> = repository
        .getAllNotifications()
        .map { notificationEntities ->
            if (notificationEntities.isNullOrEmpty()) return@map listOf()
            notificationEntities.map { notification ->
                val zonedDateTime = Instant.ofEpochSecond(notification.triggerTime)
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
    
    suspend fun updateCurrentDateTime() {
        while (true) {
            _currentDateTime.update { LocalDateTime.now() }
            delay(DELAY_TIME)
        }
    }
    
}