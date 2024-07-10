package me.ljpb.alarmbynotification.data

import java.time.LocalDateTime

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
    var finishDateTime: LocalDateTime,
)

enum class TimeType {
    Alarm,
    Timer
}