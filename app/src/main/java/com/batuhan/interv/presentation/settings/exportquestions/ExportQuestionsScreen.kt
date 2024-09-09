package com.batuhan.interv.presentation.settings.exportquestions

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batuhan.interv.R
import com.batuhan.interv.data.model.LanguageType
import com.batuhan.interv.ui.theme.InterviewselfTheme
import com.batuhan.interv.ui.theme.fontFamily
import com.batuhan.interv.util.BaseView
import com.batuhan.interv.util.DialogData
import com.batuhan.interv.util.isTablet

@Composable
fun ExportQuestionsScreen(
    onBackPressed: () -> Unit,
    showDialog: (DialogData) -> Unit = {},
    clearDialog: () -> Unit = {},
) {
    val viewModel = hiltViewModel<ExportQuestionsViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isTablet by remember(context.isTablet()) {
        derivedStateOf { context.isTablet() }
    }
    val dialogData by remember(uiState.dialogData) {
        derivedStateOf { uiState.dialogData }
    }
    if (isTablet) {
        LaunchedEffect(dialogData) {
            dialogData?.let(showDialog)
        }

        BackHandler {
            onBackPressed.invoke()
        }
    }

    var isSharing by remember {
        mutableStateOf(false)
    }

    val startActivityForResultSharing =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            viewModel.shareQuestions(it.resultCode == Activity.RESULT_OK)
        }

    val startActivityForResult =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument("application/json")) {
            it?.let {
                val contentResolver = context.contentResolver
                viewModel.exportQuestions(contentResolver, it, isSharing)
            }
        }

    LaunchedEffect(true) {
        viewModel.event.collect {
            when (it) {
                ExportQuestionsEvent.Back -> onBackPressed.invoke()
                ExportQuestionsEvent.ClearDialog -> clearDialog.invoke()
                is ExportQuestionsEvent.ShareQuestions -> {
                    val intent =
                        Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            putExtra(
                                Intent.EXTRA_SUBJECT,
                                "sharing file from interviewself",
                            )
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "sharing file which is exported as json",
                            )
                            putExtra(Intent.EXTRA_STREAM, it.uri)
                        }
                    startActivityForResultSharing.launch(
                        intent,
                    )
                }

                is ExportQuestionsEvent.ExportQuestions -> {
                    isSharing = it.isSharing
                    startActivityForResult.launch("questions-${uiState.selectedLanguageCode}.json")
                }
            }
        }
    }
    if (isTablet) {
        ExportQuestionsScreenContent(
            uiState,
            viewModel::sendEvent,
            viewModel::updateSelectedLanguage,
        )
    } else {
        BaseView(dialogData) {
            ExportQuestionsScreenContent(
                uiState,
                viewModel::sendEvent,
                viewModel::updateSelectedLanguage,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportQuestionsScreenContent(
    uiState: ExportQuestionsUiState,
    sendEvent: (ExportQuestionsEvent) -> Unit,
    updateSelectedLanguage: (String) -> Unit,
) {
    val currentLanguage by remember(uiState.selectedLanguageCode) {
        derivedStateOf { uiState.selectedLanguageCode }
    }
    val currentLanguageIndex by remember(currentLanguage) {
        derivedStateOf { LanguageType.entries.indexOfFirst { it.code == currentLanguage } }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings_export_questions),
                        fontFamily = fontFamily,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { sendEvent.invoke(ExportQuestionsEvent.Back) }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(it),
        ) {
            Text(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 16.dp),
                text = stringResource(R.string.export_questions_info),
            )
            TabRow(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(76.dp)
                        .padding(top = 12.dp, bottom = 16.dp),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                selectedTabIndex = currentLanguageIndex,
                divider = {},
                indicator = {
                    if (currentLanguageIndex < it.size) {
                        Column(
                            modifier =
                                Modifier
                                    .tabIndicatorOffset(it[currentLanguageIndex])
                                    .fillMaxSize()
                                    .padding(8.dp)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.onSurface,
                                        RoundedCornerShape(10.dp),
                                    )
                                    .padding(10.dp),
                        ) {
                        }
                    }
                },
            ) {
                Tab(
                    modifier = Modifier.height(60.dp),
                    selected = currentLanguageIndex == 0,
                    onClick = {
                        updateSelectedLanguage.invoke(LanguageType.EN.code)
                    },
                ) {
                    Text(stringResource(R.string.filter_english))
                }
                Tab(
                    modifier = Modifier.height(60.dp),
                    selected = currentLanguageIndex == 1,
                    onClick = {
                        updateSelectedLanguage.invoke(LanguageType.TR.code)
                    },
                ) {
                    Text(stringResource(R.string.filter_turkish))
                }
                Tab(
                    modifier = Modifier.height(60.dp),
                    selected = currentLanguageIndex == 2,
                    onClick = {
                        updateSelectedLanguage.invoke(LanguageType.FR.code)
                    },
                ) {
                    Text(stringResource(R.string.filter_french))
                }
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
                        sendEvent.invoke(ExportQuestionsEvent.ExportQuestions())
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
                        stringResource(id = R.string.save),
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                    )
                }
                Button(
                    modifier = Modifier.weight(0.6f),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    onClick = {
                        sendEvent.invoke(ExportQuestionsEvent.ExportQuestions(true))
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
                        stringResource(id = R.string.save_and_share),
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ExportQuestionPreivew() {
    InterviewselfTheme {
        ExportQuestionsScreenContent(uiState = ExportQuestionsUiState(), sendEvent = {}) {
//
        }
    }
}
