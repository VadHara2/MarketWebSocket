package com.vadhara7.marketwebsocket.crypto.data.networking

import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class RemoteCoinDataSource(
    private val httpClient: HttpClient,
    private val webSocketClient: WebSocketClient
) : CoinDataSource {

    override suspend fun getCoins(): Result<Flow<List<Coin>>, NetworkError> {
        return fetchInitialCoins().map { initialCoins ->
            val initialCoinsMap = initialCoins.associateBy { it.id }

            webSocketClient.observePrices()
                .scan(initialCoinsMap) { currentCoins, priceMap ->
                    currentCoins.mapValues { (id, coin) ->
                        coin.copy(priceUsd = priceMap[id]?.toDouble() ?: coin.priceUsd)
                    }
                }
                .map { it.values.toList() }
                .distinctUntilChanged()
        }
    }


    private suspend fun fetchInitialCoins(): Result<List<Coin>, NetworkError> =
        safeCall<CoinsResponseDto> {
            httpClient.get(constructUrl("/assets"))
        }.map { response -> response.data.map { it.toCoin() } }

}
