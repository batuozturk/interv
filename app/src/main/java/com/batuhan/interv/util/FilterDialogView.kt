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
import com.batuhan.interv.data.model.FilterType

@Composable
fun <T, X : FilterType<T>> FilterDialogView(
    filterType: X?,
    selectedFilter: T,
    updateFilterType: (T) -> Unit,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.systemBars)) {
        AnimatedVisibility(visible = filterType != null) {
            val dialog = remember(this) { filterType!! }
            FilterView(
                selectedFilterIndex = dialog.list.indexOf(selectedFilter),
                filterType = dialog,
                updateFilterType
            )
        }
        content()
    }
}
