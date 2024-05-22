package com.batuhan.interv.presentation.settings

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batuhan.interv.R
import com.batuhan.interv.presentation.settings.detail.SettingsDetailScreen
import com.batuhan.interv.presentation.settings.exportquestions.ExportQuestionsScreen
import com.batuhan.interv.presentation.settings.importquestions.ImportQuestionsScreen
import com.batuhan.interv.util.BrowserEvent
import com.batuhan.interv.util.DialogAction
import com.batuhan.interv.util.DialogData
import com.batuhan.interv.util.SettingsDetailAction
import com.batuhan.interv.util.dataStore
import com.batuhan.interv.util.decideDialogType
import com.batuhan.interv.util.isTablet
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SettingsScreen(
    exportQuestions: () -> Unit,
    importQuestions: () -> Unit,
    restartApplication: () -> Unit,
    setStyle: (isDarkMode: Boolean) -> Unit,
    sendBrowserEvent: (BrowserEvent) -> Unit,
    showDialog: (DialogData) -> Unit,
    clearDialog: () -> Unit,
) {
    val context = LocalContext.current
    val datastore = context.dataStore
    val viewModel = hiltViewModel<SettingsViewModel>()

    val isTablet by remember(context.isTablet()) {
        derivedStateOf { context.isTablet() }
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogData by remember(uiState.dialogData) {
        derivedStateOf { uiState.dialogData }
    }
    LaunchedEffect(dialogData) {
        dialogData?.let(showDialog)
    }

    LaunchedEffect(true) {
        viewModel.event.collect {
            when (it) {
                is SettingsEvent.ChangeStyle -> setStyle.invoke(it.isDarkMode)
                SettingsEvent.ClearDialog -> clearDialog.invoke()
                SettingsEvent.RestartApplication -> restartApplication()
                SettingsEvent.ExportQuestions -> exportQuestions.invoke()
                SettingsEvent.ImportQuestions -> importQuestions.invoke()
            }
        }
    }

    LaunchedEffect(true) {
        context.dataStore.data.collect { data ->
            val isDarkMode =
                data[SettingsViewModel.KEY_PREFERENCES_STYLE] ?: run {
                    datastore.writeData(SettingsType.Style(true), viewModel::writeData)
                    true
                }
            val langCode =
                data[SettingsViewModel.KEY_PREFERENCES_LANGUAGE] ?: run {
                    datastore.writeData(
                        SettingsType.LangCode(Locale.US.language + "-" + Locale.US.country),
                        viewModel::writeData,
                    )
                    Locale.US.language + "-" + Locale.US.country
                }
            viewModel.readData(isDarkMode, langCode)
        }
    }

    val darkTheme by remember(uiState.isDarkMode) {
        derivedStateOf { uiState.isDarkMode }
    }

    val coroutineScope = rememberCoroutineScope()

    SettingsScreenContent(
        isTablet = isTablet,
        exportQuestions = {
            viewModel.sendEvent(SettingsEvent.ExportQuestions)
        },
        importQuestions = {
            viewModel.sendEvent(SettingsEvent.ImportQuestions)
        },
        language = {
            viewModel.showDialog(
                DialogData(
                    R.string.settings_language_title,
                    actions =
                        listOf(
                            DialogAction(R.string.dismiss, viewModel::clearDialog),
                        ),
                    listOf(
                        DialogAction(
                            R.string.settings_lang_option_one,
                        ) {
                            coroutineScope.launch {
                                datastore.writeData(
                                    SettingsType.LangCode("en-US"),
                                    viewModel::writeData,
                                )
                            }
                        },
                        DialogAction(
                            R.string.settings_lang_option_two,
                        ) {
                            coroutineScope.launch {
                                datastore.writeData(
                                    SettingsType.LangCode("tr-TR"),
                                    viewModel::writeData,
                                )
                            }
                        },
                        DialogAction(
                            R.string.settings_lang_option_three,
                        ) {
                            coroutineScope.launch {
                                datastore.writeData(
                                    SettingsType.LangCode("fr-FR"),
                                    viewModel::writeData,
                                )
                            }
                        },
                    ),
                    decideDialogType(darkTheme),
                ),
            )
        },
        setStyle = {
            viewModel.showDialog(
                DialogData(
                    R.string.settings_style_title,
                    actions =
                        listOf(
                            DialogAction(R.string.dismiss, clearDialog),
                        ),
                    listOf(
                        DialogAction(R.string.settings_style_option_one) {
                            coroutineScope.launch {
                                context.dataStore.writeData(SettingsType.Style(false)){
                                    viewModel.writeData(
                                        SettingsType.Style(false),
                                    )
                                }
                            }
                        },
                        DialogAction(R.string.settings_style_option_two) {
                            coroutineScope.launch {
                                context.dataStore.writeData(SettingsType.Style(true)){
                                    viewModel.writeData(
                                        SettingsType.Style(true),
                                    )
                                }
                            }
                        },
                    ),
                    decideDialogType(darkTheme),
                ),
            )
        },
        sendBrowserEvent = sendBrowserEvent,
        showDialog = showDialog,
        clearDialog = clearDialog,
        writeData = {
            coroutineScope.launch {
                datastore.writeData(it){
                    viewModel.writeData(it)
                }
            }
        }
    )
}

@Composable
fun SettingsScreenContent(
    isTablet: Boolean,
    exportQuestions: () -> Unit,
    importQuestions: () -> Unit,
    language: () -> Unit,
    setStyle: () -> Unit,
    sendBrowserEvent: (BrowserEvent) -> Unit,
    showDialog: (DialogData) -> Unit,
    clearDialog: () -> Unit,
    writeData: (SettingsType) -> Unit
) {
    var detailType: SettingsDetailAction? by remember {
        mutableStateOf(null)
    }

    var exportQuestionsOpened: Boolean by remember {
        mutableStateOf(false)
    }
    var importQuestionsOpened: Boolean by remember {
        mutableStateOf(false)
    }
    val weight by animateFloatAsState(
        targetValue = if (detailType != null || exportQuestionsOpened || importQuestionsOpened) 3f else 0.001f,
        animationSpec =
            tween(
                1000,
            ),
    )
    Row(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            modifier = Modifier.weight(9f - weight),
            columns = GridCells.Fixed(if (isTablet) 2 else 1),
        ) {
            item {
                SettingsListItem(title = R.string.settings_style) {
                    if (isTablet) {
                        exportQuestionsOpened = false
                        detailType =
                            SettingsDetailAction.Style(
                                listOf(
                                    DialogAction(R.string.settings_style_option_one) {
                                        writeData.invoke(
                                            SettingsType.Style(false)
                                        )
                                    },
                                    DialogAction(R.string.settings_style_option_two) {
                                        writeData.invoke(
                                            SettingsType.Style(true)
                                        )
                                    },
                                ),
                            )
                    } else {
                        setStyle.invoke()
                    }
                }
            }
            item {
                SettingsListItem(title = R.string.settings_language) {
                    if (isTablet) {
                        exportQuestionsOpened = false
                        detailType =
                            SettingsDetailAction.Language(
                                listOf(
                                    DialogAction(R.string.settings_lang_option_one) {
                                        writeData.invoke(
                                            SettingsType.LangCode("en-US")
                                        )
                                    },
                                    DialogAction(R.string.settings_lang_option_two) {
                                        writeData.invoke(
                                            SettingsType.LangCode("tr-TR")
                                        )
                                    },
                                    DialogAction(R.string.settings_lang_option_three) {
                                        writeData.invoke(
                                            SettingsType.LangCode("fr-FR")
                                        )
                                    },
                                ),
                            )
                    } else {
                        language.invoke()
                    }
                }
            }
            item {
                SettingsListItem(title = R.string.settings_export_questions) {
                    if (isTablet) {
                        clearDialog.invoke()
                        exportQuestionsOpened = true
                        importQuestionsOpened = false
                        detailType = null
                    } else {
                        exportQuestions.invoke()
                    }
                }
            }
            item {
                SettingsListItem(title = R.string.settings_import_questions) {
                    if (isTablet) {
                        clearDialog.invoke()
                        importQuestionsOpened = true
                        exportQuestionsOpened = false
                        detailType = null
                    } else {
                        importQuestions.invoke()
                    }
                }
            }
            item {
                SettingsListItem(title = R.string.settings_rate_us) {
                    sendBrowserEvent(BrowserEvent.RateUs)
                }
            }
            item {
                SettingsListItem(title = R.string.settings_download_konsol) {
                    sendBrowserEvent(BrowserEvent.DownloadKonsol)
                }
            }
            item {
                SettingsListItem(title = R.string.settings_contact_us) {
                    sendBrowserEvent(BrowserEvent.ContactUs)
                }
            }
            item {
                SettingsListItem(title = R.string.settings_tos) {
                    sendBrowserEvent(BrowserEvent.TermsOfService)
                }
            }
            item {
                SettingsListItem(title = R.string.settings_privacy_policy) {
                    sendBrowserEvent(BrowserEvent.PrivacyPolicy)
                }
            }
        }
        Column(Modifier.weight(weight).fillMaxSize()) {
            if (weight > 2.25 && detailType != null) {
                SettingsDetailScreen(
                    onBackPressed = { detailType = null },
                    title = detailType!!.title,
                    actions = detailType!!.actions,
                )
            } else if (weight > 2.25 && exportQuestionsOpened) {
                ExportQuestionsScreen(onBackPressed = {
                    clearDialog.invoke()
                    exportQuestionsOpened = false }, showDialog = showDialog, clearDialog = clearDialog)
            }
            else if (weight > 2.25 && importQuestionsOpened) {
                ImportQuestionsScreen(onBackPressed = {
                    clearDialog.invoke()
                    importQuestionsOpened = false }, showDialog = showDialog, clearDialog = clearDialog)
            }
        }
    }
}

@Composable
fun SettingsListItem(
    @StringRes title: Int,
    action: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier =
            Modifier
                .fillMaxWidth().height(100.dp).padding(8.dp)
                .clickable { action.invoke() }
                .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(10.dp))
                .padding(10.dp),
    ) {
        Text(stringResource(id = title))
    }
}

suspend fun DataStore<Preferences>.writeData(
    settingsType: SettingsType,
    afterCompletion: (SettingsType) -> Unit,
) {
    edit { prefs ->
        when (settingsType) {
            is SettingsType.Style ->
                prefs[SettingsViewModel.KEY_PREFERENCES_STYLE] =
                    settingsType.isDarkMode

            is SettingsType.LangCode ->
                prefs[SettingsViewModel.KEY_PREFERENCES_LANGUAGE] =
                    settingsType.langCode
        }
    }
    afterCompletion.invoke(settingsType)
}
