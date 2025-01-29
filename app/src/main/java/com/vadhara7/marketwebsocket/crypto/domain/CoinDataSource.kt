package com.vadhara7.marketwebsocket.crypto.domain

import com.vadhara7.marketwebsocket.core.domain.util.NetworkError
import com.vadhara7.marketwebsocket.core.domain.util.Result

interface CoinDataSource {
    suspend fun getCoins(): Result<List<Coin>, NetworkError>
}