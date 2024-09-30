package com.batuhan.interv.presentation.interview.enter

import android.media.MediaRecorder
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batuhan.interv.R
import com.batuhan.interv.data.model.InterviewType
import com.batuhan.interv.data.model.makeString
import com.batuhan.interv.data.model.makeTestString
import com.batuhan.interv.presentation.interview.enter.phone.PhoneInterviewScreen
import com.batuhan.interv.presentation.interview.enter.videocall.VideoCallInterviewScreen
import com.batuhan.interv.presentation.interview.enter.videocall.VideoCallInterviewScreenForTablet
import com.batuhan.interv.util.BaseView
import com.batuhan.interv.util.DialogAction
import com.batuhan.interv.util.DialogData
import com.batuhan.interv.util.DialogType
import com.batuhan.interv.util.isTablet
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.Locale

@Composable
fun InterviewScreen(
    onBackPressed: () -> Unit,
    interviewId: Long,
    interviewType: String,
    langCode: String,
) {
    val viewModel = hiltViewModel<InterviewViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // TODO speech to text service remove

//    val speechToTextService =
//        remember {
//            SpeechRecognizer.createSpeechRecognizer(context).apply {
//                setRecognitionListener(viewModel)
//            }
//        }

//    val speechIntent =
//        remember {
//            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//                putExtra(
//                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
//                )
//                putExtra(RecognizerIntent.EXTRA_LANGUAGE, langCode)
//                putExtra(
//                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                    RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH,
//                )
//            }
//        }

    val isMyTurn by viewModel.myTurnStepFlow.collectAsStateWithLifecycle()

    val dialogData by remember(uiState.dialogData) {
        derivedStateOf { uiState.dialogData }
    }

    var isRecording by
        remember {
            mutableStateOf(false)
        }

    var timeLeft by remember { mutableIntStateOf(0) }

    LaunchedEffect(key1 = timeLeft) {
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        if(isRecording){
            dialogData?.actions?.get(0)?.action?.invoke()
        }
    }

    var audioRecorder =
        remember {
            MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(context.getExternalFilesDir(null)!!.path + "/interviewaudio.mp3")
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

                try {
                    prepare()
                } catch (e: IOException) {
                }
            }
        }

    val ttsService =
        remember {
            TextToSpeech(context, viewModel).apply {
                setOnUtteranceProgressListener(
                    object : UtteranceProgressListener() {
                        override fun onStart(p0: String?) {
                            // no-op
                        }

                        override fun onDone(p0: String?) {
                            viewModel.incrementMyTurnStep()
                        }

                        override fun onError(p0: String?) {
                            // no-op
                        }
                    },
                )
            }
        }

    val size =
        remember(uiState.steps?.size) {
            uiState.steps?.size
        }

    val lifecycleOwner = LocalLifecycleOwner.current

    val isTablet by remember(context.isTablet()) {
        derivedStateOf { context.isTablet() }
    }

    val isTest by remember(uiState.isTest) {
        derivedStateOf { uiState.isTest }
    }

    LaunchedEffect(isMyTurn) {
        if (isMyTurn >= 0 && isMyTurn != size) {
            isRecording = true
            audioRecorder.start()
            timeLeft = 120
            viewModel.showDialog(
                DialogData(
                    R.string.recording_is_started,
                    listOf(
                        DialogAction(R.string.stop_recording) {
                            isRecording = false
                            audioRecorder.stop()
                            viewModel.clearDialog()
                            viewModel.retrieveAndUploadAudio(
                                context.getExternalFilesDir(null)!!.path + "/interviewaudio.mp3",
                                langCode,
                            ) {
                                audioRecorder.reset()
                                try {
                                    audioRecorder.apply {
                                        setAudioSource(MediaRecorder.AudioSource.MIC)
                                        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                        setOutputFile(context.getExternalFilesDir(null)!!.path + "/interviewaudio.mp3")
                                        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                    }
                                    audioRecorder.prepare()
                                } catch (e: IOException) {
                                }
                            }
                        },
                    ),
                    type = DialogType.SUCCESS_INFO,
                ),
            )
        }
    }
    val params =
        remember(uiState.currentStepInt) {
            Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
            }
        }

    LaunchedEffect(true) {
        viewModel.currentStepToTalk.collect {
            if (Locale.forLanguageTag(langCode).displayName != ttsService.language.displayName) {
                ttsService.setLanguage(Locale.forLanguageTag(langCode))
            }
            ttsService.speak(
                if (isTest == true) it.makeTestString(context, langCode) else it.makeString(context, langCode),
                TextToSpeech.QUEUE_ADD,
                params,
                "InterviewselfTTS",
            )
        }
    }

    //

    DisposableEffect(key1 = lifecycleOwner) {
        val lifecycleEventObserver =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE) {
                    ttsService.stop()
                    // viewModel.sendEvent(InterviewEvent.Back)
                }
            }
        lifecycleOwner.lifecycle.addObserver(lifecycleEventObserver)

        onDispose {
            viewModel.resetStep()
            ttsService.speak("", TextToSpeech.QUEUE_FLUSH, Bundle.EMPTY, "")
            ttsService.stop()
            ttsService.shutdown()
//            speechToTextService.cancel()
//            speechToTextService.stopListening()
//            speechToTextService.destroy()
            if (isRecording) audioRecorder.stop()
            audioRecorder.release()
            lifecycleOwner.lifecycle.removeObserver(lifecycleEventObserver)
        }
    }
    LaunchedEffect(true) {
        viewModel.event.collect {
            // TODO reorganize this part
            when (it) {
                InterviewEvent.Back -> {
                    onBackPressed.invoke()
                }

                is InterviewEvent.MicrophoneState -> {
                }

                is InterviewEvent.VideoState -> {
                    // enable or disable video
                }

                is InterviewEvent.ReinitializeSpeechRecognition -> {
                    if (isMyTurn >= 0 && isMyTurn != size) {
//                        speechToTextService.startListening(speechIntent)
                    }
                }
            }
            viewModel.configureCall(it)
        }
    }
    BaseView(dialogData = dialogData) {
        if (interviewType == InterviewType.PHONE_CALL.name) {
            PhoneInterviewScreen(uiState, viewModel::sendEvent)
        } else {
            if (isTablet) {
                VideoCallInterviewScreenForTablet(uiState, viewModel::sendEvent)
            } else {
                VideoCallInterviewScreen(uiState, viewModel::sendEvent)
            }
        }
    }
}
