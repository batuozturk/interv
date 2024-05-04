package com.batuhan.interviewself.presentation.interview.create.addstep

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.batuhan.interviewself.R
import com.batuhan.interviewself.data.model.Question
import com.batuhan.interviewself.ui.theme.fontFamily
import com.batuhan.interviewself.util.BaseView
import com.batuhan.interviewself.util.DialogData
import com.batuhan.interviewself.util.isTablet

@Composable
fun AddStepScreen(
    interviewId: Long,
    onBackPressed: () -> Unit,
    showDialog: (DialogData) -> Unit = {},
    clearDialog: () -> Unit = {},
) {
    val context = LocalContext.current

    val viewModel = hiltViewModel<AddStepViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val questions = viewModel.questions.collectAsLazyPagingItems()

    val dialogData by remember(uiState.dialogData) {
        derivedStateOf { uiState.dialogData }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect {
            when (it) {
                is AddStepEvent.Back -> onBackPressed.invoke()
                is AddStepEvent.AddStep -> viewModel.addStep(it.question)
                is AddStepEvent.DeleteStep -> viewModel.deleteStep(it.interviewStep)
                is AddStepEvent.ClearDialog -> clearDialog.invoke()
            }
        }
    }

    val isTablet by remember(context.isTablet()) {
        derivedStateOf { context.isTablet() }
    }
    if (isTablet) {
        BackHandler {
            viewModel.sendEvent(AddStepEvent.Back)
        }
        LaunchedEffect(key1 = Unit) {
            viewModel.initInterviewId(interviewId)
        }
        LaunchedEffect(dialogData) {
            dialogData?.let(showDialog)
        }
        AddStepScreenContentForTablet(questions, viewModel::sendEvent)
    } else {
        AddStepScreenContent(dialogData, questions, viewModel::sendEvent)
    }
}

@Composable
fun AddStepScreenContentForTablet(
    questions: LazyPagingItems<Question>,
    sendEvent: (AddStepEvent) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { sendEvent(AddStepEvent.Back) }) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
            Text(stringResource(id = R.string.add_step))
        }

        LazyColumn(Modifier.fillMaxSize()) {
            items(questions.itemCount) {
                questions[it]?.let { question ->
                    QuestionSelectItem(question = question, sendEvent)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStepScreenContent(
    dialogData: DialogData?,
    questions: LazyPagingItems<Question>,
    sendEvent: (AddStepEvent) -> Unit,
) {
    BaseView(dialogData = dialogData) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.add_step), fontFamily = fontFamily) },
                    navigationIcon = {
                        IconButton(onClick = { sendEvent.invoke(AddStepEvent.Back) }) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                        }
                    },
                )
            },
        ) {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(it)) {
                items(questions.itemCount) {
                    questions[it]?.let { question ->
                        QuestionSelectItem(question = question, sendEvent)
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionSelectItem(
    question: Question,
    sendEvent: (AddStepEvent) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(8.dp)
            .clickable {
                sendEvent(AddStepEvent.AddStep(question))
            }
            .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(question.question ?: "undefined")
    }
}
