package me.ljpb.alarmbynotification.ui

import android.annotation.SuppressLint
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.ljpb.alarmbynotification.R
import me.ljpb.alarmbynotification.Utility
import me.ljpb.alarmbynotification.data.TimeData
import me.ljpb.alarmbynotification.data.TimeType
import me.ljpb.alarmbynotification.ui.component.AlarmCard
import me.ljpb.alarmbynotification.ui.component.TimePickerDialog
import me.ljpb.alarmbynotification.ui.component.TimerCard
import java.time.LocalDateTime

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeScreenViewMode: HomeScreenViewModel
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val timePickerDialogViewModel: TimePickerDialogViewModel =
        viewModel(factory = ViewModelProvider.Factory)
    val currentTime by homeScreenViewMode.currentDateTime.collectAsState()
    val scope = rememberCoroutineScope()
    scope.launch {
        homeScreenViewMode.updateCurrentDateTime()
    }
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { HomeScreenTopAppBar(scrollBehavior, currentTime) },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            FloatingActionButton {
                timePickerDialogViewModel.showAlarmDialog()
            }
        }
    ) { innerPadding ->
        HomeScreenContent(
            innerPadding = innerPadding,
            timePickerDialogViewModel = timePickerDialogViewModel,
            homeScreenViewModel = homeScreenViewMode
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
private fun TimeList(
    modifier: Modifier = Modifier,
    setTimeList: List<TimeData>,
    onTitleClick: (TimeData) -> Unit,
    onTimeClick: (TimeData) -> Unit,
    onDeleteClick: (TimeData) -> Unit,
    homeScreenViewModel: HomeScreenViewModel
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(setTimeList) { time ->
            if (time.type == TimeType.Alarm) {
                AlarmCard(
                    onTitleClick = { onTitleClick(time) },
                    onTimeClick = { onTimeClick(time) },
                    onDeleteClick = { onDeleteClick(time) },
                    timeData = time,
                    modifier = Modifier.padding(
                        vertical = dimensionResource(id = R.dimen.padding_small),
                        horizontal = dimensionResource(id = R.dimen.padding_medium)
                    )
                )
            } else {
                val currentTime by homeScreenViewModel.currentDateTime.collectAsState()
                TimerCard(
                    onTitleClick = { onTitleClick(time) },
                    onTimeClick = { onTimeClick(time) },
                    onDeleteClick = { onDeleteClick(time) },
                    currentTime = currentTime,
                    timeData = time,
                    modifier = Modifier.padding(
                        vertical = dimensionResource(id = R.dimen.padding_small),
                        horizontal = dimensionResource(id = R.dimen.padding_medium)
                    )
                )
            }
        }
    }
}


/**
 * HomeScreenに実際に配置するコンポーザブル
 * 状態に応じてEmptyとAlarmListを切り替える
 */
@Composable
private fun HomeScreenContent(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues,
    timePickerDialogViewModel: TimePickerDialogViewModel,
    homeScreenViewModel: HomeScreenViewModel
) {
    val setTimeList by homeScreenViewModel.setTimeList.collectAsState()
    val setTimeIsEmpty = setTimeList.isEmpty()

    if (timePickerDialogViewModel.isShow) {
        TimePickerDialog(
            onDismissRequest = timePickerDialogViewModel::hiddenDialog,
            onPositiveClick = timePickerDialogViewModel::add,
            onChangeDialog = timePickerDialogViewModel::changeDialog,
            timePickerDialogViewModel = timePickerDialogViewModel
        )
    }
/*
    if (homeScreenViewModel.timeUpdateDialogIsShow) {
        TimePickerDialog(
            onDismissRequest = timePickerDialogViewModel::hiddenDialog,
            onPositiveClick = timePickerDialogViewModel::add,
            onChangeDialog = timePickerDialogViewModel::changeDialog,
            timePickerDialogViewModel = timePickerDialogViewModel,
            showChangeButton = false
        )
    }
    
 */

    if (homeScreenViewModel.titleInputDialogIsShow) {
        val focusRequester = remember { FocusRequester() }
        TitleInputDialog(
            onDismissRequest = homeScreenViewModel::hiddenTitleInputDialog,
            onPositiveClick = homeScreenViewModel::setTitle,
            focusRequester = focusRequester,
            defaultTitle = homeScreenViewModel.getDefaultTitle()
        )
        LaunchedEffect(Unit){
            focusRequester.requestFocus()
        }
    }

    Column {
        if (setTimeIsEmpty) {
            Empty()
        } else {
            TimeList(
                setTimeList = setTimeList,
                homeScreenViewModel = homeScreenViewModel,
                onTitleClick = homeScreenViewModel::showTitleInputDialog,
                onTimeClick = homeScreenViewModel::showTimeUpdateDialog,
                onDeleteClick = homeScreenViewModel::delete,
                modifier = modifier.padding(innerPadding),
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
private fun HomeScreenTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    currentTime: LocalDateTime
) {
    val context = LocalContext.current
    val isFormat24 = DateFormat.is24HourFormat(context)

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        scrollBehavior = scrollBehavior,
        // 現在の時刻を表示
        title = {
            Text(text = Utility.localDateTimeToFormattedTime(currentTime, isFormat24))
        }
    )
}


@Composable
private fun FloatingActionButton(
    onClick: () -> Unit,
) {
    LargeFloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
    ) {
        Icon(Icons.Filled.Add, stringResource(id = R.string.add))
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
 
}