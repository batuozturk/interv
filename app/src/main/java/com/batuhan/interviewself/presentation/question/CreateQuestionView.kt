package com.batuhan.interviewself.presentation.question

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batuhan.interviewself.R
import com.batuhan.interviewself.ui.theme.InterviewselfTheme
import com.batuhan.interviewself.ui.theme.fontFamily

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
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(stringResource(id = R.string.lang_code_placeholder))
                },
                leadingIcon = {
                    Icon(painterResource(id = R.drawable.ic_language_24), contentDescription = null)
                },
                colors = OutlinedTextFieldDefaults.colors(),
                value = langCode ?: "",
                onValueChange = {
                    updateLangCode.invoke(it)
                },
                singleLine = true,
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
