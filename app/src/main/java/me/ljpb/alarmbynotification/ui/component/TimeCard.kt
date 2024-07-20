package me.ljpb.alarmbynotification.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.ljpb.alarmbynotification.R
import me.ljpb.alarmbynotification.Utility.getFormattedTime
import me.ljpb.alarmbynotification.data.AlarmInfo
import me.ljpb.alarmbynotification.data.room.AlarmInfoInterface

/**
 * @param onTitleClick アラームのタイトルをタップした時のイベント
 * @param onDeleteClick アラームを削除するイベント
 * @param is24Hour 24時間表記?
 */
@Composable
fun AlarmCard(
    modifier: Modifier = Modifier,
    onTitleClick: () -> Unit,
    onDeleteClick: () -> Unit,
    alarm: AlarmInfoInterface,
    is24Hour: Boolean,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        CardContent(
            modifier = Modifier
                .padding(
                    vertical = dimensionResource(id = R.dimen.padding_small),
                    horizontal = dimensionResource(id = R.dimen.padding_medium)
                )
                .fillMaxWidth(),
            onTitleClick = onTitleClick,
            onDeleteClick = onDeleteClick,
            alarm = alarm,
            is24Hour = is24Hour
        )
    }
}

@Composable
private fun CardContent(
    modifier: Modifier = Modifier,
    onTitleClick: () -> Unit,
    onDeleteClick: () -> Unit,
    alarm: AlarmInfoInterface,
    is24Hour: Boolean,
) {
    val icon = ALARM_ICON
    val iconDescription = stringResource(id = R.string.alarm_channel_name)
    val deleteDescription = stringResource(id = R.string.delete_alarm)

    val title =
        if (alarm.name.trim().isNotEmpty()) alarm.name else stringResource(id = R.string.untitled)

    Column(
        modifier = modifier
    ) {
        // アラームカードのタイトル
        Row(
            modifier = Modifier.clickable { onTitleClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = iconDescription
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AlarmTime(
                hour = alarm.hour,
                min = alarm.min,
                is24Hour = is24Hour,
                zoneId = alarm.zoneId
            )
            // アラーム削除ボタン
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.align(Alignment.Bottom)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = deleteDescription
                )
            }
        }
    }
}

@Composable
private fun AlarmTime(
    hour: Int,
    min: Int,
    zoneId: String,
    is24Hour: Boolean,
    modifier: Modifier = Modifier,
) {
    val formattedTime = getFormattedTime(hour, min, is24Hour)
    Text(
        text = formattedTime,
        style = MaterialTheme.typography.displayMedium,
    )
}


@Preview(showSystemUi = true)
@Composable
private fun AlarmCardPreview() {
    AlarmCard(
        onTitleClick = { /*TODO*/ },
        onDeleteClick = { /*TODO*/ },
        is24Hour = true,
        alarm = AlarmInfo(0, 0, 0, "", "")
    )
}