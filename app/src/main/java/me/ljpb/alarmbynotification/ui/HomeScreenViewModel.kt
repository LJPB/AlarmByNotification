package me.ljpb.alarmbynotification.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import me.ljpb.alarmbynotification.data.DialogDefaultContentPreferencesRepository
import me.ljpb.alarmbynotification.data.NotificationRepositoryInterface
import me.ljpb.alarmbynotification.data.TimeData
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

// 現在時刻を取得するための更新間隔
const val DELAY_TIME: Long = 100L

class HomeScreenViewModel(
    private val repository: NotificationRepositoryInterface,
    private val preferencesRepository: DialogDefaultContentPreferencesRepository,
) : ViewModel() {
    private val _currentDateTime = MutableStateFlow(LocalDateTime.now())
    val currentDateTime: StateFlow<LocalDateTime> = _currentDateTime.asStateFlow()

    val setTimeList: StateFlow<List<TimeData>> = repository
        .getAllNotifications()
        .map { notificationEntities ->
            if (notificationEntities.isNullOrEmpty()) return@map listOf()
            notificationEntities.map { notification ->
                val zonedDateTime =
                    Instant.ofEpochSecond(notification.triggerTimeMilliSeconds / 1000)
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
    
    val dialogDefaultContentIsAlarm: StateFlow<Boolean> = preferencesRepository.isAlarmDefault
        .map { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000L),
            initialValue = true
        )

    // addedItemTriggerTimeMilliSecondsの初期値
    private val initialSeconds = -1L

    // setTimeListに新たに追加された通知のtriggerTime
    private var addedItemTriggerTimeMilliSeconds by mutableLongStateOf(initialSeconds)

    var titleInputDialogIsShow by mutableStateOf(false)
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

    fun changeDefaultContent(defaultIsAlarm: Boolean) {
        viewModelScope.launch {
            preferencesRepository.changeDialogDefaultContent(defaultIsAlarm)
        }
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

    fun setAddItemTime(triggerTimeMilliSeconds: Long) {
        addedItemTriggerTimeMilliSeconds = triggerTimeMilliSeconds
    }

    fun initAddItemTime() {
        addedItemTriggerTimeMilliSeconds = initialSeconds
    }

    fun isScroll(): Boolean {
        // addedItemTriggerTimeMilliSecondsが初期値ならスクロールしない
        // setTimeListからアイテムが削除された場合にスクロールしないようにするためのもの
        return addedItemTriggerTimeMilliSeconds != initialSeconds
    }

    fun getAddedItemIndex(): Int {
        val addItemFinishDateTime = Instant.ofEpochSecond(addedItemTriggerTimeMilliSeconds / 1000)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        val tmpList = setTimeList.value
        var start = 0
        var end = tmpList.size - 1
        var middle = (start + end) / 2

        while (start <= end) {
            Log.d("start", start.toString())
            Log.d("end", end.toString())
            Log.d("middle", middle.toString())
            if (addItemFinishDateTime == tmpList[start].finishDateTime) {
                return start
            }
            if (addItemFinishDateTime == tmpList[end].finishDateTime) {
                return end
            }
            if (addItemFinishDateTime == tmpList[middle].finishDateTime) {
                return middle
            }
            if (addItemFinishDateTime.isAfter(tmpList[middle].finishDateTime)) {
                start = middle + 1
            } else if (addItemFinishDateTime.isBefore(tmpList[middle].finishDateTime)) {
                end = middle - 1
            }
            middle = (start + end) / 2
        }
        return 0
    }
}