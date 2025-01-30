package com.vadhara7.marketwebsocket.crypto.presentation.coin_list

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vadhara7.marketwebsocket.core.domain.util.onError
import com.vadhara7.marketwebsocket.core.domain.util.onSuccess
import com.vadhara7.marketwebsocket.crypto.domain.CoinDataSource
import com.vadhara7.marketwebsocket.crypto.presentation.models.CoinUi
import com.vadhara7.marketwebsocket.crypto.presentation.models.toCoinUi
import com.vadhara7.marketwebsocket.crypto.presentation.models.toDisplayableNumber
import kotlinx.coroutines.Dispatchers
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
        when (action) {
            is CoinListAction.OnCoinClick -> {
                selectCoin(action.coinUi)
            }
        }
    }

    private fun loadCoins() {
        cancelJobs()

        coinsJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true
                )
            }

            coinDataSource
                .getCoins()
                .onSuccess { coinsStateFlow ->
                    coinsStateFlow.collectLatest { coins ->
                        Log.d("TAG", "coinsStateFlow.collectLatest: $coins")

                        _state.update {
                            it.copy(
                                isLoading = false,
                                coins = coins.map { it.toCoinUi() }
                            )
                        }
                    }

                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false) }
                    _events.send(CoinListEvent.Error(error))
                }
        }
    }

    private fun selectCoin(coinUi: CoinUi) {
        cancelJobs()

        _state.update { it.copy(selectedCoin = coinUi) }

        historyJob = viewModelScope.launch {
            coinDataSource
                .getCoinHistory(
                    coinId = coinUi.id,
                    start = ZonedDateTime.now().minusDays(5),
                    end = ZonedDateTime.now()
                )
                .onSuccess { history ->

                    history.collectLatest { coinPrices ->

                        Log.d("TAG", "selectCoin: ${coinPrices.size}")
                        _state.update {
                            val updatedPrice = coinPrices.last().priceUsd.toDisplayableNumber()
                            val updatedCoin = it.selectedCoin?.copy(priceUsd = updatedPrice)

                            it.copy(selectedCoin = updatedCoin)
                        }

                    }

                }
                .onError { error ->
                    _events.send(CoinListEvent.Error(error))
                }
        }

    }

    override fun onPause(owner: LifecycleOwner) {
        Log.d("TAG", "onPause")
        cancelJobs()
        super.onPause(owner)
    }

    private fun cancelJobs() {
        coinsJob?.cancel()
        historyJob?.cancel()
    }

}