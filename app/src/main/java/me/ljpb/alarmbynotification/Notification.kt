package me.ljpb.alarmbynotification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import me.ljpb.alarmbynotification.data.NotificationInfoInterface
import me.ljpb.alarmbynotification.data.NotifyIntentKey


/**
 * 通知チャンネルの作成
 * @param context
 * @param channelId チャンネルID
 * @param channelName チャンネルの名前
 * @param importance チャンネルの重要度(NotificationManager.[])
 */
private fun createNotificationChannelHelper(
    context: Context,
    channelId: String,
    channelName: String,
    importance: Int,
) {
    // 通知チャンネル
    val mChannel = NotificationChannel(channelId, channelName, importance)
    // 通知チャンネルをシステムに登録する
    val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(mChannel)
}

/**
 * アラーム通知チャンネルとタイマー通知チャンネルの作成
 */
fun createNotificationChannel(context: Context) {
    // アラームの通知チャンネル
    createNotificationChannelHelper(
        context = context,
        channelId = context.getString(R.string.alarm_channel_id),
        channelName = context.getString(R.string.alarm_channel_name),
        importance = NotificationManager.IMPORTANCE_HIGH
    )
    // タイマーの通知チャンネル
    createNotificationChannelHelper(
        context = context,
        channelId = context.getString(R.string.timer_channel_id),
        channelName = context.getString(R.string.timer_channel_name),
        importance = NotificationManager.IMPORTANCE_HIGH
    )
}

/**
 * 通知の発行
 * @param context
 * @param channelId 通知チャンネルのID
 * @param title 通知に表示するタイトル
 * @param text 通知に表示する本文
 * @param icon 通知に表示するアイコン
 * @param notifyId 通知を区別するID
 */
private fun notify(
    context: Context,
    channelId: String,
    title: String,
    text: String,
    icon: Int,
    notifyId: Int
) {
    val builder = NotificationCompat.Builder(context, channelId)
        .setContentTitle(title)
        .setContentText(text)
        .setSmallIcon(icon)

    with(NotificationManagerCompat.from(context)) {
        // 通知権限のチェック
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO 
            return@with
        }
        // 通知
        notify(notifyId, builder.build())
    }
}

/**
 * アラーム通知の発行
 * @param context
 * @param title 通知に表示するタイトル
 * @param text 通知に表示する本文
 * @param notifyId 通知を区別するID
 */
fun alarmNotify(
    context: Context,
    title: String,
    text: String,
    notifyId: Int
) {
    notify(
        context = context,
        channelId = context.getString(R.string.alarm_channel_id),
        title = title,
        text = text,
        icon = R.drawable.alarm_notification_icon,
        notifyId = notifyId
    )
}

/**
 * タイマー通知の発行
 * @param context
 * @param title 通知に表示するタイトル
 * @param text 通知に表示する本文
 * @param notifyId 通知を区別するID
 */
fun timerNotify(
    context: Context,
    title: String,
    text: String,
    notifyId: Int
) {
    notify(
        context = context,
        channelId = context.getString(R.string.timer_channel_id),
        title = title,
        text = text,
        icon = R.drawable.timer_notification_icon,
        notifyId = notifyId
    )
}

/**
 * 通知のセット
 * @param context
 * @param notificationInfo 登録する通知の情報
 */
fun setNotification(context: Context, notificationInfo: NotificationInfoInterface) {
    val intent = Intent(context, NotifyReceiver::class.java)
    intent.putExtra(NotifyIntentKey.TITLE, notificationInfo.title)
    intent.putExtra(NotifyIntentKey.TEXT, notificationInfo.text)
    intent.putExtra(NotifyIntentKey.NOTIFY_ID, notificationInfo.notifyId)
    intent.putExtra(NotifyIntentKey.TYPE, notificationInfo.type.name)
    intent.putExtra(NotifyIntentKey.TRIGGER_TIME, notificationInfo.triggerTimeMilliSeconds)
    
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        notificationInfo.notifyId, // requestCode
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    
    val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
    val alarmClockInfo = AlarmManager.AlarmClockInfo(notificationInfo.triggerTimeMilliSeconds, pendingIntent)
    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
}

/**
 * 通知の削除
 * @param context
 * @param notificationInfo 削除対象となる通知(notifyIdが一致する通知を削除)
 */
fun deleteNotification(context: Context, notificationInfo: NotificationInfoInterface) {
    val intent = Intent(context, NotifyReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        notificationInfo.notifyId, // requestCode
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
    pendingIntent.cancel()
    alarmManager.cancel(pendingIntent)
}
