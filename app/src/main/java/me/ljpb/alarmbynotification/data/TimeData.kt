package me.ljpb.alarmbynotification.data

import java.time.ZonedDateTime

/** セットしたタイマーやアラームを表示するためのデータを表すクラス
 * @param id 一意のID
 * @param name タイマーやアラームの名称
 * @param type タイマー/アラームの種類
 * @param finishDateTime 通知が届く予定の日時
 */
data class TimeData(
    val id: Int,
    val name: String,
    val type: TimeType,
    var finishDateTime: ZonedDateTime,
)

enum class TimeType {
    Alarm,
    Timer
}