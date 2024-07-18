package me.ljpb.alarmbynotification

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import me.ljpb.alarmbynotification.data.UserPreferencesRepository
import me.ljpb.alarmbynotification.data.room.NotificationDatabase
import me.ljpb.alarmbynotification.data.NotificationRepository
import me.ljpb.alarmbynotification.data.NotificationRepositoryInterface

private const val DIALOG_DEFAULT_CONTENT_PREFERENCE_NAME = "dialog_default_content"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = DIALOG_DEFAULT_CONTENT_PREFERENCE_NAME
)

interface AppContainer {
    val notificationRepository: NotificationRepositoryInterface
    val preferencesRepository: UserPreferencesRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val notificationRepository: NotificationRepositoryInterface by lazy {
        NotificationRepository(
            context = context,
            dao = NotificationDatabase.getDatabase(context).notificationDao()
        )
    }
    override val preferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context.dataStore)
    }
}