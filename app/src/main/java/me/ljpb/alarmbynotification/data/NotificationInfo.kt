package me.ljpb.alarmbynotification.data

/**
 * 通知に関する情報
 * @property notifyId 通知を区別するID
 * @property alarmId 対応するアラームのID
 * @property triggerTimeMilliSeconds 通知を実行するエポックミリ秒
 */
interface NotificationInfoInterface {
    val notifyId: Int
    val alarmId: Long
    val triggerTimeMilliSeconds: Long
    val notifyName: String
    val zoneId: String
}

object NotifyIntentKey {
    const val NOTIFY_ID = "notifyId"
    const val ALARM_ID = "alarmId"
    const val TRIGGER_TIME_MILLI = "triggerTime"
    const val NOTIFY_NAME = "notifyName"
    const val ZONE_ID = "zoneId"
}