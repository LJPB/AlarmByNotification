package me.ljpb.alarmbynotification.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.ljpb.alarmbynotification.data.room.NotificationEntity
import me.ljpb.alarmbynotification.deleteNotification
import me.ljpb.alarmbynotification.setNotification

class NotificationRepository(
    private val context: Context, 
    private val dataStore: DataStore<Preferences>,
) : NotificationRepositoryInterface {

    private val notifications: Flow<List<NotificationEntity>> = dataStore.data
        .catch { 
            if (it is IOException) {
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preference -> // 返り値はFlowに包まれる
            preference.asMap() // preferenceをMapに変換
                .map { 
                    // 変換したMapの個々のキーと値のペアからNotificationEntityを作る
                    // 最終的にはそれらのListが内側の.mapの返り値となり，それが外側の.mapの返り値となるから，全体としてFlow<List<..>>を得る
                    toNotificationEntity(it.value.toString())
                }
                .filterNotNull()
        }
    
    override suspend fun insertNotification(notification: NotificationEntity) {
        dataStore.edit { 
            it[notification.preferenceKey()] = notification.toJson()
        }
        setNotification(context, notification)
    }

    override suspend fun updateNotification(notification: NotificationEntity) {
        dataStore.edit {
            it[notification.preferenceKey()] = notification.toJson()
        }
        setNotification(context, notification)
    }

    override suspend fun deleteNotification(notification: NotificationEntity) {
        dataStore.edit {
            it.remove(notification.preferenceKey())
        }
        deleteNotification(context, notification)
    }

    override fun getAllNotifications(): Flow<List<NotificationEntity>> {
        return notifications
    }
}

private fun NotificationEntity.preferenceKey(): Preferences.Key<String> {
    return stringPreferencesKey(this.alarmId.toString())
}

private fun NotificationEntity.toJson(): String {
    return Json.encodeToString(this)
}

private fun toNotificationEntity(json: String): NotificationEntity? {
    val notificationEntity = 
        try {
            Json.decodeFromString<NotificationEntity>(json)
        } catch (error: IllegalArgumentException) {
            null
        } catch (decodeError: SerializationException) {
            null
        }
    return notificationEntity
}