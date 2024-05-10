package com.batuhan.interviewself.util

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batuhan.interviewself.R
import com.batuhan.interviewself.data.model.FilterType
import com.batuhan.interviewself.data.model.InterviewFilterType
import com.batuhan.interviewself.data.model.QuestionFilterType
import com.batuhan.interviewself.ui.theme.InterviewselfTheme
import com.batuhan.interviewself.ui.theme.White
import com.batuhan.interviewself.ui.theme.fontFamily

@Composable
fun <X, T : FilterType<X>> FilterView(
    selectedFilterIndex: Int,
    filterType: T,
    updateFilterType: (X) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(stringResource(id = R.string.filter))
            Button(
                contentPadding = ButtonDefaults.TextButtonContentPadding,
                onClick = {updateFilterType.invoke(filterType.list[selectedFilterIndex])},
                colors =
                ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text(
                    stringResource(id = R.string.dismiss),
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal
                )
            }
        }
        ScrollableTabRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            selectedTabIndex = selectedFilterIndex,
            edgePadding = 0.dp,
            divider = {},
            indicator = {
                if (selectedFilterIndex < filterType.list.size) {
                    Column(
                        modifier =
                            Modifier
                                .tabIndicatorOffset(it[selectedFilterIndex])
                                .fillMaxSize()
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.onSurface,
                                    RoundedCornerShape(10.dp),
                                ),
                    ) {
                    }
                }
            },
        ) {
            filterType.list.forEachIndexed { i, x ->
                Tab(
                    modifier =
                        Modifier
                            .height(48.dp)
                            .padding(12.dp)
                            .fillMaxWidth(),
                    selected = selectedFilterIndex == i,
                    onClick = {
                        updateFilterType.invoke(x)
                    },
                ) {
                    val title =
                        if (x is InterviewFilterType) (x as InterviewFilterType).title else (x as QuestionFilterType).title
                    Text(stringResource(id = title))
                }
            }
        }
    }
}

@Preview
@Composable
fun FilterViewPreview() {
    InterviewselfTheme {
        FilterView(selectedFilterIndex = 0, filterType = FilterType.Interview) {
//
        }
    }
}
