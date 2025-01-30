package com.vadhara7.marketwebsocket.crypto.presentation.coin_list

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vadhara7.marketwebsocket.core.domain.util.onError
import com.vadhara7.marketwebsocket.core.domain.util.onSuccess
import com.vadhara7.marketwebsocket.crypto.domain.CoinDataSource
import com.vadhara7.marketwebsocket.crypto.presentation.models.toCoinUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CoinListViewModel(
    private val coinDataSource: CoinDataSource
): ViewModel(), DefaultLifecycleObserver {

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

    fun onAction(action: CoinListAction) {
        when(action) {
            is CoinListAction.OnCoinClick -> {
                _state.update { it.copy(selectedCoin = action.coinUi) }
            }
        }
    }

    private fun loadCoins() {
        coinsJob = viewModelScope.launch {
            coinDataSource.getCoins().collect { result ->
                Log.d("TAG", "loadCoins: $result")
                result.onSuccess { coins ->

                    _state.update { it.copy(
                        isLoading = false,
                        coins = coins.map { it.toCoinUi() }
                    ) }

                }.onError { error ->

                    _state.update { it.copy(isLoading = false) }
                    _events.send(CoinListEvent.Error(error))

                }
            }
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        Log.d("TAG", "onPause")
        coinsJob?.cancel()
        super.onPause(owner)
    }

}