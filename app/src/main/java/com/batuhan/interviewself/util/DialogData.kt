package com.batuhan.interviewself.util

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color

data class DialogData(
    @StringRes val title: Int,
    val actions: List<DialogAction>,
    val type: DialogType = DialogType.ERROR
)

data class DialogAction(
    @StringRes val text: Int,
    val action: () -> Unit
)

enum class DialogType(val containerColor: Color, val textColor: Color = Color.White) {
    ERROR(Color.Red), SUCCESS_INFO(Color.Green), INFO(Color.White, Color.Black)
}
