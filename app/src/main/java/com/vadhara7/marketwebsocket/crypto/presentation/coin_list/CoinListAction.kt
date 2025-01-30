package com.vadhara7.marketwebsocket.crypto.presentation.coin_list

import com.vadhara7.marketwebsocket.crypto.presentation.models.CoinUi

sealed interface CoinListAction {
    data class OnCoinClick(val coinUi: CoinUi): CoinListAction
    data class OnIntervalChange(val newInterval: Int): CoinListAction
}