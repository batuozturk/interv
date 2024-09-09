package com.batuhan.interv.presentation.settings.importquestions

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.batuhan.interv.ui.theme.InterviewselfTheme
import com.batuhan.interv.ui.theme.fontFamily
import com.batuhan.interv.util.BaseView
import com.batuhan.interv.util.DialogData
import com.batuhan.interv.util.isTablet

@Composable
fun ImportQuestionsScreen(
    onBackPressed: () -> Unit,
    showDialog: (DialogData) -> Unit = {},
    clearDialog: () -> Unit = {},
) {
    val viewModel = hiltViewModel<ImportQuestionsViewModel>()
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

    val startActivityForResult =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) {
            it?.let {
                val contentResolver = context.contentResolver
                viewModel.importQuestions(contentResolver, it)
            }
        }

    LaunchedEffect(true) {
        viewModel.event.collect {
            when (it) {
                ImportQuestionsEvent.Back -> onBackPressed.invoke()
                ImportQuestionsEvent.ClearDialog -> clearDialog.invoke()
                ImportQuestionsEvent.ImportQuestions -> {
                    startActivityForResult.launch(arrayOf("application/json"))
                }
            }
        }
    }
    if (isTablet) {
        ImportQuestionsScreenContent(
            viewModel::sendEvent,
        )
    } else {
        BaseView(dialogData) {
            ImportQuestionsScreenContent(
                viewModel::sendEvent,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportQuestionsScreenContent(sendEvent: (ImportQuestionsEvent) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings_import_questions),
                        fontFamily = fontFamily,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { sendEvent.invoke(ImportQuestionsEvent.Back) }) {
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
                text = stringResource(R.string.import_questions_info),
            )
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
                        sendEvent.invoke(ImportQuestionsEvent.ImportQuestions)
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
                        stringResource(id = R.string.settings_import_questions),
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
fun ImportQuestionsPreview() {
    InterviewselfTheme {
        ImportQuestionsScreenContent(sendEvent = {})
    }
}
