package com.arjun.sendbird.model

// A generic class that contains data and status about loading this data.
sealed class Resource<T>(
    val data: T? = null,
    val e: Throwable? = null
) {
    class Init<T> : Resource<T>()
    class Success<T>(data: T) : Resource<T>(data)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Error<T>(e: Throwable) : Resource<T>(e = e)
}