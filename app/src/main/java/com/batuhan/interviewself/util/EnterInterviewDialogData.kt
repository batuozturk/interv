package com.batuhan.interviewself.util

data class EnterInterviewDialogData(
    val interviewId: Long,
    val action1: () -> Unit,
    val action2: () -> Unit,
)