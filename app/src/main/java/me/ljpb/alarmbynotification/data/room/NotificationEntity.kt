package me.ljpb.alarmbynotification.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import me.ljpb.alarmbynotification.data.NotificationInfoInterface

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = false) override val notifyId: Int,
    override val alarmId: Long,
    override val triggerTimeMilliSeconds: Long,
    override val notifyName: String,
    override val zoneId: String
) : NotificationInfoInterface