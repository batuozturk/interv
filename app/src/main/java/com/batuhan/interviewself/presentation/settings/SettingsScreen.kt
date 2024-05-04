package com.batuhan.interviewself.presentation.settings

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batuhan.interviewself.R
import com.batuhan.interviewself.presentation.settings.detail.SettingsDetailScreen
import com.batuhan.interviewself.util.BrowserEvent
import com.batuhan.interviewself.util.DialogAction
import com.batuhan.interviewself.util.DialogData
import com.batuhan.interviewself.util.SettingsDetailAction
import com.batuhan.interviewself.util.dataStore
import com.batuhan.interviewself.util.decideDialogType
import com.batuhan.interviewself.util.isTablet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    exportQuestions: () -> Unit,
    importQuestions: () -> Unit,
    language: () -> Unit,
    setStyle: () -> Unit,
    sendBrowserEvent: (BrowserEvent) -> Unit,
    showDialog: (DialogData) -> Unit,
    clearDialog: () -> Unit,
) {
    val context = LocalContext.current

    // TODO datastore
    val dataStore by context.dataStore.data.collectAsStateWithLifecycle(initialValue = emptyFlow<Preferences>())

    val isTablet by remember(context.isTablet()) {
        derivedStateOf { context.isTablet() }
    }

    val darkTheme = isSystemInDarkTheme()

    val coroutineScope = rememberCoroutineScope()

    SettingsScreenContent(
        isTablet = isTablet,
        exportQuestions = { },
        importQuestions = { },
        language = {
            coroutineScope.launch {
                clearDialog.invoke()
                delay(500L)
                showDialog(
                    DialogData(
                        R.string.settings_language_title,
                        actions =
                            listOf(
                                DialogAction(R.string.dismiss, clearDialog),
                            ),
                        listOf(
                            DialogAction(R.string.settings_lang_option_one, {}),
                            DialogAction(R.string.settings_lang_option_two, {}),
                            DialogAction(R.string.settings_lang_option_three, {}),
                        ),
                        decideDialogType(darkTheme),
                    ),
                )
            }
        },
        setStyle = {
            coroutineScope.launch {
                clearDialog.invoke()
                delay(500L)
                showDialog(
                    DialogData(
                        R.string.settings_style_title,
                        actions =
                            listOf(
                                DialogAction(R.string.dismiss, clearDialog),
                            ),
                        listOf(
                            DialogAction(R.string.settings_style_option_one, {}),
                            DialogAction(R.string.settings_style_option_two, {}),
                        ),
                        decideDialogType(darkTheme),
                    ),
                )
            }
        },
        sendBrowserEvent = sendBrowserEvent,
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
) {
    var detailType: SettingsDetailAction? by remember {
        mutableStateOf(null)
    }
    val weight by animateFloatAsState(
        targetValue = if (detailType != null) 3f else 0.001f,
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
                        detailType =
                            SettingsDetailAction.Style(
                                listOf(
                                    DialogAction(R.string.settings_style_option_one, {}),
                                    DialogAction(R.string.settings_style_option_two, {}),
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
                        detailType =
                            SettingsDetailAction.Language(
                                listOf(
                                    DialogAction(R.string.settings_lang_option_one, {}),
                                    DialogAction(R.string.settings_lang_option_two, {}),
                                    DialogAction(R.string.settings_lang_option_three, {}),
                                ),
                            )
                    } else {
                        language.invoke()
                    }
                }
            }
            item {
                SettingsListItem(title = R.string.settings_export_questions, exportQuestions)
            }
            item {
                SettingsListItem(title = R.string.settings_import_questions, importQuestions)
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
