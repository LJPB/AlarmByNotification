package me.ljpb.alarmbynotification.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import me.ljpb.alarmbynotification.R
import me.ljpb.alarmbynotification.ui.TimePickerDialogViewModel

val ALARM_ICON = Icons.Default.Schedule
val TIMER_ICON = Icons.Default.HourglassTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onPositiveClick: () -> Unit,
    onChangeDialog: () -> Unit,
    timePickerDialogViewModel: TimePickerDialogViewModel,
    showChangeButton: Boolean = true
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            DialogContent(
                onDismissRequest = onDismissRequest,
                onPositiveClick = onPositiveClick,
                onChangeDialog = onChangeDialog,
                timePickerDialogViewModel = timePickerDialogViewModel,
                showChangeButton = showChangeButton
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogContent(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onPositiveClick: () -> Unit,
    onChangeDialog: () -> Unit,
    timePickerDialogViewModel: TimePickerDialogViewModel,
    showChangeButton: Boolean
) {
    val isAlarm = timePickerDialogViewModel.isAlarm
    Column(
        modifier = modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_medium))
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimensionResource(id = R.dimen.padding_medium)),
            text = stringResource(if (isAlarm) R.string.add_alarm else R.string.add_timer),
            style = MaterialTheme.typography.labelSmall
        )
        if (isAlarm) {
            // アラームセット
            TimePicker(
                state = timePickerDialogViewModel.alarmState,
            )
        } else {
            // タイマーセット
            TimeInput(
                state = timePickerDialogViewModel.timerState,
            )
        }
        // ボタン
        Row(
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showChangeButton) {
                // アラーム追加ダイアログとタイマー追加ダイアログの切り替えボタン
                // isAlarmがtrueつまり現在がアラームの時はタイマーのアイコンを表示する
                IconButton(onClick = onChangeDialog) {
                    Icon(
                        imageVector = if (isAlarm) TIMER_ICON else ALARM_ICON,
                        contentDescription = stringResource(if (isAlarm) R.string.add_timer else R.string.add_alarm)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            // キャンセルボタン
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
            // okボタン
            TextButton(
                onClick = { 
                    if (timePickerDialogViewModel.canAdd()) {
                        onPositiveClick()
                        onDismissRequest()
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    }
}

@Preview
@Composable
private fun TimePickerDialogPreview() {
    TimePickerDialog(
        onDismissRequest = {},
        onPositiveClick = {},
        onChangeDialog = {},
        timePickerDialogViewModel = viewModel() as TimePickerDialogViewModel
    )
}