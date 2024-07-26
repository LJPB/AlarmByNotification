package me.ljpb.alarmbynotification.data

import kotlinx.coroutines.flow.Flow
import me.ljpb.alarmbynotification.data.room.NotificationEntity

interface NotificationRepositoryInterface {
    suspend fun insertNotification(notification: NotificationEntity)

    suspend fun updateNotification(notification: NotificationEntity)

    suspend fun deleteNotification(notification: NotificationEntity)

    fun getNotification(notifyId: Int): Flow<NotificationEntity?>

    fun getAllNotifications(): Flow<List<NotificationEntity>?>

    fun count(): Flow<Int>
}