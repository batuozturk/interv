package com.batuhan.interviewself.presentation.interview.enter.phone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batuhan.interviewself.R
import com.batuhan.interviewself.presentation.interview.enter.InterviewButton
import com.batuhan.interviewself.presentation.interview.enter.InterviewEvent
import com.batuhan.interviewself.presentation.interview.enter.InterviewUiState
import com.batuhan.interviewself.ui.theme.InterviewselfTheme

@Composable
fun PhoneInterviewScreen(
    uiState: InterviewUiState,
    sendEvent: (InterviewEvent) -> Unit,
) {
    val isMicrophoneEnabled by remember(uiState.isMicrophoneEnabled) {
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
            InterviewButton(
                title = R.string.close_call,
                icon = R.drawable.ic_call_end_24,
            ) {
                sendEvent.invoke(InterviewEvent.Back)
            }
        }
    }
}

@Preview
@Composable
fun PhoneInterviewScreenPreview() {
    InterviewselfTheme {
        PhoneInterviewScreen(uiState = InterviewUiState(), {})
    }
}
