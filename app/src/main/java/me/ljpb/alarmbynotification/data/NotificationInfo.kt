package me.ljpb.alarmbynotification.data

/**
 * 通知に関する情報
 * @param title 通知に表示するタイトル
 * @param text 通知に表示する本文
 * @param notifyId 通知を区別するID
 * @param triggerTime 通知を実行するエポック秒
 * @param type 通知の種類(アラームかタイマー)
 */
data class NotificationInfo (
    val title: String,
    val text: String,
    val notifyId: Int,
    val triggerTime: Long,
    val type: NotifyType
)

object NotifyIntentKey {
    val title = "title"
    val text = "text"
    val notifyId = "notifyId"
    val type = "type"
}

enum class NotifyType {
    Alarm,
    Timer
}