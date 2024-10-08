package me.ljpb.alarmbynotification.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.ljpb.alarmbynotification.NotificationApplication
import me.ljpb.alarmbynotification.Utility.getFormattedTime
import me.ljpb.alarmbynotification.alarmNotify
import me.ljpb.alarmbynotification.data.NotifyIntentKey
import me.ljpb.alarmbynotification.data.room.NotificationEntity
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class NotifyReceiver : BroadcastReceiver() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val notifyId = intent.getIntExtra(NotifyIntentKey.NOTIFY_ID, 0)
            val alarmId = intent.getLongExtra(NotifyIntentKey.ALARM_ID, 0)
            val notifyName = intent.getStringExtra(NotifyIntentKey.NOTIFY_NAME) ?: ""
            val triggerTime = intent.getLongExtra(NotifyIntentKey.TRIGGER_TIME_MILLI, 1)
            val zoneId = intent.getStringExtra(NotifyIntentKey.ZONE_ID) ?: ""

            val instant = Instant.ofEpochMilli(triggerTime)
            val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of(zoneId))
            val formattedTime = getFormattedTime(
                zonedDateTime.hour, zonedDateTime.minute, DateFormat.is24HourFormat(context)
            )

            alarmNotify(
                context = context,
                title = notifyName,
                text = formattedTime.toString(),
                notifyId = notifyId
            )

            // 鳴らした通知は削除する
            val application = context.applicationContext as NotificationApplication
            val repository = application.container.notificationRepository
            val pendingResult = goAsync()
            GlobalScope.launch {
                try {
                    repository.deleteNotification(
                        NotificationEntity(
                            alarmId = alarmId,
                            notifyId = notifyId,
                            triggerTimeMilliSeconds = triggerTime,
                            notifyName = notifyName,
                            zoneId = zoneId
                        )
                    )
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}