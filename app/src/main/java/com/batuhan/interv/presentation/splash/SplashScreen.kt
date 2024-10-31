package com.batuhan.interv.presentation.splash

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batuhan.interv.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    appUpdate: Boolean,
    onBackPressed: () -> Unit,
    navigateMainScreen: () -> Unit,
    updateApp: () -> Unit
) {
    BackHandler {
        onBackPressed.invoke()
    }
    SplashScreenContent{
        if(appUpdate){
            updateApp.invoke()
            onBackPressed.invoke()
        }
        else navigateMainScreen.invoke()
    }
}

@Composable
fun SplashScreenContent(navigateMainScreen: () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(painterResource(id = R.drawable.ic_logo), contentDescription = null, modifier = Modifier.size(42.dp))
            Spacer(Modifier.width(8.dp))
            AnimatedText(baseText = "interv", navigateMainScreen)
        }
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
