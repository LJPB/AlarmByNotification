package me.ljpb.alarmbynotification.data.room

import kotlinx.serialization.Serializable

@Serializable
data class NotificationEntity(
    val notifyId: Int,
    val alarmId: Long,
    val triggerTimeMilliSeconds: Long,
    val notifyName: String,
    val zoneId: String
)

object NotifyIntentKey {
    const val NOTIFY_ID = "notifyId"
    const val ALARM_ID = "alarmId"
    const val TRIGGER_TIME_MILLI = "triggerTime"
    const val NOTIFY_NAME = "notifyName"
    const val ZONE_ID = "zoneId"
}