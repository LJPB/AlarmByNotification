package me.ljpb.alarmbynotification.ui.component

import android.annotation.SuppressLint
import android.text.format.DateFormat
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.ljpb.alarmbynotification.R
import me.ljpb.alarmbynotification.Utility.localDateTimeToFormattedTime
import me.ljpb.alarmbynotification.data.TimeData
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun AlarmCard(
    modifier: Modifier = Modifier,
    onTitleClick: () -> Unit,
    onDeleteClick: () -> Unit,
    timeData: TimeData
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
    ) {
        CardContent(
            modifier = Modifier
                .padding(
                    vertical = dimensionResource(id = R.dimen.padding_small),
                    horizontal = dimensionResource(id = R.dimen.padding_medium)
                )
                .fillMaxWidth(),
            onTitleClick = onTitleClick,
            onDeleteClick = onDeleteClick,
            currentTime = null,
            timeData = timeData,
            isAlarm = true
        )
    }
}

@Composable
fun TimerCard(
    modifier: Modifier = Modifier,
    onTitleClick: () -> Unit,
    onDeleteClick: () -> Unit,
    currentTime: LocalDateTime,
    timeData: TimeData
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
    ) {
        CardContent(
            modifier = Modifier
                .padding(
                    vertical = dimensionResource(id = R.dimen.padding_small),
                    horizontal = dimensionResource(id = R.dimen.padding_medium)
                )
                .fillMaxWidth(),
            onTitleClick = onTitleClick,
            onDeleteClick = onDeleteClick,
            currentTime = currentTime,
            timeData = timeData,
            isAlarm = false
        )
    }
}

/**
 * @param onTitleClick アラームのタイトルを設定する
 * @param onTimeClick アラームの時刻を設定する
 * @param onDeleteClick アラームを削除する
 * @param timeData カードに表示する時刻のデータ
 */
@Composable
private fun CardContent(
    modifier: Modifier = Modifier,
    onTitleClick: () -> Unit,
    onDeleteClick: () -> Unit,
    currentTime: LocalDateTime?,
    timeData: TimeData,
    isAlarm: Boolean
) {
    val icon: ImageVector
    val iconDescription: String
    val deleteDescription: String
    if (isAlarm) {
        icon = ALARM_ICON
        iconDescription = stringResource(id = R.string.alarm_channel_name)
        deleteDescription = stringResource(id = R.string.delete_alarm)
    } else {
        icon = TIMER_ICON
        iconDescription = stringResource(id = R.string.timer_channel_name)
        deleteDescription = stringResource(id = R.string.delete_timer)
    }
    val title = if (timeData.name.trim()
            .isNotEmpty()
    ) timeData.name else stringResource(id = R.string.untitled)

    Column(
        modifier = modifier
    ) {
        // アラームのタイトル
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
            if (isAlarm) AlarmTime(
                timeData = timeData.finishDateTime,
            ) else TimerTime(
                currentTime = currentTime!!,
                timeData = timeData.finishDateTime,
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
    timeData: LocalDateTime,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isFormat24 by remember { mutableStateOf(DateFormat.is24HourFormat(context)) }
    val formattedTime = localDateTimeToFormattedTime(timeData, isFormat24, false)
    Text(
        text = formattedTime,
        style = MaterialTheme.typography.displayLarge,
    )
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
private fun TimerTime(
    currentTime: LocalDateTime,
    timeData: LocalDateTime,
    modifier: Modifier = Modifier,
) {
    val text: String
    if (currentTime.isAfter(timeData)) {
        // 現在時刻がセットした時間を過ぎていたら
        text = "--:--"
    } else {
        // 終了時刻に00:00:00となるように1秒足している
        val difSeconds = Duration
            .between(currentTime, timeData)
            .toSeconds() + 1
        
        val hour = difSeconds / (60 * 60)
        val min = (difSeconds / 60) % 60
        val sec = difSeconds % 60
        
        // 時間:分:秒
        text = hour.toString().padStart(2, '0') + ":" +
                min.toString().padStart(2, '0') + ":" +
                sec.toString().padStart(2, '0')
    }
    Text(
        text = text,
        style = MaterialTheme.typography.displayLarge,
    )
}

@Preview(showSystemUi = true)
@Composable
private fun AlarmCardPreview() {
//    TimeCard(
//        modifier = Modifier.padding(16.dp),
//        currentTime = LocalDateTime.now(),
//        finishTime = TimeData(
//            id = 1,
//            finishDateTime = LocalDateTime.now(),
//            name = "aaa",
//            type = TimeType.Alarm,
//        ),
//        onTimeClick = {},
//        onTitleClick = {},
//        onDeleteClick = {}
//    )
}