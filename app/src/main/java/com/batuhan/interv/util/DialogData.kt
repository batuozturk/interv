package com.batuhan.interv.util

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.batuhan.interv.ui.theme.Gray

data class DialogData(
    @StringRes val title: Int,
    val actions: List<DialogAction>,
    val options: List<DialogAction>? = null,
    val type: DialogType = DialogType.ERROR,
)

data class DialogAction(
    @StringRes val text: Int,
    val action: () -> Unit
)

enum class DialogType(val containerColor: Color, val textColor: Color = Color.White) {
    ERROR(Color(0xFFA0153E)),
    SUCCESS_INFO(Color(0xFF4F6F52)),
    DIALOG_LIGHT(
        Color.White,
        Gray,
    ),
    DIALOG_DARK(
        Gray,
        Color.White,
    )

}
fun decideDialogType(darkMode: Boolean) = if(darkMode) DialogType.DIALOG_DARK else DialogType.DIALOG_LIGHT
