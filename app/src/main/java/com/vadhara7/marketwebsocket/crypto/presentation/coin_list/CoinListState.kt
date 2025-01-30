package com.vadhara7.marketwebsocket.crypto.presentation.coin_list

import androidx.compose.runtime.Immutable
import com.vadhara7.marketwebsocket.core.presentation.util.CoinFilter
import com.vadhara7.marketwebsocket.crypto.presentation.models.CoinUi

@Immutable
data class CoinListState(
    val isLoading: Boolean = false,
    val coins: List<CoinUi> = emptyList(),
    val selectedCoin: CoinUi? = null,
    val lastUpdated: String? = null,
    val refreshInterval: Int = 1,
    val selectedFilter: CoinFilter = CoinFilter.ALL
)