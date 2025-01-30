package com.vadhara7.marketwebsocket.core.data.networking

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class WebSocketClient(private val client: HttpClient) {

    private var webSocketSession: WebSocketSession? = null

    fun observePrices(): Flow<Map<String, String>> = flow {
        client.webSocket(method = HttpMethod.Get, host = "ws.coincap.io", path = "/prices?assets=ALL") {
            webSocketSession = this
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val data = frame.readText()
                    val priceMap: Map<String, String> = Json.decodeFromString(data)
                    emit(priceMap)
                }
            }
        }
    }

    fun observeCoin(coinId: String): Flow<Map<String, String>> = flow {
        client.webSocket(method = HttpMethod.Get, host = "ws.coincap.io", path = "/prices?assets=$coinId") {
            webSocketSession = this
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val data = frame.readText()
                    val priceMap: Map<String, String> = Json.decodeFromString(data)
                    emit(priceMap)
                }
            }
        }
    }

    suspend fun closeConnection() {
        webSocketSession?.close(CloseReason(CloseReason.Codes.GOING_AWAY, "Client closed connection"))
        webSocketSession = null
    }
}