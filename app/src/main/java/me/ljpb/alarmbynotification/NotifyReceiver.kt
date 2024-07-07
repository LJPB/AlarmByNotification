package me.ljpb.alarmbynotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.ljpb.alarmbynotification.data.NotifyIntentKey
import me.ljpb.alarmbynotification.data.NotifyType

class NotifyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val title = intent.getStringExtra(NotifyIntentKey.title) ?: ""
            val text = intent.getStringExtra(NotifyIntentKey.text) ?: ""
            val notifyId = intent.getIntExtra(NotifyIntentKey.notifyId, 1)
            
            val type = intent.getStringExtra(NotifyIntentKey.type) ?: ""
            
            when (type) {
                NotifyType.Alarm.name -> alarmNotify(
                    context = context,
                    title = title,
                    text = text,
                    notifyId = notifyId
                )
                NotifyType.Timer.name -> timerNotify(
                    context = context,
                    title = title,
                    text = text,
                    notifyId = notifyId
                )
            }
        }
    }
}