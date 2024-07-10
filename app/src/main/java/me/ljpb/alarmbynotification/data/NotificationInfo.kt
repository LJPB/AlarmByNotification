package me.ljpb.alarmbynotification.data

/**
 * 通知に関する情報
 * @param title 通知に表示するタイトル
 * @param text 通知に表示する本文
 * @param notifyId 通知を区別するID
 * @param triggerTime 通知を実行するエポック秒
 * @param type 通知の種類(アラームかタイマー)
 */
interface NotificationInfoInterface {
    val title: String
    val text: String
    val notifyId: Int
    val triggerTime: Long
    val type: TimeType
}

data class NotificationInfo (
    override val title: String,
    override val text: String,
    override val notifyId: Int,
    override val triggerTime: Long,
    override val type: TimeType
) : NotificationInfoInterface

object NotifyIntentKey {
    const val TITLE = "title"
    const val TEXT = "text"
    const val NOTIFY_ID = "notifyId"
    const val TYPE = "type"
}