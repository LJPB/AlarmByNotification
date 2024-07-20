package me.ljpb.alarmbynotification

import me.ljpb.alarmbynotification.data.room.NotificationEntity
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.zone.ZoneRulesException

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
    
    fun getFormattedTime(hour: Int, min: Int, is24Hour: Boolean): String {
        val localTime = LocalTime.of(hour, min)
        val timeFormat = if (is24Hour) {
            DateTimeFormatter.ofPattern("HH:mm")
        } else {
            DateTimeFormatter.ofPattern("KK:mm a")
        }
        return localTime.format(timeFormat)
    }

    fun getZoneId(): String = try {
        ZoneId.systemDefault().id
    } catch (_: ZoneRulesException) {
        "UTC"
    } catch (_: DateTimeException) {
        "UTC"
    }
    
    val notificationEmptyEntity = NotificationEntity(
        notifyId = 0,
        title = "",
        text = "",
        triggerTimeMilliSeconds = 0L,
        zoneId = ""
    )
}
