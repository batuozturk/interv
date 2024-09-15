package com.batuhan.interv.presentation.question

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batuhan.interv.R
import com.batuhan.interv.data.model.LanguageType
import com.batuhan.interv.data.model.findType
import com.batuhan.interv.ui.theme.InterviewselfTheme
import com.batuhan.interv.ui.theme.fontFamily
import com.batuhan.interv.util.keyboardAsState

@Composable
fun CreateQuestionView(
    isTablet: Boolean,
    questionText: String?,
    langCode: String?,
    createQuestion: () -> Unit,
    updateQuestionText: (String) -> Unit,
    updateLangCode: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (isTablet) {
        BackHandler {
            onDismiss.invoke()
        }
    }

    val selectedIndexLang by remember(langCode) {
        mutableStateOf(findType(langCode))
    }

    val focusManager = LocalFocusManager.current
    val isKeyboardOpen by keyboardAsState()
    LaunchedEffect(isKeyboardOpen) {
        if(!isKeyboardOpen) focusManager.clearFocus()
    }
    Column(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(stringResource(id = R.string.create_question_name_placeholder))
                },
                leadingIcon = {
                    Icon(
                        painterResource(id = R.drawable.ic_question_mark_24),
                        contentDescription = null,
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(),
                value = questionText ?: "",
                onValueChange = {
                    updateQuestionText.invoke(it)
                },
                singleLine = true,
            )
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
                onClick = { updateLangCode(LanguageType.EN.code) },
            ) {
                Text(stringResource(id = LanguageType.EN.text))
            }
            Tab(
                modifier = Modifier.height(48.dp).padding(12.dp),
                selected = selectedIndexLang == 1,
                onClick = { updateLangCode(LanguageType.TR.code) },
            ) {
                Text(stringResource(id = LanguageType.TR.text))
            }
            Tab(
                modifier = Modifier.height(48.dp).padding(12.dp),
                selected = selectedIndexLang == 2,
                onClick = { updateLangCode(LanguageType.FR.code) },
            ) {
                Text(stringResource(id = LanguageType.FR.text))
            }
            Tab(
                modifier = Modifier.height(48.dp).padding(12.dp),
                selected = selectedIndexLang == 3,
                onClick = { updateLangCode(LanguageType.DE.code) },
            ) {
                Text(stringResource(id = LanguageType.DE.text))
            }
            Tab(
                modifier = Modifier.height(48.dp).padding(12.dp),
                selected = selectedIndexLang == 4,
                onClick = { updateLangCode(LanguageType.ES.code) },
            ) {
                Text(stringResource(id = LanguageType.ES.text))
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
                onClick = onDismiss,
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
                onClick = createQuestion,
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

@Preview
@Composable
fun CreateQuestionPreview() {
    InterviewselfTheme {
        CreateQuestionView(
            isTablet = false,
            questionText = null,
            langCode = null,
            createQuestion = { /*TODO*/ },
            updateQuestionText = {},
            updateLangCode = {},
            {},
        )
    }
}
