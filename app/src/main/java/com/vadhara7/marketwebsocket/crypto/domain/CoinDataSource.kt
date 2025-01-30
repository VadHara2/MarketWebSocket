package com.vadhara7.marketwebsocket.crypto.domain

import com.vadhara7.marketwebsocket.core.domain.util.NetworkError
import com.vadhara7.marketwebsocket.core.domain.util.Result
import kotlinx.coroutines.flow.StateFlow
import java.time.ZonedDateTime

interface CoinDataSource {
    suspend fun getCoins(): Result<StateFlow<List<Coin>>, NetworkError>
    suspend fun getCoinHistory(
        coinId: String,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Result<StateFlow<List<CoinPrice>>, NetworkError>
}