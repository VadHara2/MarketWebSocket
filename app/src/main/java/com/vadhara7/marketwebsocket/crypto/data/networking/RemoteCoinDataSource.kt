package com.vadhara7.marketwebsocket.crypto.data.networking

import com.vadhara7.marketwebsocket.core.data.networking.WebSocketClient
import com.vadhara7.marketwebsocket.core.data.networking.constructUrl
import com.vadhara7.marketwebsocket.core.data.networking.safeCall
import com.vadhara7.marketwebsocket.core.domain.util.NetworkError
import com.vadhara7.marketwebsocket.core.domain.util.Result
import com.vadhara7.marketwebsocket.core.domain.util.map
import com.vadhara7.marketwebsocket.crypto.data.mappers.toCoin
import com.vadhara7.marketwebsocket.crypto.data.mappers.toCoinPrice
import com.vadhara7.marketwebsocket.crypto.data.networking.dto.CoinHistoryDto
import com.vadhara7.marketwebsocket.crypto.data.networking.dto.CoinsResponseDto
import com.vadhara7.marketwebsocket.crypto.domain.Coin
import com.vadhara7.marketwebsocket.crypto.domain.CoinDataSource
import com.vadhara7.marketwebsocket.crypto.domain.CoinPrice
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class RemoteCoinDataSource(
    private val httpClient: HttpClient,
    private val webSocketClient: WebSocketClient
) : CoinDataSource {


    override suspend fun getCoins(): Result<StateFlow<List<Coin>>, NetworkError> {
        return fetchInitialCoins().map { initialCoins ->
            merge(
                flowOf(initialCoins),
                webSocketClient.observePrices().map { priceMap ->
                    initialCoins.map { coin ->
                        coin.copy(priceUsd = priceMap[coin.id]?.toDouble() ?: coin.priceUsd)
                    }
                }
            )
                .stateIn(
                    scope = CoroutineScope(Dispatchers.IO),
                    started = SharingStarted.WhileSubscribed(0),
                    initialValue = initialCoins
                )
        }
    }

    private suspend fun fetchInitialCoins(): Result<List<Coin>, NetworkError> =
        safeCall<CoinsResponseDto> {
            httpClient.get(constructUrl("/assets"))
        }.map { response -> response.data.map { it.toCoin() } }

    override suspend fun getCoinHistory(
        coinId: String,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Result<StateFlow<List<CoinPrice>>, NetworkError> {
        return fetchInitialHistory(coinId, start, end).map { initialHistory ->
            webSocketClient.observeCoin(coinId)
                .scan(initialHistory) { history, priceMap ->
                    history + CoinPrice(
                        priceUsd = priceMap.values.first().toDouble(),
                        dateTime = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.of("UTC"))
                    )
                }
                .stateIn(
                    scope = CoroutineScope(Dispatchers.IO),
                    started = SharingStarted.WhileSubscribed(0),
                    initialValue = initialHistory
                )
        }
    }



    private suspend fun fetchInitialHistory(
        coinId: String,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Result<List<CoinPrice>, NetworkError> {
        val startMillis = start
            .withZoneSameInstant(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()
        val endMillis = end
            .withZoneSameInstant(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()

        return safeCall<CoinHistoryDto> {
            httpClient.get(
                urlString = constructUrl("/assets/$coinId/history")
            ) {
                parameter("interval", "h6")
                parameter("start", startMillis)
                parameter("end", endMillis)
            }
        }.map { response ->
            response.data.map { it.toCoinPrice() }
        }
    }

}
