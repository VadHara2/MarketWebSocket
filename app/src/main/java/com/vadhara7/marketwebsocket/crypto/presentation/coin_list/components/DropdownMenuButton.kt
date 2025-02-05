package com.vadhara7.marketwebsocket.crypto.presentation.coin_list.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.vadhara7.marketwebsocket.R

@Composable
fun DropdownMenuButton(selectedInterval: Int, onIntervalChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val intervals = listOf(1, 2, 5)

    Box(modifier = Modifier.wrapContentSize()) {
        Button(onClick = { expanded = true }) {
            Text(stringResource(R.string.refresh_interval, selectedInterval))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            intervals.forEach { interval ->
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.interval_sec, interval)) },
                    onClick = {
                        onIntervalChange(interval)
                        expanded = false
                    }
                )
            }
        }
    }
}