package me.ljpb.alarmbynotification.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import me.ljpb.alarmbynotification.deleteNotification
import me.ljpb.alarmbynotification.setNotification

class NotificationRepository(private val context: Context, private val dao: NotificationDao) : NotificationRepositoryInterface {
    override suspend fun insertNotification(notification: NotificationEntity) {
        dao.insert(notification)
        moveDatabase()
        setNotification(context, notification)
    }

    override suspend fun updateNotification(notification: NotificationEntity) {
        dao.update(notification)
        moveDatabase()
        setNotification(context, notification)
    }

    override suspend fun deleteNotification(notification: NotificationEntity) {
        dao.delete(notification.notifyId)
        moveDatabase()
        deleteNotification(context, notification)
    }

    override fun getNotification(notifyId: Int): Flow<NotificationEntity?> {
        return dao.getItem(notifyId)
    }

    override fun getAllNotifications(): Flow<List<NotificationEntity>?> {
        return dao.getAllItem()
    }

    override fun count(): Flow<Int> {
        return dao.count()
    }
    
    private fun moveDatabase() {
//        context.moveDatabaseFrom(
//            context,
//            context.getDatabasePath("notification_database").toString()
//        )
    }
}