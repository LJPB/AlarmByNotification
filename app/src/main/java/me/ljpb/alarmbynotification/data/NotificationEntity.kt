package me.ljpb.alarmbynotification.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    override val notifyId: Int,
    override val title: String,
    override val text: String,
    override val triggerTime: Long,
    override val type: TimeType
) : NotificationInfoInterface

class TypeConverter {
    @TypeConverter
    fun timeTypeToString(timeType: TimeType): String = timeType.name
    
    @TypeConverter
    fun stringToTimeType(timeType: String): TimeType = enumValueOf<TimeType>(timeType)
}