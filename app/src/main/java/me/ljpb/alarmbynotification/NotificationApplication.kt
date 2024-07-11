package me.ljpb.alarmbynotification

import android.app.Application

class NotificationApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}