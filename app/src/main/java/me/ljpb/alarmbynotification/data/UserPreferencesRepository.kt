package me.ljpb.alarmbynotification.data

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val SHOW_PERMISSION_DIALOG = booleanPreferencesKey("move_setting_screen")
        val TIME_PICKER = booleanPreferencesKey("time_picker")
    }
    
    // [Android13以上] 通知権限の取得を促すダイアログを表示した
    suspend fun showedPermissionDialog() {
        dataStore.edit {
            it[SHOW_PERMISSION_DIALOG] = true
        }
    }
    
    val isShowedPermissionDialog: Flow<Boolean> = dataStore.data
        .catch {
            if(it is IOException) {
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {
            it[SHOW_PERMISSION_DIALOG] ?: false
        }

    // TimePickerDialogダイアログで最後に表示した時間選択コンポーネントの種類の選択
    suspend fun recentlyIsTimePicker(isTimePicker: Boolean) {
        dataStore.edit {
            it[TIME_PICKER] = isTimePicker
        }
    }
    
    // TimePickerDialogダイアログで最後に表示した時間選択コンポーネントはTimePicker?
    val isTimePicker: Flow<Boolean> = dataStore.data
        .catch {
            if(it is IOException) {
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {
            it[TIME_PICKER] ?: true
        }

}