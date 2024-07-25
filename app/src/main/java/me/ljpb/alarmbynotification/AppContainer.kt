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

private const val USER_PREFERENCE = "user_preference"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCE
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
            context = context,
            dao = NotificationDatabase.getDatabase(directBootContext).notificationDao()
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