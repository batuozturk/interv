package com.batuhan.interv.presentation.interview.create

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
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batuhan.interv.R
import com.batuhan.interv.data.model.Interview
import com.batuhan.interv.data.model.InterviewType
import com.batuhan.interv.data.model.LanguageType
import com.batuhan.interv.data.model.findType
import com.batuhan.interv.ui.theme.InterviewselfTheme
import com.batuhan.interv.ui.theme.fontFamily
import com.batuhan.interv.util.BaseView
import com.batuhan.interv.util.DialogAction
import com.batuhan.interv.util.DialogData
import com.batuhan.interv.util.DialogType
import com.batuhan.interv.util.isTablet
import com.batuhan.interv.util.keyboardAsState

@Composable
fun CreateInterviewScreen(
    onBackPressed: () -> Unit,
    showDialog: (DialogData) -> Unit = {},
    clearDialog: () -> Unit = {},
    addStep: (interviewId: Long, language: String) -> Unit,
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
    addStep: (interviewId: Long, language: String) -> Unit,
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
    addStep: (interviewId: Long, language: String) -> Unit,
) {
    val dialogData by remember(uiState.dialogData) {
        derivedStateOf { uiState.dialogData }
    }
    BaseView(dialogData = dialogData) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.create_interview_title),
                            fontFamily = fontFamily,
                        )
                    },
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
    addStep: (interviewId: Long, language: String) -> Unit,
) {
    val interviewName by remember(uiState.currentInterview.interviewName) {
        derivedStateOf { uiState.currentInterview.interviewName }
    }
    val langCode by remember(uiState.currentInterview.langCode) {
        derivedStateOf { uiState.currentInterview.langCode }
    }
    val interviewType by remember(uiState.currentInterview.interviewType) {
        derivedStateOf { uiState.currentInterview.interviewType }
    }
    val tabRows = listOf(R.string.type_video, R.string.type_phone_call)
    val selectedIndexType by remember(interviewType) {
        mutableStateOf(interviewType?.text?.let { tabRows.indexOf(it) } ?: 0)
    }
    val selectedIndexLang by remember(langCode) {
        mutableStateOf(findType(langCode))
    }

    val focusManager = LocalFocusManager.current
    val isKeyboardOpen by keyboardAsState()
    LaunchedEffect(isKeyboardOpen) {
        if (!isKeyboardOpen) focusManager.clearFocus()
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
        TabRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 16.dp),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            selectedTabIndex = selectedIndexType,
            divider = {},
            indicator = {
                if (selectedIndexType < it.size) {
                    Column(
                        modifier =
                            Modifier.tabIndicatorOffset(it[selectedIndexType]).fillMaxSize()
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.onSurface,
                                    RoundedCornerShape(10.dp),
                                ),
                    ) {
                    }
                }
            },
        ) {
            Tab(
                modifier = Modifier.height(48.dp).padding(12.dp),
                selected = selectedIndexType == 0,
                onClick = { updateConfiguration.invoke(InterviewField.Type(InterviewType.VIDEO)) },
            ) {
                Text(stringResource(id = InterviewType.VIDEO.text), textAlign = TextAlign.Center)
            }
            Tab(
                modifier = Modifier.height(48.dp).padding(12.dp),
                selected = selectedIndexType == 1,
                onClick = { updateConfiguration.invoke(InterviewField.Type(InterviewType.PHONE_CALL)) },
            ) {
                Text(stringResource(id = InterviewType.PHONE_CALL.text), textAlign = TextAlign.Center)
            }
        }
        ScrollableTabRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 16.dp),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            selectedTabIndex = selectedIndexLang,
            divider = {},
            edgePadding = 0.dp,
            indicator = {
                if (selectedIndexLang < it.size) {
                    Column(
                        modifier =
                            Modifier.tabIndicatorOffset(it[selectedIndexLang]).fillMaxSize()
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.onSurface,
                                    RoundedCornerShape(10.dp),
                                ),
                    ) {
                    }
                }
            },
        ) {
            Tab(
                modifier = Modifier.height(48.dp).padding(12.dp),
                selected = selectedIndexLang == 0,
                onClick = { updateConfiguration.invoke(InterviewField.Language(LanguageType.EN.code)) },
            ) {
                Text(stringResource(id = LanguageType.EN.text))
            }
            Tab(
                modifier = Modifier.height(48.dp).padding(12.dp),
                selected = selectedIndexLang == 1,
                onClick = { updateConfiguration.invoke(InterviewField.Language(LanguageType.TR.code)) },
            ) {
                Text(stringResource(id = LanguageType.TR.text))
            }
            Tab(
                modifier = Modifier.height(48.dp).padding(12.dp),
                selected = selectedIndexLang == 2,
                onClick = { updateConfiguration.invoke(InterviewField.Language(LanguageType.FR.code)) },
            ) {
                Text(stringResource(id = LanguageType.FR.text))
            }
            Tab(
                modifier = Modifier.height(48.dp).padding(12.dp),
                selected = selectedIndexLang == 3,
                onClick = { updateConfiguration.invoke(InterviewField.Language(LanguageType.DE.code)) },
            ) {
                Text(stringResource(id = LanguageType.DE.text))
            }
            Tab(
                modifier = Modifier.height(48.dp).padding(12.dp),
                selected = selectedIndexLang == 4,
                onClick = { updateConfiguration.invoke(InterviewField.Language(LanguageType.ES.code)) },
            ) {
                Text(stringResource(id = LanguageType.ES.text))
            }
            Tab(
                modifier = Modifier.height(48.dp).padding(12.dp),
                selected = selectedIndexLang == 5,
                onClick = { updateConfiguration.invoke(InterviewField.Language(LanguageType.PL.code)) },
            ) {
                Text(stringResource(id = LanguageType.PL.text))
            }
            Tab(
                modifier = Modifier.height(48.dp).padding(12.dp),
                selected = selectedIndexLang == 6,
                onClick = { updateConfiguration.invoke(InterviewField.Language(LanguageType.AR.code)) },
            ) {
                Text(stringResource(id = LanguageType.AR.text))
            }
            Tab(
                modifier = Modifier.height(48.dp).padding(12.dp),
                selected = selectedIndexLang == 7,
                onClick = { updateConfiguration.invoke(InterviewField.Language(LanguageType.IT.code)) },
            ) {
                Text(stringResource(id = LanguageType.IT.text))
            }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = ButtonDefaults.TextButtonContentPadding,
            onClick = {
                addStep.invoke(
                    uiState.currentInterview.interviewId ?: return@Button,
                    uiState.currentInterview.langCode ?: return@Button,
                )
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
                stringResource(id = R.string.create_interview_add_step_title),
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
            addStep = {_, _ ->},
        )
    }
}
