package me.ljpb.alarmbynotification.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.ljpb.alarmbynotification.R
import me.ljpb.alarmbynotification.Utility
import me.ljpb.alarmbynotification.ui.theme.ui.component.TimePickerDialog
import java.time.LocalTime

// 現在時刻を取得するための更新間隔
private const val DELAY_TIME: Long = 100L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val timePickerDialogViewModel: TimePickerDialogViewModel = viewModel()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { HomeScreenTopAppBar(scrollBehavior) },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = { FloatingActionButton { timePickerDialogViewModel.showDialog() } }
    ) { innerPadding ->
        HomeScreenContent(
            innerPadding = innerPadding,
            timePickerDialogViewModel = timePickerDialogViewModel
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
    innerPadding: PaddingValues
) {
   
}


/**
 * HomeScreenに実際に配置するコンポーザブル
 * 状態に応じてEmptyとAlarmListを切り替える
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues,
    timePickerDialogViewModel: TimePickerDialogViewModel
) {
    if (timePickerDialogViewModel.isShow) {
        TimePickerDialog(
            state = timePickerDialogViewModel.timePickerState, 
            onDismissRequest = timePickerDialogViewModel::hiddenDialog,
            onPositiveClick = {}
        )
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior
) {
    val scope = rememberCoroutineScope()
    var time by remember { mutableStateOf(LocalTime.now()) }
    scope.launch { 
        while (true) {
            delay(DELAY_TIME)
            time = LocalTime.now()
        }
    }
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        scrollBehavior = scrollBehavior,
        // 現在の時刻を表示
        title = {
            Text(text = Utility.localTimeToString(time, true))
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
    HomeScreen()
}