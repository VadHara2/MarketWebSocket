package com.vadhara7.marketwebsocket.crypto.domain

import com.vadhara7.marketwebsocket.core.domain.util.NetworkError
import com.vadhara7.marketwebsocket.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.ZonedDateTime

interface CoinDataSource {
    suspend fun getCoins(): Result<Flow<List<Coin>>, NetworkError>
    suspend fun getCoinHistory(
        coinId: String
    ): Result<Flow<List<CoinPrice>>, NetworkError>
}