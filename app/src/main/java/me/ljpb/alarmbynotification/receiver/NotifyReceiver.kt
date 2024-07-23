package me.ljpb.alarmbynotification.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.ljpb.alarmbynotification.NotificationApplication
import me.ljpb.alarmbynotification.alarmNotify
import me.ljpb.alarmbynotification.data.NotifyIntentKey
import me.ljpb.alarmbynotification.data.room.NotificationEntity

class NotifyReceiver : BroadcastReceiver() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val notifyId = intent.getIntExtra(NotifyIntentKey.NOTIFY_ID, 1)
            val triggerTime = intent.getLongExtra(NotifyIntentKey.TRIGGER_TIME_MILLI, 1)
            val zoneId = intent.getStringExtra(NotifyIntentKey.ZONE_ID) ?: ""
          
            alarmNotify(
                context = context,
                title = "title",
                text = "text",
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
                            alarmId = 1,
                            notifyId = notifyId,
                            triggerTimeMilliSeconds = triggerTime,
                            notifyName = "",
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