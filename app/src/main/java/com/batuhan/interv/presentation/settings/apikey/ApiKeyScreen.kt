package com.batuhan.interv.presentation.settings.apikey

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import com.batuhan.interv.R
import com.batuhan.interv.presentation.settings.exportquestions.ExportQuestionsEvent
import com.batuhan.interv.ui.theme.fontFamily
import com.batuhan.interv.util.ApiKeyData
import com.batuhan.interv.util.DialogAction
import com.batuhan.interv.util.DialogInputAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyScreen(
    apiKeyData: ApiKeyData,
    inputAction: List<DialogInputAction>,
    onBackPressed: () -> Unit
) {
    BackHandler {
        onBackPressed.invoke()
    }
    var apiKey by remember {
        mutableStateOf(apiKeyData.apiKey)
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
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth().padding(it)
                .padding(8.dp),
        ) {
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
            Spacer(Modifier.height(8.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
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
    }


}