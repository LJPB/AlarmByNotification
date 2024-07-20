package me.ljpb.alarmbynotification.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import me.ljpb.alarmbynotification.NotificationApplication

object ViewModelProvider {
    val Factory = viewModelFactory { 
        initializer { 
            val application = (this[APPLICATION_KEY] as NotificationApplication)
            val repository = application.container.notificationRepository
            TimePickerDialogViewModel(
                repository = repository,
                alarmRepository = application.container.alarmRepository
            )
        }
        initializer {
            val application = (this[APPLICATION_KEY] as NotificationApplication)
            val preferencesRepository = application.container.preferencesRepository
            HomeScreenViewModel(
                alarmRepository = application.container.alarmRepository,
                preferencesRepository = preferencesRepository,
            )
        }
    }
}
