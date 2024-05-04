package com.batuhan.interviewself.util

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batuhan.interviewself.R
import com.batuhan.interviewself.ui.theme.InterviewselfTheme
import com.batuhan.interviewself.ui.theme.fontFamily

@Composable
fun DialogView(dialogData: DialogData?) {
    dialogData?.let {
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
            dialogData.options?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    it.map {
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

@Preview
@Composable
fun DialogViewPreview() {
    InterviewselfTheme {
        DialogView(
            DialogData(
                R.string.error_question_not_saved,
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
