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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ljpb.alarmbynotification.data.AlarmInfo
import me.ljpb.alarmbynotification.data.AlarmRepository
import me.ljpb.alarmbynotification.data.UserPreferencesRepository
import me.ljpb.alarmbynotification.data.room.AlarmInfoEntity
import me.ljpb.alarmbynotification.data.room.AlarmInfoInterface
import java.time.LocalDateTime

// 現在時刻を取得するための更新間隔
const val DELAY_TIME: Long = 100L

val INITIAL_ID: Long? = null

class HomeScreenViewModel(
    private val alarmRepository: AlarmRepository,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val _currentDateTime = MutableStateFlow(LocalDateTime.now())
    val currentDateTime: StateFlow<LocalDateTime> = _currentDateTime.asStateFlow()

    val alarmList: StateFlow<List<AlarmInfo>> = alarmRepository
        .getAllItemOrderByTimeAsc()
        .map { alarmList ->
            if (alarmList.isNullOrEmpty()) return@map listOf()
            alarmList.map { alarm -> alarm.toAlarmInfo() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000L),
            initialValue = listOf()
        )

    var isShowTitleInputDialog by mutableStateOf(false)
        private set

    // アラームリストでタップしたアラーム
    private var selectedAlarm: AlarmInfo? = null

    // 新たに追加したアラームのID
    private var idOfAddedAlarm by mutableStateOf<Long?>(INITIAL_ID)

    // 通知権限の許可取得ダイアログが一度表示されたかどうか
    var isShowedPermissionDialog: StateFlow<Boolean> =
        preferencesRepository.isShowedPermissionDialog
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000L),
                initialValue = true
            )

    /**
     * アラームを選択するときに呼び出すメソッド
     */
    fun selectAlarm(alarm: AlarmInfoInterface): HomeScreenViewModel {
        selectedAlarm = alarm as AlarmInfo
        return this
    }

    fun showTitleInputDialog() {
        isShowTitleInputDialog = true
    }

    fun hiddenTitleInputDialog(): HomeScreenViewModel {
        isShowTitleInputDialog = false
        return this
    }

    fun setAlarmName(name: String): HomeScreenViewModel {
        if (selectedAlarm != null) {
            viewModelScope.launch {
                alarmRepository.update(selectedAlarm!!.toAlarmInfoEntity().copy(name = name))
            }
        }
        return this
    }
    
    fun getSelectedAlarmName(): String {
        if (selectedAlarm != null) {
            selectedAlarm!!.name
        }
        return ""
    }

    /**
     * 選択したアラームを解除する
     */
    fun releaseSelectedAlarm() {
        selectedAlarm = null
    }

    fun delete(): HomeScreenViewModel {
        if (selectedAlarm != null) {
            viewModelScope.launch {
                alarmRepository.delete(selectedAlarm!!.id)
            }
        }
        return this
    }

    fun showPermissionDialog() {
        viewModelScope.launch {
            preferencesRepository.showedPermissionDialog()
        }
    }
    
    suspend fun updateCurrentDateTime() {
        while (true) {
            _currentDateTime.update { LocalDateTime.now() }
            delay(DELAY_TIME)
        }
    }

    // === 以下，追加したアラームへスクロールするための機能 ===
    /**
     * 新たに追加したアラームのID(DBのプライマリキー)を保存
     */
    fun setAddItemId(id: Long) {
        idOfAddedAlarm = id
    }

    fun initAddItemId() {
        idOfAddedAlarm = INITIAL_ID
    }

    /**
     * アラームを追加した後，アラームを削除する場合，idOfAddedAlarmが変更されてなかった場合，idOfAddedAlarmの位置へスクロールしてしまう
     * これを防ぐために，idOfAddedAlarmが初期値かどうかを判定して，初期値ならばスクロールしないようにする
     */
    fun isScroll(): Boolean {
        return idOfAddedAlarm != INITIAL_ID
    }

    /**
     * 新たに追加したアラームのリスト内でのIndexを返す
     */
    fun getAddedItemIndex(): Int {
        if (idOfAddedAlarm == INITIAL_ID) return 0
        val tmpList = alarmList.value
        for (i in tmpList.indices) {
            if (tmpList[i].id == idOfAddedAlarm) {
                return i
            }
        }
        return 0
    }
    // === 以上，追加したアラームへスクロールするための機能 ===
}

private fun AlarmInfoEntity.toAlarmInfo(): AlarmInfo {
    return AlarmInfo(
        id = this.id,
        hour = this.hour,
        min = this.min,
        name = this.name,
        zoneId = this.zoneId
    )
}

private fun AlarmInfo.toAlarmInfoEntity(): AlarmInfoEntity {
    return AlarmInfoEntity(
        id = this.id,
        hour = this.hour,
        min = this.min,
        name = this.name,
        zoneId = this.zoneId
    )
}