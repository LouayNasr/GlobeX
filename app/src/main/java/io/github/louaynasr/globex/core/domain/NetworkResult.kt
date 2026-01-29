package io.github.louaynasr.globex.core.domain

import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val error: DataError) : NetworkResult<Nothing>()
}

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            response.body()?.let { data ->
                NetworkResult.Success(data)
            } ?: NetworkResult.Error(DataError.Remote.SERIALIZATION)
        } else {
            val error = when (response.code()) {
                408 -> DataError.Remote.REQUEST_TIMEOUT
                429 -> DataError.Remote.TOO_MANY_REQUESTS
                500 -> DataError.Remote.SERVER
                else -> DataError.Remote.UNKNOWN
            }
            NetworkResult.Error(error)
        }
    } catch (e: Exception) {
        val error = when (e) {
            is SocketTimeoutException -> DataError.Remote.REQUEST_TIMEOUT
            is IOException -> DataError.Remote.NO_INTERNET
            else -> DataError.Remote.UNKNOWN
        }
        NetworkResult.Error(error)
    }
}