package com.vadhara7.marketwebsocket.crypto.data.networking

import com.vadhara7.marketwebsocket.core.data.networking.constructUrl
import com.vadhara7.marketwebsocket.core.data.networking.safeCall
import com.vadhara7.marketwebsocket.core.domain.util.NetworkError
import com.vadhara7.marketwebsocket.core.domain.util.map
import com.vadhara7.marketwebsocket.crypto.data.mappers.toCoin
import com.vadhara7.marketwebsocket.crypto.data.networking.dto.CoinsResponseDto
import com.vadhara7.marketwebsocket.crypto.domain.Coin
import com.vadhara7.marketwebsocket.crypto.domain.CoinDataSource
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import com.vadhara7.marketwebsocket.core.domain.util.Result

class RemoteCoinDataSource(
    private val httpClient: HttpClient
): CoinDataSource {

    override suspend fun getCoins(): Result<List<Coin>, NetworkError> {
        return safeCall<CoinsResponseDto> {
            httpClient.get(
                urlString = constructUrl("/assets")
            )
        }.map { response ->
            response.data.map { it.toCoin() }
        }
    }
}