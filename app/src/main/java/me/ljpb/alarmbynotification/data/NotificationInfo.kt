package me.ljpb.alarmbynotification.data

/**
 * 通知に関する情報
 * @property title 通知に表示するタイトル
 * @property text 通知に表示する本文
 * @property notifyId 通知を区別するID
 * @property triggerTimeMilliSeconds 通知を実行するエポック秒
 * @property type 通知の種類(アラームかタイマー)
 */
interface NotificationInfoInterface {
    val title: String
    val text: String
    val notifyId: Int
    val triggerTimeMilliSeconds: Long
    val type: TimeType
    val zoneId: String
}

object NotifyIntentKey {
    const val TITLE = "title"
    const val TEXT = "text"
    const val NOTIFY_ID = "notifyId"
    const val TRIGGER_TIME = "triggerTime"
    const val TYPE = "type"
    const val ZONE_ID = "zoneId"
}