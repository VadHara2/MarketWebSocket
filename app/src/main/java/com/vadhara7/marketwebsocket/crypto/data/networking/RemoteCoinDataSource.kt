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
import com.vadhara7.marketwebsocket.crypto.domain.CoinPrice
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
            merge(
                flowOf(initialCoins),
                webSocketClient.observePrices()
                    .map { priceMap ->
                        initialCoins.map { coin ->
                            coin.copy(priceUsd = priceMap[coin.id]?.toDouble() ?: coin.priceUsd)
                        }
                    }
                    .distinctUntilChanged()
            )
        }
    }

    private suspend fun fetchInitialCoins(): Result<List<Coin>, NetworkError> =
        safeCall<CoinsResponseDto> {
            httpClient.get(constructUrl("/assets"))
        }.map { response -> response.data.map { it.toCoin() } }

    override suspend fun getCoinHistory(
        coinId: String
    ): Result<Flow<List<CoinPrice>>, NetworkError> {
        // обраний апі не має історії протягом 5 хв, тому відслідковуюю її сам
        return Result.Success(
            webSocketClient.observeCoin(coinId)
                .scan(emptyList<CoinPrice>()) { history, priceMap ->
                    val newPrice = CoinPrice(
                        priceUsd = priceMap.values.first().toDouble(),
                        dateTime = Instant.ofEpochMilli(System.currentTimeMillis())
                            .atZone(ZoneId.systemDefault())
                    )

                    val updatedHistory = (history + newPrice)
                        .filter { it.dateTime.isAfter(ZonedDateTime.now().minusMinutes(5)) }

                    updatedHistory
                }
                .flowOn(Dispatchers.IO)
        )
    }
}
