package com.batuhan.interv.util

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.batuhan.interv.ui.theme.InterviewselfTheme

@Composable
fun ActionView(
    searchString: (String) -> Unit,
    icon1: ImageVector,
    icon2: ImageVector,
    icon3: ImageVector? = null,
    placeholderString: String,
    action1: () -> Unit,
    action2: () -> Unit,
    action3: (() -> Unit)? = null
) {
    var textString by remember {
        mutableStateOf("")
    }
    val focusManager = LocalFocusManager.current
    val isKeyboardOpen by keyboardAsState()
    LaunchedEffect(isKeyboardOpen) {
        if (!isKeyboardOpen) focusManager.clearFocus()
    }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(7f),
            placeholder = {
                Text(placeholderString)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            colors = OutlinedTextFieldDefaults.colors(),
            value = textString,
            onValueChange = {
                searchString.invoke(it)
                textString = it
            },
            singleLine = true,
        )
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(onClick = action1, modifier = Modifier.weight(1.5f)) {
            Icon(icon1, contentDescription = null)
        }
        IconButton(onClick = action2, modifier = Modifier.weight(1.5f)) {
            Icon(icon2, contentDescription = null)
        }
        action3?.let{
            IconButton(onClick = action3, modifier = Modifier.weight(1.5f)) {
                Icon(icon3!!, contentDescription = null)
            }
        }
    }
}

@Preview
@Composable
fun ActionViewPreview() {
    InterviewselfTheme {
        ActionView(
            searchString = { _ -> },
            icon1 = Icons.AutoMirrored.Default.List,
            icon2 = Icons.Outlined.Add,
            icon3 = Icons.Outlined.Add,
            placeholderString = "search questions",
            action1 = {},
            action2 = {},
            action3 = {}
        )
    }
}
