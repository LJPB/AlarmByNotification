package me.ljpb.alarmbynotification.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import me.ljpb.alarmbynotification.NotificationApplication

object ViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = (this[APPLICATION_KEY] as NotificationApplication)
            val notificationRepository = application.container.notificationRepository
            val alarmRepository = application.container.alarmRepository
            val preferencesRepository = application.container.preferencesRepository
            TimePickerDialogViewModel(
                notificationRepository = notificationRepository,
                alarmRepository = alarmRepository,
                preferencesRepository = preferencesRepository
            )
        }
        initializer {
            val application = (this[APPLICATION_KEY] as NotificationApplication)
            val notificationRepository = application.container.notificationRepository
            val alarmRepository = application.container.alarmRepository
            val preferencesRepository = application.container.preferencesRepository
            HomeScreenViewModel(
                notificationRepository = notificationRepository,
                alarmRepository = alarmRepository,
                preferencesRepository = preferencesRepository,
            )
        }
    }
}
