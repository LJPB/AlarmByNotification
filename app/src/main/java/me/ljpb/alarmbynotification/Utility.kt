package me.ljpb.alarmbynotification

import java.time.LocalTime
import java.time.format.DateTimeFormatter

object Utility {
    fun localTimeToString(localTime: LocalTime, isFormat24: Boolean = false): String {
       val timeFormat = if (isFormat24) {
            DateTimeFormatter.ofPattern("HH:mm:ss")
        } else {
            DateTimeFormatter.ofPattern("KK:mm:ss a")
        }
        return localTime.format(timeFormat)
    }
}