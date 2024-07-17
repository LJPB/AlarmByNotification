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
                app = application,
                repository = repository,
            )
        }
        initializer {
            val application = (this[APPLICATION_KEY] as NotificationApplication)
            val repository = application.container.notificationRepository
            val preferencesRepository = application.container.preferencesRepository
            HomeScreenViewModel(
                repository = repository,
                preferencesRepository = preferencesRepository
            )
        }
    }
}
