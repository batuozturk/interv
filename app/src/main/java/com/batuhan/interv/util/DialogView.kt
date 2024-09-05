package com.batuhan.interv.util

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batuhan.interv.R
import com.batuhan.interv.ui.theme.InterviewselfTheme
import com.batuhan.interv.ui.theme.fontFamily

@Composable
fun DialogView(dialogData: DialogData?) {
    dialogData?.let {
        it.takeIf { it.type == DialogType.DIALOG_DARK || it.type == DialogType.DIALOG_LIGHT }?.let {
            if (it.inputActions != null) {
                InputDialog(
                    dialogData.title,
                    dialogData.actions,
                    dialogData.apiKeyData!!,
                    dialogData.inputActions!!,
                )
            } else {
                SettingsDialog(
                    dialogData.title,
                    dialogData.actions,
                    dialogData.options!!,
                    dialogData.languageData,
                    dialogData.styleData,
                )
            }
        } ?: run {
            // TODO remove column or row and set weights
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(dialogData.type.containerColor)
                        .padding(8.dp),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        stringResource(id = dialogData.title),
                        color = dialogData.type.textColor,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                    )
                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        dialogData.actions.map {
                            Button(
                                contentPadding = ButtonDefaults.TextButtonContentPadding,
                                onClick = it.action,
                                colors =
                                    ButtonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = dialogData.type.textColor,
                                        disabledContainerColor = Color.Transparent,
                                        disabledContentColor = dialogData.type.textColor,
                                    ),
                            ) {
                                Text(
                                    stringResource(id = it.text),
                                    fontFamily = fontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = dialogData.type.textColor,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    @StringRes title: Int,
    actions: List<DialogAction>,
    options: List<DialogAction>,
    languageData: LanguageData?,
    styleData: StyleData?,
) {
    val selectedIndex =
        remember {
            languageData?.let { data ->
                data.selectedLanguageIndex
            } ?: run {
                if (styleData?.isDarkMode == true) {
                    1
                } else {
                    0
                }
            }
        }
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(stringResource(id = title), modifier = Modifier.weight(6f))
            Button(
                modifier = Modifier.weight(2f),
                contentPadding = ButtonDefaults.TextButtonContentPadding,
                onClick = actions[0].action,
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
                )
            }
        }
        TabRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            selectedTabIndex = selectedIndex,
            divider = {},
            indicator = {
                if (selectedIndex < options.size) {
                    Column(
                        modifier =
                            Modifier
                                .tabIndicatorOffset(it[selectedIndex])
                                .fillMaxSize()
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
            options.forEachIndexed { i, x ->
                Tab(
                    modifier =
                        Modifier
                            .height(48.dp)
                            .padding(12.dp)
                            .fillMaxWidth(),
                    selected = selectedIndex == i,
                    onClick = x.action,
                ) {
                    Text(stringResource(id = x.text))
                }
            }
        }
    }
}

@Composable
fun InputDialog(
    @StringRes title: Int,
    actions: List<DialogAction>,
    apiKeyData: ApiKeyData,
    inputAction: List<DialogInputAction>,
) {
    var apiKey by remember {
        mutableStateOf(apiKeyData.apiKey)
    }
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(stringResource(id = title), modifier = Modifier.weight(4f))
            Button(
                modifier = Modifier.weight(2f),
                contentPadding = ButtonDefaults.TextButtonContentPadding,
                onClick = actions[0].action,
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
                )
            }
            Button(
                modifier = Modifier.weight(2f),
                contentPadding = ButtonDefaults.TextButtonContentPadding,
                onClick = {
                    inputAction[0].action.invoke(apiKey)
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
                )
            }
        }
        OutlinedTextField(
            value = apiKey,
            onValueChange = {
                apiKey = it
            },
            colors = OutlinedTextFieldDefaults.colors(),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
            placeholder = {
                Text(stringResource(R.string.api_key_placeholder))
            },
            singleLine = true,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.api_key_info),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
fun DialogViewPreview() {
    InterviewselfTheme {
        DialogView(
            DialogData(
                R.string.filter,
                actions =
                    listOf(
                        DialogAction(R.string.dismiss, {}),
                    ),
                listOf(
                    DialogAction(R.string.settings_lang_option_one, {}),
                    DialogAction(R.string.settings_lang_option_two, {}),
                    DialogAction(R.string.settings_lang_option_three, {}),
                ),
                decideDialogType(isSystemInDarkTheme()),
            ),
        )
    }
}
