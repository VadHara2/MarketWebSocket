package com.vadhara7.marketwebsocket.crypto.presentation.coin_list

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vadhara7.marketwebsocket.core.domain.util.onError
import com.vadhara7.marketwebsocket.core.domain.util.onSuccess
import com.vadhara7.marketwebsocket.core.presentation.util.CoinFilter
import com.vadhara7.marketwebsocket.crypto.domain.CoinDataSource
import com.vadhara7.marketwebsocket.crypto.presentation.models.CoinPrice
import com.vadhara7.marketwebsocket.crypto.presentation.models.CoinUi
import com.vadhara7.marketwebsocket.crypto.presentation.models.toCoinUi
import com.vadhara7.marketwebsocket.crypto.presentation.models.toDisplayableNumber
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class CoinListViewModel(
    private val coinDataSource: CoinDataSource
) : ViewModel(), DefaultLifecycleObserver {

    private val _state = MutableStateFlow(CoinListState())
    val state = _state
        .onStart { loadCoins() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(0),
            CoinListState(isLoading = true)
        )

    private val _events = Channel<CoinListEvent>()
    val events = _events.receiveAsFlow()

    private var coinsJob: Job? = null
    private var historyJob: Job? = null

    fun onAction(action: CoinListAction) {
        when (action ) {
            is CoinListAction.OnCoinClick -> selectCoin(action.coinUi)
            is CoinListAction.OnIntervalChange -> {
                _state.update {
                    it.copy(refreshInterval = action.newInterval)
                }
                loadCoins()
            }
            is CoinListAction.OnFilterSelect -> {
                _state.update {
                    it.copy(selectedFilter = action.filter, coins = it.coins.applyFilters(action.filter))
                }
            }
        }
    }

    private fun loadCoins() {
        coinsJob?.cancel()
        coinsJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            coinDataSource.getCoins(_state.value.refreshInterval).onSuccess { coinsStateFlow ->
                coinsStateFlow.collectLatest { coins ->
                    Log.d("TAG", "coinsStateFlow.collectLatest: $coins")

                    _state.update { state ->
                        val currentTime = ZonedDateTime.now()

                        val updatedCoins = coins.map { coin ->
                            val previousCoin = state.coins.find { it.id == coin.id }

                            if (previousCoin?.priceUsd?.value == coin.priceUsd.toDisplayableNumber().value) {
                                return@map previousCoin
                            }

                            val updatedHistory = (previousCoin?.coinPriceHistory ?: emptyList())
                                .filter { it.dateTime.isAfter(currentTime.minusMinutes(5)) }
                                .plus(CoinPrice(priceUsd = coin.priceUsd, dateTime = currentTime))

                            coin.toCoinUi().copy(coinPriceHistory = updatedHistory)
                        }

                        val selectedCoin = state.selectedCoin?.let { selected ->
                            updatedCoins.find { it.id == selected.id }
                        }

                        state.copy(
                            isLoading = false,
                            coins = updatedCoins.applyFilters(state.selectedFilter),
                            selectedCoin = selectedCoin,
                            lastUpdated = DateTimeFormatter
                                .ofPattern("HH:mm:ss")
                                .format(currentTime)
                        )
                    }
                }
            }.onError { error ->
                _state.update { it.copy(isLoading = false) }
                _events.send(CoinListEvent.Error(error))
            }
        }
    }

    private fun selectCoin(coinUi: CoinUi) {
        Log.d("TAG", "selectCoin: #$coinUi")
        _state.update { it.copy(selectedCoin = coinUi) }
    }

    override fun onPause(owner: LifecycleOwner) {
        Log.d("TAG", "onPause")
        cancelJobs()
        super.onPause(owner)
    }

    private fun cancelJobs() {
        Log.d("TAG", "cancelJobs")
        coinsJob?.cancel()
        historyJob?.cancel()
    }

    private fun List<CoinUi>.applyFilters(filter: CoinFilter): List<CoinUi> {
        return when (filter) {
            CoinFilter.TOP_GAINERS -> sortedByDescending { it.changePercent24Hr.value }
            CoinFilter.TOP_LOSERS -> sortedBy { it.changePercent24Hr.value }
            CoinFilter.HIGH_VOLUME -> filter { it.marketCapUsd.value > 1_000_000 }
            CoinFilter.WATCHLIST -> filter { true }
            CoinFilter.ALL -> this
        }
    }
}
