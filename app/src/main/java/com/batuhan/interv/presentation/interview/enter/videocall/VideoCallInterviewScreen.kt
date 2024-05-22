package com.batuhan.interv.presentation.interview.enter.videocall

import android.content.Context
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.batuhan.interv.R
import com.batuhan.interv.presentation.interview.enter.InterviewButton
import com.batuhan.interv.presentation.interview.enter.InterviewEvent
import com.batuhan.interv.presentation.interview.enter.InterviewUiState
import com.batuhan.interv.ui.theme.InterviewselfTheme
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
fun VideoCallInterviewScreen(
    uiState: InterviewUiState,
    sendEvent: (InterviewEvent) -> Unit,
) {
    val isMicrophoneEnabled by remember(uiState.isMicrophoneEnabled) {
        derivedStateOf { uiState.isMicrophoneEnabled }
    }

    val isVideoEnabled by remember(uiState.isVideoEnabled) {
        derivedStateOf { uiState.isVideoEnabled }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    val context = LocalContext.current

    val cameraProvider =
        remember {
            ProcessCameraProvider.getInstance(context)
        }

    val previewView =
        remember {
            PreviewView(context)
        }
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(8.dp),
    ) {
        Box(
            modifier =
            Modifier
                .fillMaxWidth()
                .weight(2f),
            contentAlignment = Alignment.Center,
        ) {
            Image(painter = painterResource(id = R.drawable.ic_person_2_24), contentDescription = null, modifier = Modifier.fillMaxSize())
            Text("HR Manager", fontSize = 18.sp, modifier = Modifier.align(Alignment.BottomStart))
        }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(4f),
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
                previewView.apply {
                    setBackgroundColor(Color.Transparent.toArgb())
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    post {
                        cameraProvider.addListener(
                            Runnable {
                                val cameraProvider = cameraProvider.get()
                                bindPreview(
                                    cameraProvider,
                                    lifecycleOwner,
                                    this,
                                )
                            },
                            ContextCompat.getMainExecutor(context),
                        )
                    }
                }
            })
            Column(
                modifier = Modifier.fillMaxWidth().height(80.dp).align(Alignment.BottomCenter).background(Color.Black.copy(0.2f)),
                verticalArrangement = Arrangement.SpaceEvenly,
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
}

@ComposePreview
@Composable
fun VideoCallInterviewPreview() {
    InterviewselfTheme {
        VideoCallInterviewScreen(uiState = InterviewUiState()) {
        }
    }
}

fun bindPreview(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
) {
    val preview: Preview =
        Preview.Builder()
            .build()

    val cameraSelector: CameraSelector =
        CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

    preview.setSurfaceProvider(previewView.surfaceProvider)

    var camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }
