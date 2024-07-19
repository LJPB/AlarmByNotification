package me.ljpb.alarmbynotification.data

import java.time.ZonedDateTime

/** セットしたタイマーやアラームを表示するためのデータを表すクラス
 * @param id 一意のID
 * @param title アラームの名称
 * @param finishDateTime 通知が届く予定の日時
 */
data class TimeData(
    val id: Int,
    val title: String,
    var finishDateTime: ZonedDateTime,
)