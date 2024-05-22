package com.batuhan.interv.util

sealed class Result<out T> {
    data class Success<out R>(val data: R): Result<R>()
    data class Error(val exception: Throwable): Result<Nothing>()
}