package com.vadhara7.marketwebsocket.crypto.domain

import kotlinx.coroutines.flow.Flow

interface WebSocketService {
    fun observePrices(refreshInterval: Long): Flow<Map<String, String>>
    suspend fun closeConnection()
}