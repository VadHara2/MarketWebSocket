@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package com.vadhara7.marketwebsocket.core.navigation

import android.widget.Toast
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vadhara7.marketwebsocket.core.presentation.util.ObserveAsEvents
import com.vadhara7.marketwebsocket.core.presentation.util.toString
import com.vadhara7.marketwebsocket.crypto.presentation.coin_detail.CoinDetailScreen
import com.vadhara7.marketwebsocket.crypto.presentation.coin_list.CoinListAction
import com.vadhara7.marketwebsocket.crypto.presentation.coin_list.CoinListEvent
import com.vadhara7.marketwebsocket.crypto.presentation.coin_list.CoinListScreen
import com.vadhara7.marketwebsocket.crypto.presentation.coin_list.CoinListViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AdaptiveCoinListDetailPane(
    modifier: Modifier = Modifier,
    viewModel: CoinListViewModel = koinViewModel(),
    lifecycle: Lifecycle
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    lifecycle.addObserver(viewModel)

    ObserveAsEvents(events = viewModel.events) { event ->
        when (event) {
            is CoinListEvent.Error -> {
                Toast.makeText(
                    context,
                    event.error.toString(context),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
    val listState = rememberLazyListState()

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        listPane = {
            AnimatedPane {
                CoinListScreen(
                    state = state,
                    listState = listState,
                    onAction = { action ->
                        viewModel.onAction(action)
                        when (action) {
                            is CoinListAction.OnCoinClick -> {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail
                                )
                            }
                            else -> { }
                        }
                    }
                )
            }
        },
        detailPane = {
            AnimatedPane {
                CoinDetailScreen(state = state)
            }
        },
        modifier = modifier
    )
}