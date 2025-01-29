package com.vadhara7.marketwebsocket.crypto.presentation.coin_list

import com.vadhara7.marketwebsocket.core.domain.util.NetworkError

sealed interface CoinListEvent {
    data class Error(val error: NetworkError): CoinListEvent
}