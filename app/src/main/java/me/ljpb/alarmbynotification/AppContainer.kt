package me.ljpb.alarmbynotification

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import me.ljpb.alarmbynotification.data.AlarmRepository
import me.ljpb.alarmbynotification.data.NotificationRepository
import me.ljpb.alarmbynotification.data.NotificationRepositoryInterface
import me.ljpb.alarmbynotification.data.UserPreferencesRepository
import me.ljpb.alarmbynotification.data.room.AlarmDatabase
import me.ljpb.alarmbynotification.data.room.NotificationDatabase

private const val DIALOG_DEFAULT_CONTENT_PREFERENCE_NAME = "dialog_default_content"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = DIALOG_DEFAULT_CONTENT_PREFERENCE_NAME
)

interface AppContainer {
    val notificationRepository: NotificationRepositoryInterface
    val preferencesRepository: UserPreferencesRepository
    val alarmRepository: AlarmRepository
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
    override val alarmRepository: AlarmRepository by lazy {
        AlarmRepository(
            dao = AlarmDatabase.getDatabase(context).alarmDao()
        )
    }
}