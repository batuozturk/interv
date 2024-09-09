package com.batuhan.interv.presentation.interview

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.batuhan.interv.MainActivity
import com.batuhan.interv.R
import com.batuhan.interv.data.model.FilterType
import com.batuhan.interv.data.model.Interview
import com.batuhan.interv.data.model.InterviewType
import com.batuhan.interv.presentation.interview.create.CreateInterviewScreen
import com.batuhan.interv.presentation.interview.create.addstep.AddStepScreen
import com.batuhan.interv.presentation.interview.detail.InterviewDetailScreen
import com.batuhan.interv.util.ActionView
import com.batuhan.interv.util.DialogAction
import com.batuhan.interv.util.DialogData
import com.batuhan.interv.util.DialogType
import com.batuhan.interv.util.FilterDialogView
import com.batuhan.interv.util.dataStore
import com.batuhan.interv.util.isTablet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun InterviewListScreen(
    showDialog: (DialogData) -> Unit,
    clearDialog: () -> Unit,
    createInterview: () -> Unit,
    navigateToDetail: (interviewId: Long) -> Unit,
    enterInterview: (interviewId: Long, interviewType: InterviewType, langCode: String, apiKey: String) -> Unit,
) {
    val context = LocalContext.current
    val viewModel = hiltViewModel<InterviewListViewModel>()
    val interviews = viewModel.interviews.collectAsLazyPagingItems()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogData by remember(uiState.dialogData) {
        derivedStateOf { uiState.dialogData }
    }
    var apiKey by remember {
        mutableStateOf("")
    }
    LaunchedEffect(true) {
        context.dataStore.data.collect {
            apiKey = it[MainActivity.KEY_PREFERENCES_OPENAI_CLIENT_KEY] ?: run {
                ""
            }
        }
    }
    LaunchedEffect(dialogData) {
        dialogData?.let(showDialog)
    }
    LaunchedEffect(true) {
        viewModel.event.collect {
            when (it) {
                InterviewListEvent.CreateInterview -> createInterview.invoke()
                InterviewListEvent.ClearDialog -> clearDialog.invoke()
                is InterviewListEvent.Detail -> navigateToDetail.invoke(it.interviewId)
                is InterviewListEvent.DeleteInterview -> viewModel.deleteInterview(it.interview)
                is InterviewListEvent.EnterInterview -> {
                    if (apiKey.isEmpty()) {
                        viewModel.showDialog(
                            DialogData(
                                title = R.string.api_key_empty,
                                type = DialogType.ERROR,
                                actions =
                                    listOf(
                                        DialogAction(R.string.dismiss) {
                                            viewModel.clearDialog()
                                        },
                                    ),
                            ),
                        )
                    } else {
                        enterInterview.invoke(
                            it.interviewId,
                            it.interviewType,
                            it.langCode,
                            apiKey,
                        )
                    }
                }

                is InterviewListEvent.OpenFilter -> {
                    viewModel.setFilterType()
                }
            }
        }
    }
    val isTablet by remember(context.isTablet()) {
        derivedStateOf { context.isTablet() }
    }
    val filterType: FilterType.Interview? by remember(uiState.filterType) {
        derivedStateOf { uiState.filterType }
    }
    val selectedFilter by remember(uiState.selectedFilter) {
        derivedStateOf { uiState.selectedFilter }
    }
    FilterDialogView(
        filterType = filterType,
        selectedFilter = selectedFilter,
        updateFilterType = viewModel::filter,
    ) {
        if (isTablet) {
            InterviewListScreenContentForTablets(
                interviews,
                viewModel::sendEvent,
                viewModel::showDialog,
                viewModel::clearDialog,
                viewModel::filterByText,
            )
        } else {
            InterviewListScreenContent(interviews, viewModel::sendEvent, viewModel::filterByText)
        }
    }
}

@Composable
fun InterviewListScreenContent(
    interviews: LazyPagingItems<Interview>,
    sendEvent: (InterviewListEvent) -> Unit,
    updateFilterText: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        ActionView(
            searchString = updateFilterText,
            icon1 = Icons.AutoMirrored.Default.List,
            icon2 = Icons.Default.Add,
            placeholderString = stringResource(R.string.search_interviews),
            action1 = { sendEvent(InterviewListEvent.OpenFilter) },
            action2 = { sendEvent(InterviewListEvent.CreateInterview) },
        )
        LazyColumn(Modifier.fillMaxSize()) {
            items(interviews.itemCount) {
                interviews[it]?.let { interview ->
                    InterviewListItem(interview = interview, sendEvent = sendEvent) {
                        sendEvent(InterviewListEvent.Detail(it))
                    }
                }
            }
        }
    }
}

@Composable
fun InterviewListScreenContentForTablets(
    interviews: LazyPagingItems<Interview>,
    sendEvent: (InterviewListEvent) -> Unit,
    showDialog: (DialogData) -> Unit,
    clearDialog: () -> Unit,
    updateFilterText: (String) -> Unit,
) {
    var isCreating by remember {
        mutableStateOf(false)
    }
    var isAddingStep by remember {
        mutableStateOf(false)
    }
    var addStepInterviewId: Long? by remember {
        mutableStateOf(null)
    }

    var addStepLanguage: String? by remember {
        mutableStateOf(null)
    }

    val coroutineScope = rememberCoroutineScope()

    var interviewDetailId: Long? by remember {
        mutableStateOf(null)
    }
    val weight by animateFloatAsState(
        targetValue = if (isCreating) 3f else 0.001f,
        animationSpec = tween(durationMillis = 1000),
    )
    val weight2 by animateFloatAsState(
        targetValue = if (isAddingStep) 3f else 0.001f,
        animationSpec = tween(durationMillis = 1000),
    )
    val weight3 by animateFloatAsState(
        targetValue = if (interviewDetailId != null) 3f else 0.001f,
        animationSpec = tween(durationMillis = 1000),
    )
    Row(Modifier.fillMaxSize()) {
        Column(Modifier.weight(9f - weight - weight2 - weight3)) {
            ActionView(
                searchString = updateFilterText,
                icon1 = Icons.AutoMirrored.Default.List,
                icon2 = Icons.Default.Add,
                placeholderString = stringResource(R.string.search_interviews),
                action1 = { sendEvent.invoke(InterviewListEvent.OpenFilter) }, // filtering
                action2 = {
                    interviewDetailId = null
                    isCreating = true
                },
            )
            LazyVerticalGrid(GridCells.Fixed(2), Modifier.fillMaxHeight()) {
                items(interviews.itemCount) {
                    interviews[it]?.let { interview ->
                        InterviewListItem(isCreating, interview = interview, sendEvent) {
                            coroutineScope.launch {
                                if (interviewDetailId != null) {
                                    interviewDetailId = null
                                    delay(1000L)
                                }
                                interviewDetailId = it
                            }
                        }
                    }
                }
            }
        }
        Column(modifier = Modifier.weight(weight)) {
            if (weight > 2.25) {
                CreateInterviewScreen(
                    onBackPressed = {
                        isCreating = false
                        isAddingStep = false
                    },
                    showDialog = showDialog,
                    clearDialog = clearDialog,
                    addStep = { id, language ->
                        addStepInterviewId = id
                        addStepLanguage = language
                        isAddingStep = true
                    },
                )
            }
        }
        Column(modifier = Modifier.weight(weight2)) {
            if (weight2 > 2.25 && addStepInterviewId != null && addStepLanguage != null) {
                AddStepScreen(
                    interviewId = addStepInterviewId!!,
                    language = addStepLanguage!!,
                    onBackPressed = {
                        addStepLanguage = null
                        addStepInterviewId = null
                        isAddingStep = false
                    },
                    showDialog = showDialog,
                    clearDialog = clearDialog,
                )
            }
        }
        Column(modifier = Modifier.weight(weight3)) {
            if (weight3 > 2.25 && interviewDetailId != null) {
                InterviewDetailScreen(
                    interviewId = interviewDetailId!!,
                    onBackPressed = {
                        interviewDetailId = null
                    },
                    showDialog = showDialog,
                    clearDialog = clearDialog,
                )
            }
        }
    }
}

@Composable
fun InterviewListItem(
    isInterviewCreating: Boolean = false,
    interview: Interview,
    sendEvent: (InterviewListEvent) -> Unit,
    detail: (Long) -> Unit,
) {
    if (interview.interviewName != null) {
        Row(
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 100.dp)
                .padding(8.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(10.dp))
                .clickable {
                    if (!isInterviewCreating) {
                        detail(interview.interviewId ?: return@clickable)
                    }
                }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                interview.interviewName,
                modifier = Modifier.weight(if (interview.completed != true) 6f else 7f),
            )
            if (interview.completed != true) {
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        sendEvent(
                            InterviewListEvent.EnterInterview(
                                interview.interviewId ?: return@IconButton,
                                interview.interviewType ?: return@IconButton,
                                interview.langCode ?: return@IconButton,
                            ),
                        )
                    },
                ) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                }
            }
            IconButton(
                modifier = Modifier.weight(1f),
                onClick = { sendEvent(InterviewListEvent.DeleteInterview(interview)) },
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null)
            }
        }
    }
}
