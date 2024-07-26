package me.ljpb.alarmbynotification

import android.content.Context
import kotlinx.coroutines.flow.firstOrNull
import me.ljpb.alarmbynotification.data.NotificationRepositoryInterface
import java.time.DateTimeException
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.zone.ZoneRulesException

/**
 * 環境に合わせてフォーマットした時間の文字列を管理
 * @param time 時間の部分(04:00 pmの04:00の部分)
 * @param period 午前/午後の部分(04:00 pmのpmの部分)
 */
class FormattedTime(val time: String, val period: String) {
    override fun toString(): String {
        return time + period
    }
}

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

    /**
     * 引数で渡された[triggerHour]時間[triggerMinutes]分が[currentTime]を基準にして何ミリ秒後かを返す。
     * 例えば，現在時刻が10:00 amで引数として09:00 amが渡されたら，翌日の09:00 amまでのミリ秒数を返す。
     * 現在の時刻と同じ場合は，翌日の時刻とみなす。
     */
    fun getMilliSecondsOfNextTime(
        triggerHour: Int,
        triggerMinutes: Int,
        currentTime: ZonedDateTime
    ): Long {
        val currentHour = currentTime.hour
        val currentMin = currentTime.minute
        val currentSec = currentTime.second
        val currentTimeAsMinutes = currentHour * 60 + currentMin
        val triggerTimeAsMinutes = triggerHour * 60 + triggerMinutes

        // 引数で渡した時間 - 現在時間
        val diff = if (currentTimeAsMinutes >= triggerTimeAsMinutes) {
            24 * 60 - (currentTimeAsMinutes - triggerTimeAsMinutes)
        } else {
            triggerTimeAsMinutes - currentTimeAsMinutes
        }

        val triggerDateTime = currentTime
            // 分単位で計算したいため，秒を0にしている
            // たとえば現在時刻が10時10分10秒で，triggerTimeが11時10分のとき，minusSecondsがなければ，triggerDateTimeは11時10分10秒になってしまう
            // この秒差をなくすために，currentTimeからcurrentTimeの秒数を引いている
            .minusSeconds(currentSec.toLong())
            .plusMinutes(diff.toLong())
            .toEpochSecond() * 1000 // ミリ秒化

        return triggerDateTime
    }

    fun getHowManyLater(triggerHour: Int, triggerMin: Int, currentTime: ZonedDateTime): Pair<Long, Long> {
        val triggerTimeMilliSeconds = getMilliSecondsOfNextTime(triggerHour, triggerMin, currentTime)
        val instant = Instant.ofEpochMilli(triggerTimeMilliSeconds)
        val triggerDateTime = ZonedDateTime.ofInstant(instant, currentTime.zone)
        val duration = Duration
            .between(currentTime.minusSeconds(currentTime.second.toLong()), triggerDateTime)
            .plusMinutes(1)
            .toMinutes()
        return Pair(duration / 60, duration % 60)
    }
    
    fun getZoneId(): String = try {
        ZoneId.systemDefault().id
    } catch (_: ZoneRulesException) {
        "UTC"
    } catch (_: DateTimeException) {
        "UTC"
    }

    suspend fun resettingNotify(context: Context, repository: NotificationRepositoryInterface) {
        repository
            .getAllNotifications()
            .firstOrNull()
            ?.forEach { notification ->
                // ========== 注意 ==========
                // triggerTimeMilliSecondsは秒単位の時間の1000倍でミリ秒を表現しているため，1秒未満は全て0となっている。
                val currentTime = System.currentTimeMillis()
                if (notification.triggerTimeMilliSeconds < currentTime) {
                    // 過ぎていたら
                    setNotification(context = context, notificationInfo = notification)
                    repository.deleteNotification(notification)
                } else {
                    setNotification(context = context, notificationInfo = notification)
                }
            }
    }

}
