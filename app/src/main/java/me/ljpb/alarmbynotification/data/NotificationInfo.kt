package me.ljpb.alarmbynotification.data

/**
 * 通知に関する情報
 * @param title 通知に表示するタイトル
 * @param text 通知に表示する本文
 * @param notifyId 通知を区別するID
 * @param triggerTime 通知を実行するエポック秒
 */
data class NotificationInfo (
    val title: String,
    val text: String,
    val notifyId: Int,
    val triggerTime: Long
)

object NotifyIntentKey {
    val title = "title"
    val text = "text"
    val notifyId = "notifyId"
}