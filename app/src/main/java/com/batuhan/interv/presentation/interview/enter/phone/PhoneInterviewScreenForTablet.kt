package com.batuhan.interv.presentation.interview.enter.phone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batuhan.interv.R
import com.batuhan.interv.presentation.interview.enter.InterviewButton
import com.batuhan.interv.presentation.interview.enter.InterviewEvent
import com.batuhan.interv.presentation.interview.enter.InterviewUiState
import com.batuhan.interv.ui.theme.InterviewselfTheme

@Composable
fun PhoneInterviewScreenForTablet(
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
            LazyHorizontalGrid(userScrollEnabled = false, rows = GridCells.Fixed(3)) {
                item {
                    InterviewButton(
                        title = R.string.create_interview_title,
                        if (isMicrophoneEnabled) R.drawable.ic_mic_none_24 else R.drawable.ic_mic_off_24,
                    ) {
                        sendEvent.invoke(InterviewEvent.MicrophoneState(!isMicrophoneEnabled))
                    }
                }
                item {
                    InterviewButton(
                        title = R.string.pause,
                        icon = R.drawable.ic_phone_paused_24,
                    ) {
                        sendEvent.invoke(InterviewEvent.Back)
                    }
                }
                item {
                    InterviewButton(
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

@Preview(device = Devices.TABLET)
@Composable
fun PhoneInterviewScreenForTabletPreview() {
    InterviewselfTheme {
        PhoneInterviewScreen(uiState = InterviewUiState(), {})
    }
}
