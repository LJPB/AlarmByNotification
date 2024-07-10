package me.ljpb.alarmbynotification

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Utility {
    fun localDateTimeToFormattedTime(localDateTime: LocalDateTime, isFormat24: Boolean = false, displaySecond: Boolean = true): String {
        val timeFormat = if (isFormat24) {
            val ptn = if(displaySecond) "HH:mm:ss" else "HH:mm"
            DateTimeFormatter.ofPattern(ptn)
        } else {
            val ptn = if(displaySecond) "KK:mm:ss a" else "KK:mm a"
            DateTimeFormatter.ofPattern(ptn)
        }
        return localDateTime.format(timeFormat)
    }

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