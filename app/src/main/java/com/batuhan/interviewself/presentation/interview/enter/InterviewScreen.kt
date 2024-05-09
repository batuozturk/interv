package com.batuhan.interviewself.presentation.interview.enter

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batuhan.interviewself.data.model.InterviewType
import com.batuhan.interviewself.data.model.makeString
import com.batuhan.interviewself.presentation.interview.enter.phone.PhoneInterviewScreen
import com.batuhan.interviewself.presentation.interview.enter.videocall.VideoCallInterviewScreen
import com.batuhan.interviewself.presentation.interview.enter.videocall.VideoCallInterviewScreenForTablet
import com.batuhan.interviewself.util.BaseView
import com.batuhan.interviewself.util.isTablet
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

    val speechToTextService =
        remember {
            SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(viewModel)
            }
        }

    val speechIntent =
        remember {
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, langCode)
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH,
                )
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
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

    val dialogData by remember(uiState.dialogData) {
        derivedStateOf { uiState.dialogData }
    }

    val size = remember(uiState.steps?.size) {
        uiState.steps?.size
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    val isTablet by remember(context.isTablet()) {
        derivedStateOf { context.isTablet() }
    }

    val isMyTurn by viewModel.myTurnStepFlow.collectAsStateWithLifecycle()

    LaunchedEffect(isMyTurn) {
        if(isMyTurn >= 0 && isMyTurn != size){
            speechToTextService.startListening(speechIntent)
        }

    }
    val params = remember(uiState.currentStepInt) {
        Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
        }
    }

    LaunchedEffect(true) {
        viewModel.currentStepToTalk.collect {
            if(Locale.forLanguageTag(langCode).displayName != ttsService.language.displayName)
                ttsService.setLanguage(Locale.forLanguageTag(langCode))
            ttsService.speak(
                it.makeString(context, langCode),
                TextToSpeech.QUEUE_ADD,
                params,
                "InterviewselfTTS",
            )
        }
    }

    DisposableEffect(key1 = lifecycleOwner) {
        val lifecycleEventObserver =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE) {
                    ttsService.stop()
                    //viewModel.sendEvent(InterviewEvent.Back)
                }
            }
        lifecycleOwner.lifecycle.addObserver(lifecycleEventObserver)

        onDispose {
            viewModel.resetStep()
            ttsService.speak("", TextToSpeech.QUEUE_FLUSH, Bundle.EMPTY, "")
            ttsService.stop()
            ttsService.shutdown()
            speechToTextService.cancel()
            speechToTextService.stopListening()
            speechToTextService.destroy()
            lifecycleOwner.lifecycle.removeObserver(lifecycleEventObserver)
        }
    }
    LaunchedEffect(true) {
        viewModel.event.collect {
            when (it) {
                InterviewEvent.Back -> {
                    onBackPressed.invoke()
                }

                is InterviewEvent.MicrophoneState -> {
                }

                is InterviewEvent.VideoState -> {
                    // enable or disable video
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
