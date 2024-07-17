package me.ljpb.alarmbynotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * 端末が再起動した時に通知を再セットするためのクラス
 */
class BootCompletedReceiver : BroadcastReceiver() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        if (intent.action == Intent.ACTION_BOOT_COMPLETED){

            val application = context.applicationContext as NotificationApplication
            val repository = application.container.notificationRepository
            
            val pendingResult = goAsync()
            GlobalScope.launch {
                try {
                    repository
                        .getAllNotifications()
                        .firstOrNull()
                        ?.forEach { 
                            val currentTime = System.currentTimeMillis()
                            // 過ぎていたら
                            if (it.triggerTimeMilliSeconds > currentTime) {
                                deleteNotification(context, notificationInfo = it)
                            } else {
                                setNotification(context = context, notificationInfo = it)
                            }
                        }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}