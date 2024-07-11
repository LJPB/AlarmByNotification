package me.ljpb.alarmbynotification.data

import kotlinx.coroutines.flow.Flow

interface NotificationRepositoryInterface {
    suspend fun insertNotification(notification: NotificationEntity)
    
    suspend fun updateNotification(notification: NotificationEntity)
    
    suspend fun deleteNotification(notification: NotificationEntity)
    
    fun getNotification(id: Int): Flow<NotificationEntity?>
    
    fun getAllNotifications():  Flow<List<NotificationEntity>>
}