package me.ljpb.alarmbynotification.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.ljpb.alarmbynotification.NotificationApplication
import me.ljpb.alarmbynotification.Utility.resettingNotify

class LockedBootCompletedReceiver : BroadcastReceiver() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            val application = context.applicationContext as NotificationApplication
            val repository = application.container.notificationRepository

            val pendingResult = goAsync()
            GlobalScope.launch {
                try {
                    resettingNotify(context, repository)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}