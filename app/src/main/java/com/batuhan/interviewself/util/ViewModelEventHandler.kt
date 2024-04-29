package com.batuhan.interviewself.util

interface ViewModelEventHandler<in T, in X> {

    fun sendEvent(event: T)

    fun showDialog(dialogData: DialogData)

    fun clearDialog()

    fun retryOperation(error: X)
}