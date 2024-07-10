package me.ljpb.alarmbynotification

import java.time.LocalDateTime
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
}