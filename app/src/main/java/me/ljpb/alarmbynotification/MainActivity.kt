package me.ljpb.alarmbynotification

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import me.ljpb.alarmbynotification.ui.HomeScreen
import me.ljpb.alarmbynotification.ui.HomeScreenViewModel
import me.ljpb.alarmbynotification.ui.TimePickerDialogViewModel
import me.ljpb.alarmbynotification.ui.ViewModelProvider
import me.ljpb.alarmbynotification.ui.theme.AlarmByNotificationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)
        enableEdgeToEdge()
        setContent {
            AlarmByNotificationTheme {
                val homeScreenViewModel: HomeScreenViewModel = viewModel(factory = ViewModelProvider.Factory)
                val timePickerDialogViewModel: TimePickerDialogViewModel = viewModel(factory = ViewModelProvider.Factory)
                HomeScreen(
                    homeScreenViewMode = homeScreenViewModel,
                    timePickerDialogViewModel = timePickerDialogViewModel
                )
            }
        }
    }
}