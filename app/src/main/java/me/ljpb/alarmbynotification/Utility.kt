package me.ljpb.alarmbynotification

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
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
            val ptn = if(displaySecond) "HH:mm:ss" else "HH:mm"
            DateTimeFormatter.ofPattern(ptn)
        } else {
            val ptn = if(displaySecond) "KK:mm:ss a" else "KK:mm a"
            DateTimeFormatter.ofPattern(ptn)
        }
        return localDateTime.format(timeFormat)
    }

    /** 与えられたエポック秒の時刻をフォーマットした文字列に変換 (例 : epochSeconds → 16:00, 04:00 p.m.)
     * @param epochSeconds
     * @param isFormat24 24時間表記かどうか
     * @param timeZoneId
     */
    fun epochSecondsToFormattedTimeOfDay(
        epochSeconds: Long,
        isFormat24: Boolean = false,
        timeZoneId: String
    ): String {
        val instant = Instant.ofEpochSecond(epochSeconds)
        val zoneId = ZoneId.of(timeZoneId)
        val time = instant.atZone(zoneId).toLocalDateTime()
        return localDateTimeToFormattedTime(time, isFormat24, false)
    }
}