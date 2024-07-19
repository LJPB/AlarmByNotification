package me.ljpb.alarmbynotification.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.ljpb.alarmbynotification.R
import me.ljpb.alarmbynotification.ui.TimePickerDialogViewModel

val ALARM_ICON = Icons.Default.Alarm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onPositiveClick: () -> Unit,
    timePickerDialogViewModel: TimePickerDialogViewModel,
) {

    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight(),
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            DialogContent(
                onDismissRequest = onDismissRequest,
                onPositiveClick = onPositiveClick,
                timePickerDialogViewModel = timePickerDialogViewModel
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
    timePickerDialogViewModel: TimePickerDialogViewModel,
) {
    Column(
        modifier = modifier
            .padding(top = 16.dp, bottom = 8.dp, start = 24.dp, end = 24.dp)
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.add_alarm),
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(20.dp))
        TimePicker(state = timePickerDialogViewModel.alarmState,)
        // ボタン
        Row {
            Spacer(modifier = Modifier.weight(1f))
            // キャンセルボタン
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
            // okボタン
            TextButton(
                onClick = {
                    onPositiveClick()
                    onDismissRequest()
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
//    TimePickerDialog(
//        onDismissRequest = {},
//        onPositiveClick = {},
//        onChangeDialog = {},
//        timePickerDialogViewModel = viewModel() as TimePickerDialogViewModel
//    )
}