package me.ljpb.alarmbynotification.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.ljpb.alarmbynotification.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    modifier: Modifier = Modifier, 
    state: TimePickerState,
    onDismissRequest: () -> Unit,
    onPositiveClick: () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_medium)),
            ) {
                Text(
                    modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_medium)),
                    text = stringResource(id = R.string.select_time),
                    style = MaterialTheme.typography.labelSmall
                )

                TimePicker(
                    state = state
                )
                
                // ボタン
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = dimensionResource(id = R.dimen.padding_medium)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // キャンセルボタン
                    TextButton(
                        onClick = onDismissRequest
                    ) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                    
                    // okボタン
                    TextButton(
                        onClick = onPositiveClick
                    ) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun TimePickerDialogPreview() {
    TimePickerDialog(
        state = TimePickerState(initialHour = 11, initialMinute = 24, is24Hour = true),
        onDismissRequest = {},
        onPositiveClick = {}
    )
}