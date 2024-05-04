package com.batuhan.interviewself.presentation.interview.create

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batuhan.interviewself.R
import com.batuhan.interviewself.data.model.Interview
import com.batuhan.interviewself.data.model.InterviewType
import com.batuhan.interviewself.ui.theme.InterviewselfTheme
import com.batuhan.interviewself.ui.theme.fontFamily
import com.batuhan.interviewself.util.BaseView
import com.batuhan.interviewself.util.DialogAction
import com.batuhan.interviewself.util.DialogData
import com.batuhan.interviewself.util.DialogType
import com.batuhan.interviewself.util.isTablet

@Composable
fun CreateInterviewScreen(
    onBackPressed: () -> Unit,
    showDialog: (DialogData) -> Unit = {},
    clearDialog: () -> Unit = {},
    addStep: (interviewId: Long) -> Unit,
) {
    val context = LocalContext.current
    val viewModel = hiltViewModel<CreateInterviewViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(true) {
        viewModel.initializeInterview()
        viewModel.event.collect {
            when (it) {
                is CreateInterviewEvent.Back -> {
                    if (it.isSuccess) {
                        showDialog(
                            DialogData(
                                title = R.string.success_interview_saved,
                                type = DialogType.SUCCESS_INFO,
                                actions =
                                    listOf(
                                        DialogAction(R.string.dismiss, clearDialog),
                                    ),
                            ),
                        )
                        viewModel.setInterviewAsInitial()
                    }
                    onBackPressed()
                }

                CreateInterviewEvent.ClearDialog -> clearDialog()
            }
        }
    }
    BackHandler {
        viewModel.cancelInterview(uiState.currentInterview)
    }
    val isTablet by remember(context.isTablet()) {
        derivedStateOf { context.isTablet() }
    }
    if (isTablet) {
        val dialogData by remember(uiState.dialogData) {
            derivedStateOf { uiState.dialogData }
        }
        LaunchedEffect(dialogData) {
            dialogData?.let(showDialog)
        }
        CreateInterviewScreenContentForTablet(
            uiState = uiState,
            updateConfiguration = viewModel::updateCurrentSetup,
            cancelInterview = viewModel::cancelInterview,
            createInterview = viewModel::createInterview,
            addStep = addStep,
        )
    } else {
        CreateInterviewScreenContent(
            uiState = uiState,
            updateConfiguration = viewModel::updateCurrentSetup,
            cancelInterview = viewModel::cancelInterview,
            createInterview = viewModel::createInterview,
            addStep = addStep,
        )
    }
}

@Composable
fun CreateInterviewScreenContentForTablet(
    uiState: CreateInterviewUiState,
    updateConfiguration: (InterviewField) -> Unit,
    cancelInterview: (Interview) -> Unit,
    createInterview: () -> Unit,
    addStep: (interviewId: Long) -> Unit,
) {
    ScreenContent(
        modifier = Modifier.fillMaxSize(),
        uiState = uiState,
        updateConfiguration = updateConfiguration,
        cancelInterview = cancelInterview,
        createInterview = createInterview,
        addStep = addStep,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInterviewScreenContent(
    uiState: CreateInterviewUiState,
    updateConfiguration: (InterviewField) -> Unit,
    cancelInterview: (Interview) -> Unit,
    createInterview: () -> Unit,
    addStep: (interviewId: Long) -> Unit,
) {
    val dialogData by remember(uiState.dialogData) {
        derivedStateOf { uiState.dialogData }
    }
    BaseView(dialogData = dialogData) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.create_interview_title), fontFamily = fontFamily) },
                    navigationIcon = {
                        IconButton(onClick = { cancelInterview(uiState.currentInterview) }) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                        }
                    },
                )
            },
        ) {
            ScreenContent(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(it),
                uiState = uiState,
                updateConfiguration = updateConfiguration,
                cancelInterview = cancelInterview,
                createInterview = createInterview,
                addStep = addStep,
            )
        }
    }
}

@Composable
fun ScreenContent(
    modifier: Modifier = Modifier,
    uiState: CreateInterviewUiState,
    updateConfiguration: (InterviewField) -> Unit,
    cancelInterview: (Interview) -> Unit,
    createInterview: () -> Unit,
    addStep: (interviewId: Long) -> Unit,
) {
    val interviewName by remember(uiState.currentInterview.interviewName) {
        derivedStateOf { uiState.currentInterview.interviewName }
    }
    val questionDuration by remember(uiState.currentInterview.questionDuration) {
        derivedStateOf { uiState.currentInterview.questionDuration }
    }
    val langCode by remember(uiState.currentInterview.langCode) {
        derivedStateOf { uiState.currentInterview.langCode }
    }
    val interviewType by remember(uiState.currentInterview.interviewType) {
        derivedStateOf { uiState.currentInterview.interviewType }
    }
    val tabRows = listOf(R.string.type_video, R.string.type_phone_call)
    val selectedIndex by remember(interviewType) {
        mutableStateOf(interviewType?.text?.let { tabRows.indexOf(it) } ?: 0)
    }
    Column(
        modifier =
            modifier
                .padding(horizontal = 8.dp),
    ) {
        OutlinedTextField(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            placeholder = {
                Text(stringResource(R.string.create_interview_name_placeholder))
            },
            leadingIcon = {
                Icon(
                    painterResource(id = R.drawable.ic_question_mark_24),
                    contentDescription = null,
                )
            },
            colors = OutlinedTextFieldDefaults.colors(),
            value = interviewName ?: "",
            onValueChange = {
                updateConfiguration.invoke(InterviewField.Name(it))
            },
            singleLine = true,
        )
        OutlinedTextField(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
            placeholder = {
                Text(stringResource(R.string.lang_code_placeholder))
            },
            leadingIcon = {
                Icon(painterResource(id = R.drawable.ic_language_24), contentDescription = null)
            },
            colors = OutlinedTextFieldDefaults.colors(),
            value = langCode ?: "",
            onValueChange = {
                updateConfiguration.invoke(InterviewField.Language(it))
            },
            singleLine = true,
        )
        OutlinedTextField(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
            placeholder = {
                Text(stringResource(R.string.create_interview_question_duration_placeholder))
            },
            leadingIcon = {
                Icon(painterResource(id = R.drawable.ic_access_time_24), contentDescription = null)
            },
            colors = OutlinedTextFieldDefaults.colors(),
            value = questionDuration?.toString() ?: "0",
            onValueChange = {
                updateConfiguration.invoke(InterviewField.Duration(it))
            },
            singleLine = true,
        )
        TabRow(
            modifier =
                Modifier
                    .fillMaxWidth().height(76.dp)
                    .padding(top = 12.dp, bottom = 16.dp),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            selectedTabIndex = selectedIndex,
            divider = {},
            indicator = {
                if (selectedIndex < it.size) {
                    Column(
                        modifier =
                            Modifier.tabIndicatorOffset(it[selectedIndex]).fillMaxSize().padding(8.dp)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.onSurface,
                                    RoundedCornerShape(10.dp),
                                ).padding(10.dp),
                    ) {
                    }
                }
            },
        ) {
            Tab(
                modifier = Modifier.height(60.dp),
                selected = selectedIndex == 0,
                onClick = { updateConfiguration.invoke(InterviewField.Type(InterviewType.VIDEO)) },
            ) {
                Text(stringResource(id = InterviewType.VIDEO.text))
            }
            Tab(
                modifier = Modifier.height(60.dp),
                selected = selectedIndex == 0,
                onClick = { updateConfiguration.invoke(InterviewField.Type(InterviewType.PHONE_CALL)) },
            ) {
                Text(stringResource(id = InterviewType.PHONE_CALL.text))
            }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = ButtonDefaults.TextButtonContentPadding,
            onClick = {
                addStep.invoke(uiState.currentInterview.interviewId ?: return@Button)
            },
            colors =
                ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface,
                ),
        ) {
            Text(
                stringResource(id = R.string.add_step),
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Button(
                modifier = Modifier.weight(0.6f),
                contentPadding = ButtonDefaults.TextButtonContentPadding,
                onClick = {
                    cancelInterview(uiState.currentInterview)
                },
                colors =
                    ButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
            ) {
                Text(
                    stringResource(id = R.string.dismiss),
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                )
            }
            Button(
                modifier = Modifier.weight(0.6f),
                contentPadding = ButtonDefaults.TextButtonContentPadding,
                onClick = createInterview,
                colors =
                    ButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
            ) {
                Text(
                    stringResource(id = R.string.save),
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenContentPreview() {
    InterviewselfTheme {
        ScreenContent(
            uiState = CreateInterviewUiState(),
            updateConfiguration = {},
            cancelInterview = {},
            createInterview = { /*TODO*/ },
            modifier = Modifier.fillMaxSize(),
            addStep = {},
        )
    }
}
