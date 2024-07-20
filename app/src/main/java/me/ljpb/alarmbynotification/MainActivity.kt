package me.ljpb.alarmbynotification

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import me.ljpb.alarmbynotification.ui.HomeScreen
import me.ljpb.alarmbynotification.ui.HomeScreenViewModel
import me.ljpb.alarmbynotification.ui.TimePickerDialogViewModel
import me.ljpb.alarmbynotification.ui.ViewModelProvider
import me.ljpb.alarmbynotification.ui.theme.AlarmByNotificationTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlarmByNotificationTheme {
                val homeScreenViewModel: HomeScreenViewModel =
                    viewModel(factory = ViewModelProvider.Factory)
                val timePickerDialogViewModel: TimePickerDialogViewModel =
                    viewModel(factory = ViewModelProvider.Factory)
                
                if (Build.VERSION.SDK_INT >= 33) {
                    val notificationPermissionState = rememberPermissionState(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    )
                    if (notificationPermissionState.status.isGranted) {
                        createNotificationChannel(this)
                    }
                } else {
                    createNotificationChannel(this)
                }
                val appContainer = AppDataContainer(this)
                val windowSize = calculateWindowSizeClass(this)
                HomeScreen(
                    windowSize = windowSize,
                    homeScreenViewModel = homeScreenViewModel,
                    timePickerDialogViewModel = timePickerDialogViewModel
                )
            }
        }
    }
}