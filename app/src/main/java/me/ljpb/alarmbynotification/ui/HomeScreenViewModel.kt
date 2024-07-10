package me.ljpb.alarmbynotification.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

// 現在時刻を取得するための更新間隔
const val DELAY_TIME: Long = 100L

class HomeScreenViewModel : ViewModel() {
    private val _currentDateTime = MutableStateFlow(LocalDateTime.now())
    
    val currentDateTime: StateFlow<LocalDateTime> = _currentDateTime.asStateFlow()
    
    suspend fun updateCurrentDateTime() {
        while(true) {
            _currentDateTime.update { LocalDateTime.now() }
            delay(DELAY_TIME)
        }
    }
    
}