package me.ljpb.alarmbynotification.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @param id PendingIntentのRequestCodeと一致
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val title: String,
    val text: String,
    val triggerTime: Long,
    val type: String
)
