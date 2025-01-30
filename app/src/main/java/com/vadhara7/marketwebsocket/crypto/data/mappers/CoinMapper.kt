package com.vadhara7.marketwebsocket.crypto.data.mappers

import com.vadhara7.marketwebsocket.crypto.data.networking.dto.CoinDto
import com.vadhara7.marketwebsocket.crypto.domain.Coin

fun CoinDto.toCoin(): Coin {
    return Coin(
        id = id,
        rank = rank,
        name = name,
        symbol = symbol,
        marketCapUsd = marketCapUsd,
        priceUsd = priceUsd,
        changePercent24Hr = changePercent24Hr
    )
}
