package com.example.diecasthangar.data.remote

sealed class Response<out T> {
    object Loading: Response<Nothing>()

    data class Success<out T>(
        var data: @UnsafeVariance T?
    ): Response<T>()

    data class Failure(
        val e: Exception
    ): Response<Nothing>()
}

