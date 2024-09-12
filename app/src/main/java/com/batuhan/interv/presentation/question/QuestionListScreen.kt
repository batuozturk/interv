package com.batuhan.interv.presentation.question

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.batuhan.interv.MainActivity
import com.batuhan.interv.R
import com.batuhan.interv.data.model.FilterType
import com.batuhan.interv.data.model.Question
import com.batuhan.interv.util.ActionView
import com.batuhan.interv.util.DialogAction
import com.batuhan.interv.util.DialogData
import com.batuhan.interv.util.DialogType
import com.batuhan.interv.util.FilterDialogView
import com.batuhan.interv.util.dataStore
import com.batuhan.interv.util.isTablet

@Composable
fun QuestionListScreen(
    showDialog: (DialogData) -> Unit,
    clearDialog: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel = hiltViewModel<QuestionListViewModel>()
    val questions = viewModel.questions.collectAsLazyPagingItems()
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
                is QuestionListEvent.InitializeQuestion -> {
                    viewModel.setQuestionEditing(true)
                }

                is QuestionListEvent.CreateQuestion -> {
                    viewModel.createQuestion()
                }

                QuestionListEvent.ClearDialog -> clearDialog.invoke()
                is QuestionListEvent.DeleteQuestion -> viewModel.deleteQuestion(it.question)
                QuestionListEvent.OpenFilter -> viewModel.setFilterType()

                QuestionListEvent.GenerateQuestions -> {
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
                        viewModel.generateQuestions(apiKey)
                    }
                }

                QuestionListEvent.InitializeGenerateQuestions -> {
                    viewModel.setGenerating(true)
                }
            }
        }
    }
    val isTablet by remember(context.isTablet()) {
        derivedStateOf { context.isTablet() }
    }
    val filterType: FilterType.Question? by remember {
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
            QuestionListScreenContentForTablet(
                uiState,
                questions,
                viewModel::sendEvent,
                viewModel::updateQuestionText,
                viewModel::updateLangCode,
                onEditDismiss = {
                    viewModel.setQuestionEditing(false)
                },
                viewModel::filterByText,
                viewModel::updateGenerateQuestionText,
                onGenerateDismiss = {
                    viewModel.setGenerating(false)
                },
            )
        } else {
            QuestionListScreenContent(
                uiState,
                questions,
                viewModel::sendEvent,
                viewModel::updateQuestionText,
                viewModel::updateLangCode,
                onEditDismiss = {
                    viewModel.setQuestionEditing(false)
                },
                viewModel::filterByText,
                viewModel::updateGenerateQuestionText,
                onGenerateDismiss = {
                    viewModel.setGenerating(false)
                },
            )
        }
    }
}

@Composable
fun QuestionListScreenContentForTablet(
    uiState: QuestionListUiState,
    questions: LazyPagingItems<Question>,
    sendEvent: (QuestionListEvent) -> Unit,
    updateQuestionText: (String) -> Unit,
    updateLangCode: (String) -> Unit,
    onEditDismiss: () -> Unit,
    updateFilterText: (String) -> Unit,
    updateGenerateQuestionText: (String) -> Unit,
    onGenerateDismiss: () -> Unit,
) {
    val isEditing by remember(uiState.isEditing) {
        derivedStateOf { uiState.isEditing }
    }
    val isGenerating by remember(uiState.isGenerating) {
        derivedStateOf { uiState.isGenerating }
    }
    val questionText by remember(uiState.questionText) {
        derivedStateOf { uiState.questionText }
    }
    val langCode by remember(uiState.langCode) {
        derivedStateOf { uiState.langCode }
    }

    val generateQuestionText by remember(uiState.generateQuestionText) {
        derivedStateOf { uiState.generateQuestionText }
    }
    val weight by animateFloatAsState(
        targetValue = if (isEditing || isGenerating) 3f else 0.001f,
        animationSpec = tween(durationMillis = 1000),
    )
    val loadState = questions.loadState
    Row(Modifier.fillMaxSize()) {
        Column(Modifier.weight(9f - weight)) {
            ActionView(
                searchString = updateFilterText,
                icon1 = Icons.AutoMirrored.Default.List,
                icon2 = Icons.Default.Add,
                icon3 = ImageVector.vectorResource(id = R.drawable.ic_outline_auto_awesome_24),
                placeholderString = stringResource(R.string.search_questions),
                action1 = { sendEvent(QuestionListEvent.OpenFilter) }, // filtering
                action2 = { sendEvent(QuestionListEvent.InitializeQuestion) },
                action3 = { sendEvent(QuestionListEvent.InitializeGenerateQuestions) },
            )
            LazyVerticalGrid(GridCells.Fixed(2), Modifier.fillMaxHeight()) {
                items(questions.itemCount) {
                    questions[it]?.let { question ->
                        QuestionListItem(question = question, sendEvent)
                    }
                }
                if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && questions.itemCount == 0) {
                    item {
                        EmptyQuestionView()
                    }
                }
            }
        }
        Column(modifier = Modifier.weight(weight)) {
            if (weight > 2.25 && isEditing) {
                CreateQuestionView(
                    isTablet = true,
                    questionText = questionText,
                    langCode = langCode,
                    createQuestion = { sendEvent(QuestionListEvent.CreateQuestion) },
                    updateQuestionText = updateQuestionText,
                    updateLangCode = updateLangCode,
                    onDismiss = onEditDismiss,
                )
            } else if (weight > 2.25 && isGenerating) {
                CreateQuestionView(
                    isTablet = true,
                    questionText = generateQuestionText,
                    langCode = langCode,
                    createQuestion = { sendEvent(QuestionListEvent.GenerateQuestions) },
                    updateQuestionText = updateGenerateQuestionText,
                    updateLangCode = updateLangCode,
                    onDismiss = onGenerateDismiss,
                )
            }
        }
    }
}

@Composable
fun QuestionListScreenContent(
    uiState: QuestionListUiState,
    questions: LazyPagingItems<Question>,
    sendEvent: (QuestionListEvent) -> Unit,
    updateQuestionText: (String) -> Unit,
    updateLangCode: (String) -> Unit,
    onEditDismiss: () -> Unit,
    updateFilterText: (String) -> Unit,
    updateGenerateQuestionText: (String) -> Unit,
    onGenerateDismiss: () -> Unit,
) {
    val isEditing by remember(uiState.isEditing) {
        derivedStateOf { uiState.isEditing }
    }
    val isGenerating by remember(uiState.isGenerating) {
        derivedStateOf { uiState.isGenerating }
    }
    val questionText by remember(uiState.questionText) {
        derivedStateOf { uiState.questionText }
    }
    val langCode by remember(uiState.langCode) {
        derivedStateOf { uiState.langCode }
    }
    val generateQuestionText by remember(uiState.generateQuestionText) {
        derivedStateOf { uiState.generateQuestionText }
    }

    val loadState = questions.loadState

    Column(Modifier.fillMaxSize()) {
        ActionView(
            searchString = updateFilterText,
            icon1 = Icons.AutoMirrored.Default.List,
            icon2 = Icons.Default.Add,
            icon3 = ImageVector.vectorResource(id = R.drawable.ic_outline_auto_awesome_24),
            placeholderString = stringResource(R.string.search_questions),
            action1 = { sendEvent(QuestionListEvent.OpenFilter) }, // filtering
            action2 = { sendEvent(QuestionListEvent.InitializeQuestion) },
            action3 = { sendEvent(QuestionListEvent.InitializeGenerateQuestions) },
        )
        AnimatedVisibility(visible = isEditing) {
            CreateQuestionView(
                isTablet = false,
                questionText = questionText,
                langCode = langCode,
                createQuestion = { sendEvent(QuestionListEvent.CreateQuestion) },
                updateQuestionText = updateQuestionText,
                updateLangCode = updateLangCode,
                onDismiss = onEditDismiss,
            )
        }
        AnimatedVisibility(visible = isGenerating) {
            CreateQuestionView(
                isTablet = true,
                questionText = generateQuestionText,
                langCode = langCode,
                createQuestion = { sendEvent(QuestionListEvent.GenerateQuestions) },
                updateQuestionText = updateGenerateQuestionText,
                updateLangCode = updateLangCode,
                onDismiss = onGenerateDismiss,
            )
        }
        if (!isEditing && !isGenerating) {
            LazyColumn(Modifier.fillMaxSize()) {
                items(questions.itemCount) {
                    questions[it]?.let { question ->
                        QuestionListItem(question = question, sendEvent)
                    }
                }
                if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && questions.itemCount == 0) {
                    item {
                        EmptyQuestionView()
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionListItem(
    question: Question,
    sendEvent: (QuestionListEvent) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 100.dp)
            .padding(8.dp)
            .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(question.question ?: "undefined", modifier = Modifier.weight(7f))
        IconButton(
            modifier = Modifier.weight(1f),
            onClick = { sendEvent(QuestionListEvent.DeleteQuestion(question)) },
        ) {
            Icon(Icons.Outlined.Delete, contentDescription = null)
        }
    }
}

@Composable
fun EmptyQuestionView() {
    Text(
        stringResource(R.string.empty_question_info),
        modifier = Modifier.fillMaxSize().padding(32.dp),
        textAlign = TextAlign.Center,
    )
}
