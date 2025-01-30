package com.vadhara7.marketwebsocket.crypto.presentation.models

import android.icu.text.NumberFormat
import androidx.annotation.DrawableRes
import com.vadhara7.marketwebsocket.crypto.domain.Coin
import com.vadhara7.marketwebsocket.core.presentation.util.getDrawableIdForCoin
import com.vadhara7.marketwebsocket.crypto.presentation.coin_detail.DataPoint
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CoinUi(
    val id: String,
    val rank: Int,
    val name: String,
    val symbol: String,
    val marketCapUsd: DisplayableNumber,
    val priceUsd: DisplayableNumber,
    val changePercent24Hr: DisplayableNumber,
    @DrawableRes val iconRes: Int,
    val coinPriceHistory: List<CoinPrice> = emptyList()
)

data class DisplayableNumber(
    val value: Double,
    val formatted: String
)

data class CoinPrice(
    val priceUsd: Double,
    val dateTime: ZonedDateTime
){
    fun toDataPoint(): DataPoint {
        return DataPoint(
            x = dateTime.hour.toFloat(),
            y = priceUsd.toFloat(),
            xLabel = DateTimeFormatter
                .ofPattern("mm:ss")
                .format(dateTime)
        )
    }
}

fun Coin.toCoinUi(): CoinUi {
    return CoinUi(
        id = id,
        name = name,
        symbol = symbol,
        rank = rank,
        priceUsd = priceUsd.toDisplayableNumber(),
        marketCapUsd = marketCapUsd.toDisplayableNumber(),
        changePercent24Hr = changePercent24Hr.toDisplayableNumber(),
        iconRes = getDrawableIdForCoin(symbol)
    )
}

fun Double.toDisplayableNumber(): DisplayableNumber {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return DisplayableNumber(
        value = this,
        formatted = formatter.format(this)
    )
}