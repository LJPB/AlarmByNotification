package me.ljpb.alarmbynotification.data

/** セットしたタイマーやアラームを表示するためのデータを表すクラス
 * @param id 一意のID
 * @param name タイマーやアラームの名称
 * @param type タイマー/アラームの種類
 * @param epochSeconds 通知が届く予定のエポック秒
 */
data class TimeData(
    val id: Int,
    val name: String,
    val type: TimeType,
    var epochSeconds: Long,
)

enum class TimeType {
    Alarm,
    Timer
}