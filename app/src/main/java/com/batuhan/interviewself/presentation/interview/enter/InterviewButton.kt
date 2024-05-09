package com.batuhan.interviewself.presentation.interview.enter

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun InterviewButton(
    @StringRes title: Int,
    @DrawableRes icon: Int,
    sendEvent: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(8.dp).clickable(role = Role.Button) {
                sendEvent.invoke()
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(painterResource(id = icon), contentDescription = null)
        Text(stringResource(id = title), textAlign = TextAlign.Center)
    }
}