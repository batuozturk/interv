package com.batuhan.interviewself.presentation.interview.enter.phone

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batuhan.interviewself.R
import com.batuhan.interviewself.presentation.interview.enter.InterviewEvent
import com.batuhan.interviewself.presentation.interview.enter.InterviewUiState
import com.batuhan.interviewself.ui.theme.InterviewselfTheme

@Composable
fun PhoneInterviewScreen(uiState: InterviewUiState, sendEvent: (InterviewEvent) -> Unit) {
    val isMicrophoneEnabled by remember(uiState.isMicrophoneEnabled){
        derivedStateOf { uiState.isMicrophoneEnabled }
    }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(8.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(4f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("HR Manager", fontSize = 25.sp)
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(4f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LazyVerticalGrid(userScrollEnabled = false, columns = GridCells.Fixed(3)) {
                item {
                    PhoneInterviewButton(
                        title = if(isMicrophoneEnabled) R.string.microphone_title_enabled else R.string.microphone_title_not_enabled,
                        if(isMicrophoneEnabled) R.drawable.ic_mic_none_24 else R.drawable.ic_mic_off_24,
                    ) {
                        sendEvent.invoke(InterviewEvent.MicrophoneState(!isMicrophoneEnabled))
                    }
                }
                item {
                    PhoneInterviewButton(
                        title = R.string.pause,
                        icon = R.drawable.ic_phone_paused_24,
                    ) {
                        sendEvent.invoke(InterviewEvent.Back)
                    }
                }
                item {
                    PhoneInterviewButton(
                        title = R.string.close_call,
                        icon = R.drawable.ic_call_end_24,
                    ) {
                        sendEvent.invoke(InterviewEvent.Back)
                    }
                }
            }

        }
    }
}

@Composable
fun PhoneInterviewButton(
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

@Preview
@Composable
fun PhoneInterviewScreenPreview() {
    InterviewselfTheme {
        PhoneInterviewScreen(uiState = InterviewUiState(), {})
    }
}
