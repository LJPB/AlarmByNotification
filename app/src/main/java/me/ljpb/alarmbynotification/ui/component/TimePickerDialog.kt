package me.ljpb.alarmbynotification.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Keyboard
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
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import me.ljpb.alarmbynotification.R
import me.ljpb.alarmbynotification.ui.component.ScreenType.CompactCompact
import me.ljpb.alarmbynotification.ui.component.ScreenType.CompactLandscape
import me.ljpb.alarmbynotification.ui.component.ScreenType.NormalLandscape
import me.ljpb.alarmbynotification.ui.component.ScreenType.NormalPortrait

val ALARM_ICON = Icons.Default.Alarm

/**
 * [CompactCompact] 縦横ともにCompactな小さい画面
 * [CompactLandscape] windowHeightSizeClass.Compactな横画面。一般的な大きさの端末の横画面を想定
 * [NormalPortrait] 一般的な大きさの端末の縦画面と，その他端末の縦画面を想定
 * [NormalLandscape] 一般的な大きさの端末を除く，Normalの横画面
 */
enum class ScreenType {
    CompactCompact, CompactLandscape, NormalPortrait, NormalLandscape,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onPositiveClick: (Boolean) -> Unit,
    recentlyIsTimePicker: Boolean,
    windowSizeClass: WindowSizeClass,
    timePickerState: TimePickerState,
) {
    // 表示しているダイアログはTimePicker? 初期値は，前回表示した種類と同じ(初回起動時はtrue)
    var currentIsPicker by rememberSaveable { mutableStateOf(recentlyIsTimePicker) }

    // 横画面?
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val screenType: ScreenType
    // ダイアログの大きさに関するフラグ
    val usePlatformDefaultWidth: Boolean
    val dialogModifier: Modifier

    when {
        windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact && windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact -> { // 縦横ともにCompact
            screenType = ScreenType.CompactCompact
            usePlatformDefaultWidth = false
            dialogModifier = modifier
                .wrapContentHeight()
                .wrapContentHeight()
                .padding(24.dp)
        }

        isLandscape && windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact -> { // 一般的な端末の横画面
            // && windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact である必要に注意
            screenType = ScreenType.CompactLandscape
            if (currentIsPicker) {
                // 横画面にした時のTimePickerが収まらないからサイズを調整する必要がある
                usePlatformDefaultWidth = false
                dialogModifier = modifier
                    .wrapContentWidth()
                    .fillMaxHeight() // 横画面にした時のTimePickerが収まらないから広げる必要がある
            } else {
                // TimeInputは収まるため，fillMaxHeightで無駄に広げないようにするために，分岐している
                usePlatformDefaultWidth = true
                dialogModifier = modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
            }
        }

        isLandscape -> {
            screenType = ScreenType.NormalLandscape
            usePlatformDefaultWidth = false
            dialogModifier = modifier
                .wrapContentWidth()
                .wrapContentHeight()
        }

        else -> {
            screenType = ScreenType.NormalPortrait
            usePlatformDefaultWidth = true
            dialogModifier = modifier
                .wrapContentWidth()
                .wrapContentHeight()
        }
    }
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = dialogModifier,
        properties = DialogProperties(
            usePlatformDefaultWidth = usePlatformDefaultWidth,
            dismissOnClickOutside = currentIsPicker  // TimeInputのときキーボードを閉じると同時に背景をタップしてダイアログが閉じてしまうウザさを回避するためのもの
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.large, tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            DialogContent(
                onDismissRequest = onDismissRequest,
                onPositiveClick = { onPositiveClick(currentIsPicker) },
                screenType = screenType,
                isPicker = currentIsPicker,
                iconButtonOnClick = { currentIsPicker = !currentIsPicker },
                timePickerState = timePickerState,
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
    iconButtonOnClick: () -> Unit,
    screenType: ScreenType,
    isPicker: Boolean,
    timePickerState: TimePickerState,
) {
    // リソース関連
    val iconButtonIcon: ImageVector
    val iconButtonDescription: String
    val dialogTitle: String
    if (isPicker) {
        iconButtonIcon = Icons.Default.Keyboard
        iconButtonDescription = stringResource(id = R.string.input_time)
        dialogTitle = stringResource(id = R.string.picker_time)
    } else {
        iconButtonIcon = Icons.Default.Schedule
        iconButtonDescription = stringResource(id = R.string.picker_time)
        dialogTitle = stringResource(id = R.string.input_time)
    }

    when (screenType) {
        ScreenType.CompactCompact -> {
            DialogContentCompactBody(
                onDismissRequest = onDismissRequest,
                onPositiveClick = onPositiveClick,
                timePickerState = timePickerState,
            )
        }

        ScreenType.CompactLandscape -> {
            if (isPicker) {
                DialogContentCompactLandscapePickerBody(
                    onDismissRequest = onDismissRequest,
                    onPositiveClick = onPositiveClick,
                    iconButtonOnClick = iconButtonOnClick,
                    timePickerState = timePickerState,
                    iconButtonIcon = iconButtonIcon,
                    iconButtonDescription = iconButtonDescription,
                    dialogTitle = dialogTitle,
                )
            } else {
                // TimeInputはそのまま収まるから，特別なサイズ調整をしていないContentBodyで十分
                DialogContentBody(
                    onDismissRequest = onDismissRequest,
                    onPositiveClick = onPositiveClick,
                    iconButtonOnClick = iconButtonOnClick,
                    timePickerState = timePickerState,
                    iconButtonIcon = iconButtonIcon,
                    iconButtonDescription = iconButtonDescription,
                    dialogTitle = dialogTitle,
                    isPicker = isPicker
                )
            }
        }

        else -> {
            DialogContentBody(
                onDismissRequest = onDismissRequest,
                onPositiveClick = onPositiveClick,
                iconButtonOnClick = iconButtonOnClick,
                timePickerState = timePickerState,
                iconButtonIcon = iconButtonIcon,
                iconButtonDescription = iconButtonDescription,
                dialogTitle = dialogTitle,
                isPicker = isPicker
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogContentBody(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onPositiveClick: () -> Unit,
    iconButtonOnClick: () -> Unit,
    timePickerState: TimePickerState,
    iconButtonIcon: ImageVector,
    iconButtonDescription: String,
    dialogTitle: String,
    isPicker: Boolean
) {
    Column(
        modifier = modifier.padding(top = 24.dp, bottom = 8.dp, start = 24.dp, end = 24.dp),
    ) {
        Text(
            text = dialogTitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.height(20.dp))
        if (isPicker) {
            TimePicker(
                modifier = Modifier.align(Alignment.CenterHorizontally), state = timePickerState
            )
        } else {
            TimeInput(
                modifier = Modifier.align(Alignment.CenterHorizontally), state = timePickerState
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            IconButton(
                onClick = iconButtonOnClick,
            ) {
                Icon(
                    imageVector = iconButtonIcon,
                    contentDescription = iconButtonDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.cancel))
            }
            TextButton(onClick = {
                onDismissRequest()
                onPositiveClick()
            }) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogContentCompactLandscapePickerBody(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onPositiveClick: () -> Unit,
    iconButtonOnClick: () -> Unit,
    timePickerState: TimePickerState,
    iconButtonIcon: ImageVector,
    iconButtonDescription: String,
    dialogTitle: String,
) {
    // コンパクトな横画面だとTimePickerとボタンがダイアログに収まらない
    // そのためBoxでTimePickerのpadding部分にボタンを重ねて表示している
    Box(
        modifier = modifier
            .fillMaxHeight()
            .wrapContentWidth()
            .padding(horizontal = 24.dp),
    ) {
        Text(
            text = dialogTitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        TimePicker(
            modifier = Modifier
                .align(Alignment.Center)
                .scale(0.8f),
            state = timePickerState,
            layoutType = TimePickerLayoutType.Horizontal
        )
        Row(
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            IconButton(onClick = iconButtonOnClick) {
                Icon(
                    imageVector = iconButtonIcon,
                    contentDescription = iconButtonDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.cancel))
            }
            TextButton(onClick = {
                onDismissRequest()
                onPositiveClick()
            }) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    }
}

/**
 * WindowWidthSizeとWindowHeightSizeがともにCompactな端末用
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogContentCompactBody(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onPositiveClick: () -> Unit,
    timePickerState: TimePickerState,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp, start = 24.dp, end = 24.dp),
    ) {
        Text(
            text = stringResource(id = R.string.input_time),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        TimeInput(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            state = timePickerState,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.cancel))
            }
            TextButton(onClick = {
                onDismissRequest()
                onPositiveClick()
            }) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = false, widthDp = 400, heightDp = 400)
@Composable
private fun TimePickerDialogPreview() {
    BasicAlertDialog(
        onDismissRequest = {},
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight(),
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.large, tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            DialogContentCompactBody(
                onDismissRequest = {},
                onPositiveClick = {},
                timePickerState = TimePickerState(0, 0, false),
            )
        }
    }
}