package me.ljpb.alarmbynotification.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import me.ljpb.alarmbynotification.R
import me.ljpb.alarmbynotification.Utility
import me.ljpb.alarmbynotification.data.room.AlarmInfoInterface
import me.ljpb.alarmbynotification.ui.component.AlarmCard
import me.ljpb.alarmbynotification.ui.component.NotificationPermissionDialog
import me.ljpb.alarmbynotification.ui.component.TimePickerDialog
import java.time.LocalDateTime

@SuppressLint("CoroutineCreationDuringComposition", "StateFlowValueCalledInComposition")
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    windowSize: WindowSizeClass,
    homeScreenViewModel: HomeScreenViewModel,
    timePickerDialogViewModel: TimePickerDialogViewModel,
) {
    val currentTime by homeScreenViewModel.currentDateTime.collectAsState()
    val scope = rememberCoroutineScope()
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    scope.launch {
        homeScreenViewModel.updateCurrentDateTime()
    }
    Scaffold(
        topBar = {
            HomeScreenTopAppBar(currentTime = currentTime)
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            if (windowSize.widthSizeClass == WindowWidthSizeClass.Compact) {
                FloatingActionButton {
                    timePickerDialogViewModel.showAlarmDialog(is24Hour = is24Hour)
                }
            }
        }
    ) { innerPadding ->
        HomeScreenContent(
            innerPadding = innerPadding,
            timePickerDialogViewModel = timePickerDialogViewModel,
            homeScreenViewModel = homeScreenViewModel,
            windowSize = windowSize,
            is24Hour = is24Hour,
            actionButtonOnClick = { timePickerDialogViewModel.showAlarmDialog(is24Hour = is24Hour) }
        )
    }
}

/**
 * アラームがない時に表示する画面
 */
@Composable
private fun Empty(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.empty),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * アラームの一覧を表示する
 */
@Composable
private fun AlarmList(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues,
    alarmList: List<AlarmInfoInterface>,
    onTitleClick: (AlarmInfoInterface) -> Unit,
    onDeleteClick: (AlarmInfoInterface) -> Unit,
    onEnableChange: (AlarmInfoInterface, Boolean) -> Unit,
    is24Hour: Boolean,
    homeScreenViewModel: HomeScreenViewModel
) {
    val listState = rememberLazyListState()
    val notificationList by homeScreenViewModel.notificationList.collectAsState()
    var expandCardIndex by remember { mutableIntStateOf(-1) }
    LazyColumn(
        state = listState,
        modifier = modifier.padding(innerPadding)
    ) {
        itemsIndexed(alarmList) { index, alarm ->
            AlarmCard(
                onTitleClick = { onTitleClick(alarm) },
                onDeleteClick = {
                    onDeleteClick(alarm)
                    expandCardIndex = -1
                },
                onEnableChange = { enable -> onEnableChange(alarm, enable) },
                enable = notificationList.find { it.alarmId == alarm.id } != null,  // notificationListに含まれていれば(findがnull出なければ)有効
                alarm = alarm,
                modifier = Modifier.padding(
                    vertical = dimensionResource(id = R.dimen.padding_small),
                    horizontal = dimensionResource(id = R.dimen.padding_medium)
                ),
                is24Hour = is24Hour,
                expanded = expandCardIndex == index,
                onExpandedChange = {
                    expandCardIndex = if (expandCardIndex == index) {
                        -1
                    } else {
                        index
                    } 
                }
            )
        }
        item {
            Spacer(modifier = Modifier.height(128.dp))
        }
    }

    LaunchedEffect(alarmList.size) {
        if (listState.layoutInfo.totalItemsCount > 0) {
            if (homeScreenViewModel.isScroll()) {
                val index = homeScreenViewModel.getAddedItemIndex()
                listState.animateScrollToItem(index)
                homeScreenViewModel.initAddItemId()
            }
        }
    }

}

/**
 * HomeScreenに実際に配置するコンポーザブル
 * 状態に応じてEmptyとAlarmListを切り替える
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun HomeScreenContent(
    modifier: Modifier = Modifier,
    windowSize: WindowSizeClass,
    innerPadding: PaddingValues,
    timePickerDialogViewModel: TimePickerDialogViewModel,
    homeScreenViewModel: HomeScreenViewModel,
    actionButtonOnClick: () -> Unit,
    is24Hour: Boolean,
) {
    val alarmList by homeScreenViewModel.alarmList.collectAsState()

    // 通知権限の許可を促すダイアログを一度表示したかどうか
    var isShowedDialog by remember { mutableStateOf(false) }

    if (timePickerDialogViewModel.isShow) {
        TimePickerDialog(
            onDismissRequest = timePickerDialogViewModel::hiddenDialog,
            onPositiveClick = { timePickerDialogViewModel.add(homeScreenViewModel::setAddItemId) },
            windowSizeClass = windowSize,
            timePickerDialogViewModel = timePickerDialogViewModel,
            // todo ここでis24Hourを渡す
        )
    }

    if (homeScreenViewModel.isShowTitleInputDialog) {
        val focusRequester = remember { FocusRequester() }
        TitleInputDialog(
            onDismissRequest = {
                homeScreenViewModel
                    .hiddenTitleInputDialog()
                    .releaseSelectedAlarm()
            },
            onPositiveClick = homeScreenViewModel::setAlarmName,
            focusRequester = focusRequester,
            defaultTitle = homeScreenViewModel.getSelectedAlarmName()
        )
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }

    /*
        通知権限の許可を促すダイアログ・・・NotificationPermissionDialog
        通知権限の許可ダイアログ・・・OSのやつ
    */
    if (Build.VERSION.SDK_INT >= 33 && !isShowedDialog) {
        // Android13以上で，アプリ起動後にまだ通知権限の許可を促すダイアログを表示していない場合
        val context = LocalContext.current
        val isShowedPermissionDialog by homeScreenViewModel.isShowedPermissionDialog.collectAsState()

        val notificationPermissionState = rememberPermissionState(
            android.Manifest.permission.POST_NOTIFICATIONS
        )
        if (!notificationPermissionState.status.isGranted) {
            NotificationPermissionDialog(onDismissRequest = { isShowedDialog = true }) {
                if (isShowedPermissionDialog) {
                    // 通知権限の許可ダイアログを過去に一度表示している場合は，通知設定画面に遷移する
                    val intent = Intent()
                    intent.action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    intent.putExtra("app_package", context.packageName)
                    intent.putExtra("app_uid", context.applicationInfo.uid)
                    intent.putExtra(
                        "android.provider.extra.APP_PACKAGE",
                        context.packageName
                    )
                    context.startActivity(intent)
                } else {
                    // 通知権限の許可ダイアログを一度も表示したことがない場合
                    notificationPermissionState.launchPermissionRequest()
                    homeScreenViewModel.showPermissionDialog()
                }
            }
        }
    }

    when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            HomeScreenContentBody(
                innerPadding = innerPadding,
                alarmList = alarmList,
                homeScreenViewModel = homeScreenViewModel,
                is24Hour = is24Hour
            )
        }

        else -> {  // 横画面やタブレットなど画面の幅が広い場合
            Row {
                HomeScreenContentBody(
                    modifier = Modifier.weight(1f),
                    innerPadding = innerPadding,
                    alarmList = alarmList,
                    homeScreenViewModel = homeScreenViewModel,
                    is24Hour = is24Hour
                )
                Box(
                    modifier = Modifier
                        .width(128.dp)
                        .fillMaxHeight()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingActionButton {
                        actionButtonOnClick()
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreenContentBody(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues,
    alarmList: List<AlarmInfoInterface>,
    homeScreenViewModel: HomeScreenViewModel,
    is24Hour: Boolean,
) {
    val alarmListIsEmpty = alarmList.isEmpty()
    Box(
        modifier = modifier
    ) {
        if (alarmListIsEmpty) {
            Empty()
        } else {
            AlarmList(
                alarmList = alarmList,
                homeScreenViewModel = homeScreenViewModel,
                onTitleClick = {
                    homeScreenViewModel
                        .selectAlarm(it)
                        .showTitleInputDialog()
                },
                onDeleteClick = {
                    homeScreenViewModel
                        .selectAlarm(it)
                        .changeEnableTo(false)
                        .delete()
                        .releaseSelectedAlarm()
                },
                onEnableChange = { alarm, enable ->
                    homeScreenViewModel
                        .selectAlarm(alarm)
                        .changeEnableTo(enable)
                        .releaseSelectedAlarm()
                },
                innerPadding = innerPadding,
                is24Hour = is24Hour,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TitleInputDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onPositiveClick: (String) -> Unit,
    focusRequester: FocusRequester,
    defaultTitle: String = ""
) {
    var inputTitle by remember { mutableStateOf(defaultTitle) }
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                OutlinedTextField(
                    modifier = Modifier.focusRequester(focusRequester),
                    label = {
                        Text(stringResource(id = R.string.set_title))
                    },
                    value = inputTitle,
                    onValueChange = {
                        inputTitle = it
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onPositiveClick(inputTitle)
                            onDismissRequest()
                        }
                    )
                )
                Row(
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { onDismissRequest() }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                    TextButton(onClick = {
                        onPositiveClick(inputTitle)
                        onDismissRequest()
                    }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenTopAppBar(currentTime: LocalDateTime) {
    val context = LocalContext.current
    val isFormat24 = DateFormat.is24HourFormat(context)

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        // 現在の時刻を表示
        title = {
            Text(text = Utility.localDateTimeToFormattedTime(currentTime, isFormat24))
        },
    )
}


@Composable
private fun FloatingActionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    LargeFloatingActionButton(
        modifier = modifier,
        onClick = onClick,
        shape = CircleShape,
    ) {
        Icon(Icons.Filled.Add, stringResource(id = R.string.add))
    }
}

@Preview(showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
//    HomeScreenTopAppBar(currentTime = LocalDateTime.now())
}