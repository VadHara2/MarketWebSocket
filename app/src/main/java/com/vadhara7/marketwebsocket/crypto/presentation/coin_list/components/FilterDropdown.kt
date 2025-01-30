package com.vadhara7.marketwebsocket.crypto.presentation.coin_list.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.vadhara7.marketwebsocket.R
import com.vadhara7.marketwebsocket.core.presentation.util.CoinFilter

@Composable
fun FilterDropdown(selectedFilter: CoinFilter, onFilterSelected: (CoinFilter) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val filters = CoinFilter.values().toList()

    Box {
        Button(onClick = { expanded = true }) {
            Text(stringResource(R.string.filter, selectedFilter.name))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            filters.forEach { filter ->
                DropdownMenuItem(
                    text = { Text(filter.name) },
                    onClick = {
                        onFilterSelected(filter)
                        expanded = false
                    }
                )
            }
        }
    }
}
