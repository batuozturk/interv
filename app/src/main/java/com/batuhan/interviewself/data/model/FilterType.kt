package com.batuhan.interviewself.data.model

sealed class FilterType<out T>( val list: List<T>) {
    data object Question: FilterType<QuestionFilterType>(QuestionFilterType.entries.toList())
    data object Interview: FilterType<InterviewFilterType>(InterviewFilterType.entries.toList())
}
