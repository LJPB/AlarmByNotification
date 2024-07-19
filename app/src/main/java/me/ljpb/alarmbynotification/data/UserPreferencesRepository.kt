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
    }
    
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

}