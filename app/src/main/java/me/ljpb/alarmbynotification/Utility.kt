package me.ljpb.alarmbynotification

import me.ljpb.alarmbynotification.data.room.NotificationEntity
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.zone.ZoneRulesException

/**
 * 環境に合わせてフォーマットした時間の文字列を管理
 * @param time 時間の部分(04:00 pmの04:00の部分)
 * @param period 午前/午後の部分(04:00 pmのpmの部分)
 */
data class FormattedTime(
    val time: String,
    val period: String
)

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

    // フォーマットした時間をFormattedTimeにして返す
    fun getFormattedTime(hour: Int, min: Int, is24Hour: Boolean): FormattedTime {
        val localTime = LocalTime.of(hour, min)
        val formattedTime = if (is24Hour) {
            FormattedTime(
                time = localTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                period = ""
            )
        } else {
            FormattedTime(
                time = localTime.format(DateTimeFormatter.ofPattern("KK:mm")),
                period = localTime.format(DateTimeFormatter.ofPattern(" a"))
            )
        }
        return formattedTime
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
