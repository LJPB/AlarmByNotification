package me.ljpb.alarmbynotification.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.ljpb.alarmbynotification.R
import me.ljpb.alarmbynotification.Utility.getFormattedTime
import me.ljpb.alarmbynotification.Utility.getZoneId
import me.ljpb.alarmbynotification.data.AlarmInfo
import me.ljpb.alarmbynotification.data.room.AlarmInfoInterface

/**
 * @param onTitleClick アラームのタイトルをタップした時のイベント
 * @param onDeleteClick アラームを削除するイベント
 * @param onEnableChange アラームを有効/無効化する時のイベント
 * @param is24Hour 24時間表記?
 * @param expanded このカードを拡大するかどうか
 * @param enable アラームの有効/無効ステータス
 */
@Composable
fun AlarmCard(
    modifier: Modifier = Modifier,
    onTitleClick: () -> Unit,
    onTimeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEnableChange: (Boolean) -> Unit,
    onExpandedChange: () -> Unit,
    alarm: AlarmInfoInterface,
    is24Hour: Boolean,
    expanded: Boolean = false,
    enable: Boolean
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onExpandedChange() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        CardContent(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_medium))
                .fillMaxWidth(),
            onTitleClick = onTitleClick,
            onDeleteClick = onDeleteClick,
            alarm = alarm,
            is24Hour = is24Hour,
            onEnableChange = onEnableChange,
            onTimeClick = onTimeClick,
            enable = enable,
            expanded = expanded,
            onExpandedChange = { onExpandedChange() }
        )
    }
}

@Composable
private fun CardContent(
    modifier: Modifier = Modifier,
    onTitleClick: () -> Unit,
    onTimeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEnableChange: (Boolean) -> Unit,
    onExpandedChange: () -> Unit,
    alarm: AlarmInfoInterface,
    is24Hour: Boolean,
    expanded: Boolean,
    enable: Boolean
) {
    val title =
        if (alarm.name.trim().isNotEmpty()) alarm.name else stringResource(id = R.string.untitled)
    val expandIcon: ImageVector
    val expandDescription: String
    if (expanded) {
        // expanded == true，カードは開いているから「閉じる」を表すup
        expandIcon = Icons.Default.KeyboardArrowUp
        expandDescription = stringResource(id = R.string.to_close_card)
    } else {
        // expanded == falseの時は，カードは閉じているから「開く」を表すdown
        expandIcon = Icons.Default.KeyboardArrowDown
        expandDescription = stringResource(id = R.string.to_open_card)
    }
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // アラームカードのタイトル
            Box(modifier = Modifier
                .clickable { onTitleClick() }
                .defaultMinSize(minWidth = 24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = expandIcon,
                contentDescription = expandDescription,
                modifier = Modifier.clickable { onExpandedChange() }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // アラームの時間
            AlarmTime(
                hour = alarm.hour,
                min = alarm.min,
                is24Hour = is24Hour,
                zoneId = alarm.zoneId,
                enable = enable,
                modifier = Modifier.clickable { onTimeClick() }
            )
            // アラームの有効/無効化スイッチ
            Switch(
                checked = enable,
                onCheckedChange = onEnableChange
            )
        }

        if (expanded) {
            Column {
                // タイムゾーン
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = stringResource(id = R.string.time_zone),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = alarm.zoneId,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                // 削除ボタン
                Row(
                    modifier = Modifier
                        .clickable { onDeleteClick() },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(id = R.string.delete_alarm),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
    enable: Boolean,
    modifier: Modifier = Modifier,
) {
    val formattedTime = getFormattedTime(hour, min, is24Hour)
    val color: Color
    val weight: FontWeight
    if (enable) {
        color = MaterialTheme.colorScheme.onSurface
        weight = FontWeight.Medium
    } else {
        color = MaterialTheme.colorScheme.outline
        weight = FontWeight.Normal
    }
    val currentZoneId = getZoneId()
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 時間
        Text(
            text = formattedTime.time,
            style = MaterialTheme.typography.displayMedium,
            color = color,
            fontWeight = weight
        )
        // 午前午後
        if (formattedTime.period != "") {
            Text(
                text = formattedTime.period,
                style = MaterialTheme.typography.headlineSmall,
                color = color,
            )
        }
        
        if (currentZoneId != zoneId) {
            Text(
                text = " ($zoneId)",
                style = MaterialTheme.typography.labelMedium,
                color = color,
            )
        }
    }
}


@Preview(showSystemUi = true)
@Composable
private fun AlarmCardEnablePreview() {
    AlarmCard(
        onTitleClick = {},
        onDeleteClick = { },
        onEnableChange = {},
        enable = false,
        alarm = AlarmInfo(0, 0, 0, "", ""),
        modifier = Modifier.padding(
            vertical = dimensionResource(id = R.dimen.padding_small),
            horizontal = dimensionResource(id = R.dimen.padding_medium)
        ),
        is24Hour = true,
        expanded = true,
        onExpandedChange = {},
        onTimeClick = {}
    )
}

@Preview(showSystemUi = true)
@Composable
private fun AlarmCardDisablePreview() {
//    AlarmCard(
//        onTitleClick = { /*TODO*/ },
//        onDeleteClick = { /*TODO*/ },
//        is24Hour = true,
//        enable = false,
//        onEnableChange = {},
//        alarm = AlarmInfo(0, 0, 0, "", "")
//    )
}