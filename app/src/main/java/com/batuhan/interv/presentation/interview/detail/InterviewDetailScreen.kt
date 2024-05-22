package com.batuhan.interv.presentation.interview.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batuhan.interv.R
import com.batuhan.interv.data.model.InterviewStep
import com.batuhan.interv.data.model.InterviewType
import com.batuhan.interv.ui.theme.fontFamily
import com.batuhan.interv.util.BaseView
import com.batuhan.interv.util.DialogData
import com.batuhan.interv.util.EnterInterviewDialogData
import com.batuhan.interv.util.EnterInterviewView
import com.batuhan.interv.util.isTablet

@Composable
fun InterviewDetailScreen(
    interviewId: Long,
    onBackPressed: () -> Unit,
    showDialog: (DialogData) -> Unit = {},
    clearDialog: () -> Unit = {},
    enterInterview: (Long, InterviewType, String) -> Unit = { _, _, _ -> },
) {
    val context = LocalContext.current

    val viewModel = hiltViewModel<InterviewDetailViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = interviewId) {
        viewModel.getInterviewWithSteps(interviewId)
    }

    val dialogData by remember(uiState.dialogData) {
        derivedStateOf { uiState.dialogData }
    }

    var enterInterviewDialogData: EnterInterviewDialogData? by remember {
        mutableStateOf(null)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect {
            when (it) {
                is InterviewDetailEvent.Back -> {
                    viewModel.setInterviewWithStepsAsInitial()
                    onBackPressed.invoke()
                }

                is InterviewDetailEvent.ClearDialog -> clearDialog.invoke()
                is InterviewDetailEvent.DeleteInterview -> viewModel.deleteInterview(it.interview)
                is InterviewDetailEvent.RetryInterview -> viewModel.retryInterview(it.interview, it.isTablet)
                is InterviewDetailEvent.ShareInterview -> viewModel.shareInterview(it.interview)
                is InterviewDetailEvent.EnterInterview -> {
                    enterInterviewDialogData =
                        EnterInterviewDialogData(
                            it.interviewId,
                            {
                                enterInterview(it.interviewId, it.interviewType, it.languageCode)
                            },
                            {
                                enterInterviewDialogData = null
                            },
                        )
                }
            }
        }
    }

    val isTablet by remember(context.isTablet()) {
        derivedStateOf { context.isTablet() }
    }
    if (isTablet) {
        BackHandler {
            viewModel.sendEvent(InterviewDetailEvent.Back)
        }
        LaunchedEffect(dialogData) {
            dialogData?.let(showDialog)
        }
    }
    EnterInterviewView(data = enterInterviewDialogData) {
        if (isTablet) {
            InterviewDetailScreenContentForTablet(interviewId, uiState, viewModel::sendEvent)
        } else {
            InterviewDetailScreenContent(interviewId, dialogData, uiState, viewModel::sendEvent)
        }
    }
}

@Composable
fun InterviewDetailScreenContentForTablet(
    interviewId: Long,
    uiState: InterviewDetailUiState,
    sendEvent: (InterviewDetailEvent) -> Unit,
) {
    ScreenContent(interviewId, uiState, sendEvent)
}

@Composable
fun InterviewDetailScreenContent(
    interviewId: Long,
    dialogData: DialogData?,
    uiState: InterviewDetailUiState,
    sendEvent: (InterviewDetailEvent) -> Unit,
) {
    BaseView(dialogData = dialogData) {
        ScreenContent(interviewId, uiState, sendEvent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenContent(
    interviewId: Long,
    uiState: InterviewDetailUiState,
    sendEvent: (InterviewDetailEvent) -> Unit,
) {
    val interviewWithSteps by remember(uiState.interviewWithSteps) {
        derivedStateOf { uiState.interviewWithSteps }
    }
    val context = LocalContext.current
    val isTablet by remember(context.isTablet()) {
        derivedStateOf { context.isTablet() }
    }
    if (interviewWithSteps != null && interviewWithSteps?.interview?.interviewId == interviewId) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            interviewWithSteps!!.interview?.interviewName ?: "",
                            fontFamily = fontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors =
                        TopAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Unspecified,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                sendEvent.invoke(
                                    InterviewDetailEvent.Back,
                                )
                            },
                        ) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        if (interviewWithSteps?.interview?.completed == true) {
                            IconButton(
                                onClick = {
                                    sendEvent.invoke(
                                        InterviewDetailEvent.RetryInterview(
                                            interviewWithSteps!!.interview!!,
                                            isTablet
                                        ),
                                    )
                                },
                            ) {
                                Icon(Icons.Outlined.Refresh, contentDescription = null)
                            }
                            /*
                            IconButton(
                                onClick = {
                                    sendEvent.invoke(
                                        InterviewDetailEvent.ShareInterview(
                                            interviewWithSteps!!.interview!!,
                                        ),
                                    )
                                },
                            ) {
                                Icon(Icons.Outlined.Share, contentDescription = null)
                            }
                            */
                        } else {
                            if (!isTablet) {
                                IconButton(
                                    onClick = {
                                        sendEvent.invoke(
                                            InterviewDetailEvent.EnterInterview(
                                                interviewWithSteps!!.interview!!.interviewId
                                                    ?: return@IconButton,
                                                interviewWithSteps!!.interview!!.interviewType
                                                    ?: return@IconButton,
                                                interviewWithSteps!!.interview!!.langCode
                                                    ?: return@IconButton,
                                            ),
                                        )
                                    },
                                ) {
                                    Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                                }
                            }
                        }
                        IconButton(
                            onClick = {
                                sendEvent.invoke(
                                    InterviewDetailEvent.DeleteInterview(
                                        interviewWithSteps!!.interview!!,
                                    ),
                                )
                            },
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = null)
                        }
                    },
                )
            },
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(it)) {
                item {
                    Column(
                        Modifier
                            .fillMaxWidth().height(80.dp)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(stringResource(id = R.string.interview_detail_question_and_answers))
                    }
                }
                items(interviewWithSteps?.steps?.size ?: 0) {
                    interviewWithSteps?.steps?.get(it)?.let {
                        InterviewDetailListItem(interview = it)
                    }
                }
            }
        }
    }
}

@Composable
fun InterviewDetailListItem(interview: InterviewStep) {
    if (interview.interviewStepId != null) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(10.dp))
                .padding(10.dp),
        ) {
            Text(interview.question?.question?.lowercase() ?: "")
            Spacer(modifier = Modifier.height(40.dp))
            Text(interview.answer?.lowercase() ?: "not answered yet")
        }
    }
}
