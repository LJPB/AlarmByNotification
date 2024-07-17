package me.ljpb.alarmbynotification.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.ljpb.alarmbynotification.NotificationApplication
import me.ljpb.alarmbynotification.alarmNotify
import me.ljpb.alarmbynotification.data.NotificationEntity
import me.ljpb.alarmbynotification.data.NotifyIntentKey
import me.ljpb.alarmbynotification.data.TimeType
import me.ljpb.alarmbynotification.timerNotify

class NotifyReceiver : BroadcastReceiver() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val title = intent.getStringExtra(NotifyIntentKey.TITLE) ?: ""
            val text = intent.getStringExtra(NotifyIntentKey.TEXT) ?: ""
            val notifyId = intent.getIntExtra(NotifyIntentKey.NOTIFY_ID, 1)
            val triggerTime = intent.getLongExtra(NotifyIntentKey.TRIGGER_TIME, 1)
            val type = intent.getStringExtra(NotifyIntentKey.TYPE) ?: ""
            when (type) {
                TimeType.Alarm.name -> alarmNotify(
                    context = context,
                    title = title,
                    text = text,
                    notifyId = notifyId
                )

                TimeType.Timer.name -> timerNotify(
                    context = context,
                    title = title,
                    text = text,
                    notifyId = notifyId
                )
            }
            val application = context.applicationContext as NotificationApplication
            val repository = application.container.notificationRepository
            val pendingResult = goAsync()
            GlobalScope.launch {
                try {
                    repository.deleteNotification(
                        NotificationEntity(
                            notifyId = notifyId,
                            title = title,
                            text = text,
                            triggerTimeMilliSeconds = triggerTime,
                            type = type.toTimeType(),
                        )
                    )
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
    
    private fun String.toTimeType(): TimeType = if (this == TimeType.Alarm.name) TimeType.Alarm else TimeType.Timer
    
}