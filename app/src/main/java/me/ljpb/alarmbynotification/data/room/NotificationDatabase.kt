package me.ljpb.alarmbynotification.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [NotificationEntity::class], version = 1, exportSchema = false)
@TypeConverters(NotificationEntityTypeConverter::class)
abstract class NotificationDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var Instance: NotificationDatabase? = null
        fun getDatabase(context: Context): NotificationDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    NotificationDatabase::class.java,
                    "notification_database"
                )
                    .build()
                    .also { Instance = it }
            }
        }
    }
}