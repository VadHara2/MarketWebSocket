package com.vadhara7.marketwebsocket.crypto.data.networking

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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.seconds

class RemoteCoinDataSource(
    private val httpClient: HttpClient,
    private val webSocketClient: WebSocketClient
) : CoinDataSource {

    @OptIn(FlowPreview::class)
    override suspend fun getCoins(refreshInterval: Int): Result<Flow<List<Coin>>, NetworkError> {
        return fetchInitialCoins().map { initialCoins ->
            val initialCoinsMap = initialCoins.associateBy { it.id }

            webSocketClient.observePrices(refreshInterval = refreshInterval * 1000L)
                .sample(refreshInterval.seconds)
                .scan(initialCoinsMap) { currentCoins, priceMap ->
                    currentCoins.mapValues { (id, coin) ->
                        val newPrice = priceMap[id]?.toDouble() ?: coin.priceUsd

                        val price24HrAgo = if (coin.changePercent24Hr != 0.0) {
                            coin.priceUsd / (1 + coin.changePercent24Hr / 100)
                        } else {
                            coin.priceUsd
                        }

                        val updatedChangePercent24Hr = if (price24HrAgo != 0.0) {
                            ((newPrice - price24HrAgo) / price24HrAgo) * 100
                        } else {
                            0.0
                        }

                        coin.copy(priceUsd = newPrice, changePercent24Hr = updatedChangePercent24Hr)
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
