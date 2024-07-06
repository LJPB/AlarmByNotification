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


/**
 * アラームに設定した時間を表示するカード
 */
@Composable
fun AlarmCard(
    modifier: Modifier = Modifier,
    isAlarm: Boolean
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
            onTitleClick = {},
            onTimeClick = {},
            onDeleteClick = {},
            isAlarm = isAlarm
        )
    }
}

/**
 * @param onTitleClick アラームのタイトルを設定する
 * @param onTimeClick アラームの時刻を設定する
 * @param onDeleteClick アラームを削除する
 */
@Composable
private fun CardContent(
    modifier: Modifier = Modifier,
    onTitleClick: () -> Unit,
    onTimeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isAlarm: Boolean
) {
    Column(
        modifier = modifier
    ) {
        // アラームのタイトル
       Row(
           modifier = Modifier.clickable { onTitleClick() },
           verticalAlignment = Alignment.CenterVertically
       ) {
           Icon(
               imageVector = if (isAlarm) ALARM_ICON else TIMER_ICON,
               contentDescription = stringResource(if (isAlarm) R.string.add_timer else R.string.add_alarm)
           )
           Spacer(modifier = Modifier.width(8.dp))
           Text(
                text = stringResource(id = R.string.untitled),
                style = MaterialTheme.typography.bodyMedium,
           )
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // アラームの設定時間
            Text(
                text = "16:00",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.clickable { onTimeClick() }
            )
            // アラーム削除ボタン
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.align(Alignment.Bottom)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(id = R.string.delete_alarm)
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun AlarmCardPreview() {
    AlarmCard(
        modifier = Modifier.padding(16.dp),
        isAlarm = true
    )
}