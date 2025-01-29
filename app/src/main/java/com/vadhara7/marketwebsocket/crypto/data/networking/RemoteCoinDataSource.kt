package com.vadhara7.marketwebsocket.crypto.data.networking

import com.vadhara7.marketwebsocket.core.data.networking.WebSocketClient
import com.vadhara7.marketwebsocket.core.data.networking.constructUrl
import com.vadhara7.marketwebsocket.core.data.networking.safeCall
import com.vadhara7.marketwebsocket.core.domain.util.NetworkError
import com.vadhara7.marketwebsocket.core.domain.util.Result
import com.vadhara7.marketwebsocket.core.domain.util.map
import com.vadhara7.marketwebsocket.crypto.data.mappers.toCoin
import com.vadhara7.marketwebsocket.crypto.data.networking.dto.CoinsResponseDto
import com.vadhara7.marketwebsocket.crypto.domain.Coin
import com.vadhara7.marketwebsocket.crypto.domain.CoinDataSource
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class RemoteCoinDataSource(
    private val httpClient: HttpClient,
    private val webSocketClient: WebSocketClient
) : CoinDataSource {

    private val _coins = MutableStateFlow<Result<List<Coin>, NetworkError>>(Result.Success(emptyList()))

    override suspend fun getCoins(): StateFlow<Result<List<Coin>, NetworkError>> =
        merge(fetchInitialCoinsFlow(), observeWebSocket())
            .onCompletion { webSocketClient.closeConnection() }
            .stateIn(
                scope = CoroutineScope(Dispatchers.IO),
                started = SharingStarted.WhileSubscribed(0),
                initialValue = _coins.value
            )

    private fun fetchInitialCoinsFlow(): Flow<Result<List<Coin>, NetworkError>> = flow {
        emit(fetchInitialCoins())
    }.onEach { _coins.value = it }

    private suspend fun fetchInitialCoins(): Result<List<Coin>, NetworkError> =
        safeCall<CoinsResponseDto> {
            httpClient.get(constructUrl("/assets"))
        }.map { response -> response.data.map { it.toCoin() } }

    private fun observeWebSocket(): Flow<Result<List<Coin>, NetworkError>> =
        webSocketClient.observePrices().map { priceMap ->
            _coins.value.map { coins ->
                coins.map { coin ->
                    coin.copy(priceUsd = priceMap[coin.id]?.toDouble() ?: coin.priceUsd)
                }
            }
        }.onEach { _coins.value = it }
}
