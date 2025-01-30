package com.vadhara7.marketwebsocket.crypto.domain

import com.vadhara7.marketwebsocket.core.domain.util.NetworkError
import com.vadhara7.marketwebsocket.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface CoinDataSource {
    suspend fun getCoins(refreshInterval: Int): Result<Flow<List<Coin>>, NetworkError>
}