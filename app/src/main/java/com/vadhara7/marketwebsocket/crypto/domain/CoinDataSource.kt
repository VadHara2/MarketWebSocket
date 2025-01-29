package com.vadhara7.marketwebsocket.crypto.domain

import com.vadhara7.marketwebsocket.core.domain.util.NetworkError
import com.vadhara7.marketwebsocket.core.domain.util.Result
import kotlinx.coroutines.flow.StateFlow

interface CoinDataSource {
    suspend fun getCoins(): StateFlow<Result<List<Coin>, NetworkError>>
}