package com.batuhan.interviewself.presentation.splash

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onBackPressed: () -> Unit,
    navigateMainScreen: () -> Unit,
) {
    BackHandler {
        onBackPressed.invoke()
    }
    SplashScreenContent(navigateMainScreen)
}

@Composable
fun SplashScreenContent(navigateMainScreen: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedText(baseText = "interviewself", navigateMainScreen)
    }
}

@Composable
fun AnimatedText(
    baseText: String,
    navigateMainScreen: () -> Unit,
) {
    var updatedString by rememberSaveable {
        mutableStateOf("")
    }

    LaunchedEffect(key1 = true) {
        delay(150L)
        while (updatedString.length < baseText.length) {
            val currentIndex = updatedString.length
            updatedString += baseText[currentIndex]
            delay(150L)
        }
        delay(1000L)
        navigateMainScreen.invoke()
    }

    Text(
        text = updatedString,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    )
}

@Preview
@Composable
fun SplashPreview() {
    SplashScreenContent({})
}
