package com.batuhan.interviewself.util

import android.media.MediaPlayer
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.batuhan.interviewself.R
import com.batuhan.interviewself.ui.theme.InterviewselfTheme
import com.batuhan.interviewself.ui.theme.fontFamily

@Composable
fun EnterInterviewDialogView(data: EnterInterviewDialogData?) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mediaPlayer by remember {
        mutableStateOf(MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI))
    }
    DisposableEffect(key1 = lifecycleOwner) {
        val lifecycleEventObserver =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_DESTROY) {
                    mediaPlayer.pause()
                    mediaPlayer.release()
                } else if (event == Lifecycle.Event.ON_RESUME) {
                    mediaPlayer.start()
                } else if (event == Lifecycle.Event.ON_PAUSE) {
                    mediaPlayer.pause()
                }
            }
        lifecycleOwner.lifecycle.addObserver(lifecycleEventObserver)

        onDispose {
            mediaPlayer.pause()
            mediaPlayer.release()
            lifecycleOwner.lifecycle.removeObserver(lifecycleEventObserver)
        }
    }

    data?.let {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                stringResource(R.string.hr_manager_calling_text),
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                textAlign = TextAlign.Center,
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = data.action1,
                    modifier = Modifier.weight(1f).background(Color(0xFF4F6F52)),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F6F52),
                            contentColor = Color.White,
                        ),
                ) {
                    Text(stringResource(R.string.open_call), fontSize = 20.sp, fontFamily = fontFamily)
                }
                Button(
                    onClick = data.action2,
                    shape = ButtonDefaults.textShape,
                    modifier = Modifier.weight(1f).background(Color(0xFFA0153E)),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFA0153E),
                            contentColor = Color.White,
                        ),
                ) {
                    Text(
                        stringResource(id = R.string.dismiss),
                        fontSize = 20.sp,
                        fontFamily = fontFamily,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun EnterInterviewDialogViewPreview() {
    InterviewselfTheme {
        EnterInterviewDialogView(
            EnterInterviewDialogData(
                1L,
                {},
                {},
            ),
        )
    }
}
