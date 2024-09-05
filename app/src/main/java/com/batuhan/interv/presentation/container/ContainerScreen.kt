package com.batuhan.interv.presentation.container

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.batuhan.interv.R
import com.batuhan.interv.data.model.InterviewType
import com.batuhan.interv.presentation.interview.InterviewListScreen
import com.batuhan.interv.presentation.question.QuestionListScreen
import com.batuhan.interv.presentation.settings.SettingsScreen
import com.batuhan.interv.ui.theme.fontFamily
import com.batuhan.interv.util.BaseView
import com.batuhan.interv.util.BrowserEvent
import com.batuhan.interv.util.DialogData
import com.batuhan.interv.util.EnterInterviewDialogData
import com.batuhan.interv.util.EnterInterviewView
import kotlinx.coroutines.launch

@Composable
fun ContainerScreen(
    createInterview: () -> Unit,
    navigateToDetail: (Long) -> Unit,
    enterInterview: (Long, InterviewType, String, String) -> Unit,
    sendBrowserEvent: (BrowserEvent) -> Unit,
    restartApplication: () -> Unit,
    setStyle: (Boolean) -> Unit,
    importQuestions: () -> Unit,
    exportQuestions: () -> Unit
) {
    ContainerScreenContent(createInterview, navigateToDetail, enterInterview, sendBrowserEvent, restartApplication, setStyle, importQuestions, exportQuestions)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContainerScreenContent(
    createInterview: () -> Unit,
    navigateToDetail: (Long) -> Unit,
    enterInterview: (Long, InterviewType, String, String) -> Unit,
    sendBrowserEvent: (BrowserEvent) -> Unit,
    restartApplication: () -> Unit,
    setStyle: (Boolean) -> Unit,
    importQuestions: () -> Unit,
    exportQuestions: () -> Unit
) {
    val pagerState =
        rememberPagerState {
            3
        }
    var dialogData: DialogData? by remember {
        mutableStateOf(null)
    }
    var enterInterviewDialogData: EnterInterviewDialogData? by remember {
        mutableStateOf(null)
    }

    val coroutineScope = rememberCoroutineScope()
    EnterInterviewView(data = enterInterviewDialogData) {
        BaseView(dialogData) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    NavigationBar(containerColor = Color.Transparent) {
                        NavigationBarItem(
                            alwaysShowLabel = false,
                            selected = pagerState.currentPage == 0,
                            onClick = {
                                dialogData = null
                                coroutineScope.launch {
                                    pagerState.scrollToPage(0)
                                }
                            },
                            icon = {
                                Icon(
                                    painterResource(id = R.drawable.ic_quiz),
                                    contentDescription = null,
                                )
                            },
                            label = {
                                Text(
                                    stringResource(id = R.string.navitem_one),
                                    fontFamily = fontFamily,
                                )
                            },
                        )
                        NavigationBarItem(
                            alwaysShowLabel = false,
                            selected = pagerState.currentPage == 1,
                            onClick = {
                                dialogData = null
                                coroutineScope.launch {
                                    pagerState.scrollToPage(1)
                                }
                            },
                            icon = {
                                Icon(
                                    painterResource(id = R.drawable.ic_question_mark_24),
                                    contentDescription = null,
                                )
                            },
                            label = {
                                Text(
                                    stringResource(id = R.string.navitem_two),
                                    fontFamily = fontFamily,
                                )
                            },
                        )
                        NavigationBarItem(
                            alwaysShowLabel = false,
                            selected = pagerState.currentPage == 2,
                            onClick = {
                                dialogData = null
                                coroutineScope.launch {
                                    pagerState.scrollToPage(2)
                                }
                            },
                            icon = {
                                Icon(
                                    Icons.Outlined.Settings,
                                    contentDescription = null,
                                )
                            },
                            label = {
                                Text(
                                    stringResource(id = R.string.navitem_three),
                                    fontFamily = fontFamily,
                                )
                            },
                        )
                    }
                },
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.padding(it),
                    userScrollEnabled = false,
                ) { page ->
                    when (page) {
                        0 -> {
                            InterviewListScreen(
                                showDialog = { data ->
                                    dialogData = data
                                },
                                clearDialog = { dialogData = null },
                                createInterview = createInterview,
                                navigateToDetail = navigateToDetail,
                                enterInterview = { id, type, langCode, apiKey ->
                                    enterInterviewDialogData =
                                        EnterInterviewDialogData(
                                            id,
                                            {
                                                enterInterview.invoke(id, type, langCode, apiKey)
                                            },
                                            {
                                                enterInterviewDialogData = null
                                            },
                                        )
                                },
                            )
                        }

                        1 -> {
                            QuestionListScreen(
                                showDialog = { data ->
                                    dialogData = data
                                },
                                clearDialog = { dialogData = null },
                            )
                        }

                        2 -> {
                            SettingsScreen(
                                exportQuestions = exportQuestions,
                                importQuestions = importQuestions,
                                restartApplication = restartApplication,
                                setStyle = setStyle,
                                showDialog = { data ->
                                    dialogData = data
                                },
                                clearDialog = { dialogData = null },
                                sendBrowserEvent = sendBrowserEvent,
                            )
                        }
                    }
                }
            }
        }
    }
}
