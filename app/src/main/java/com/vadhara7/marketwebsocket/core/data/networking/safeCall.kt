package com.vadhara7.marketwebsocket.core.data.networking

import com.vadhara7.marketwebsocket.core.domain.util.NetworkError
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import kotlin.coroutines.coroutineContext
import com.vadhara7.marketwebsocket.core.domain.util.Result

suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse
): Result<T, NetworkError> {
    val response = try {
        execute()
    } catch(e: UnresolvedAddressException) {
        return Result.Error(NetworkError.NO_INTERNET)
    } catch(e: SerializationException) {
        return Result.Error(NetworkError.SERIALIZATION)
    } catch(e: Exception) {
        coroutineContext.ensureActive()
        return Result.Error(NetworkError.UNKNOWN)
    }

    return responseToResult(response)
}