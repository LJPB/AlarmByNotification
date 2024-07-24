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

private const val DIALOG_DEFAULT_CONTENT_PREFERENCE_NAME = "dialog_default_content"
private const val NOTIFICATION_PREFERENCE_NAME = "notifications"
private val Context.dialogDataStore: DataStore<Preferences> by preferencesDataStore(
    name = DIALOG_DEFAULT_CONTENT_PREFERENCE_NAME
)
private val Context.notificationDatastore: DataStore<Preferences> by preferencesDataStore(
    name = NOTIFICATION_PREFERENCE_NAME
)

interface AppContainer {
    val notificationRepository: NotificationRepositoryInterface
    val preferencesRepository: UserPreferencesRepository
    val alarmRepository: AlarmRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val notificationRepository: NotificationRepositoryInterface by lazy {
        val directBootContext = context.createDeviceProtectedStorageContext()
        NotificationRepository(
            context = directBootContext,
            dataStore = directBootContext.notificationDatastore
        )
    }
    override val preferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context.dialogDataStore)
    }
    override val alarmRepository: AlarmRepository by lazy {
        AlarmRepository(
            dao = AlarmDatabase.getDatabase(context).alarmDao()
        )
    }
}