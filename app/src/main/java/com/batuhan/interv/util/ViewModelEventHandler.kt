package com.batuhan.interv.util

interface ViewModelEventHandler<in T, in X> {

    fun sendEvent(event: T)

    fun showDialog(dialogData: DialogData)

    fun clearDialog()

    fun retryOperation(error: X)
}