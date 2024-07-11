package me.ljpb.alarmbynotification

import android.content.Context
import me.ljpb.alarmbynotification.data.NotificationDatabase
import me.ljpb.alarmbynotification.data.NotificationRepository
import me.ljpb.alarmbynotification.data.NotificationRepositoryInterface

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val notificationRepository: NotificationRepositoryInterface
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val notificationRepository: NotificationRepositoryInterface by lazy {
        NotificationRepository(
            context = context,
            dao = NotificationDatabase.getDatabase(context).notificationDao()
        )
    }
}