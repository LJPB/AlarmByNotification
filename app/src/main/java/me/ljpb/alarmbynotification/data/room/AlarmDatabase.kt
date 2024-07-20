package me.ljpb.alarmbynotification.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AlarmInfoEntity::class], version = 1, exportSchema = false)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var Instance: AlarmDatabase? = null
        fun getDatabase(context: Context): AlarmDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    AlarmDatabase::class.java,
                    "alarm_database"
                )
                    .build()
                    .also { Instance = it }
            }
        }
    }
}