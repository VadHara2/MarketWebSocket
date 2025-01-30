package com.vadhara7.marketwebsocket.crypto.presentation.coin_list

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vadhara7.marketwebsocket.R
import com.vadhara7.marketwebsocket.crypto.presentation.coin_list.components.CoinListItem
import com.vadhara7.marketwebsocket.crypto.presentation.coin_list.components.DropdownMenuButton
import com.vadhara7.marketwebsocket.crypto.presentation.coin_list.components.FilterDropdown
import com.vadhara7.marketwebsocket.crypto.presentation.coin_list.components.previewCoin
import com.vadhara7.marketwebsocket.ui.theme.MarketWebSocketTheme

@Composable
fun CoinListScreen(
    state: CoinListState,
    modifier: Modifier = Modifier,
    onAction: (CoinListAction) -> Unit,
    listState: LazyListState
) {
    val contentColor = if (isSystemInDarkTheme()) {
        Color.White
    } else {
        Color.Black
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            DropdownMenuButton(
                selectedInterval = state.refreshInterval,
                onIntervalChange = { newInterval ->
                    onAction(CoinListAction.OnIntervalChange(newInterval))
                }
            )
            FilterDropdown(
                selectedFilter = state.selectedFilter,
                onFilterSelected = { newFilter -> onAction(CoinListAction.OnFilterSelect(newFilter)) }
            )
        }

        Text(
            text = stringResource(R.string.last_updated, state.lastUpdated ?: "N/A"),
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            color = contentColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 16.dp)
        )


        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = listState
            ) {
                items(state.coins) { coinUi ->
                    CoinListItem(
                        coinUi = coinUi,
                        onClick = { onAction(CoinListAction.OnCoinClick(coinUi)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CoinListScreenPreview() {
    MarketWebSocketTheme {
        CoinListScreen(
            state = CoinListState(
                coins = (1..100).map {
                    previewCoin.copy(id = it.toString())
                },
                lastUpdated = "12:34:56"
            ),
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            onAction = {},
            listState = rememberLazyListState()
        )
    }
}
