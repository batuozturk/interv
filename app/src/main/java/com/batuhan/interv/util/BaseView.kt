package com.batuhan.interv.util

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun BaseView(
    dialogData: DialogData?,
    content: @Composable () -> Unit,
) {

    Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars)) {
        AnimatedVisibility(visible = dialogData != null) {
            val dialog = remember(this) { dialogData!! }
            DialogView(dialogData = dialog)
        }
        content()
    }
}
