package me.ljpb.alarmbynotification

import me.ljpb.alarmbynotification.data.room.NotificationEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Utility {
    /** 与えられたLocalDateTimeの時刻をフォーマットした文字列に変換 (例 : LocalDateTime → 16:00, 04:00 p.m.)
     * @param localDateTime
     * @param isFormat24 24時間表記かどうか
     * @param displaySecond 秒を表示するかどうか
     */
    fun localDateTimeToFormattedTime(
        localDateTime: LocalDateTime,
        isFormat24: Boolean = false,
        displaySecond: Boolean = true
    ): String {
        val timeFormat = if (isFormat24) {
            val ptn = if (displaySecond) "HH:mm:ss" else "HH:mm"
            DateTimeFormatter.ofPattern(ptn)
        } else {
            val ptn = if (displaySecond) "KK:mm:ss a" else "KK:mm a"
            DateTimeFormatter.ofPattern(ptn)
        }
        return localDateTime.format(timeFormat)
    }

    val notificationEmptyEntity = NotificationEntity(
        notifyId = 0,
        title = "",
        text = "",
        triggerTimeMilliSeconds = 0L,
        zoneId = ""
    )

}