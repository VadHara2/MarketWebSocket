package com.vadhara7.marketwebsocket

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.vadhara7.marketwebsocket.core.navigation.AdaptiveCoinListDetailPane
import com.vadhara7.marketwebsocket.core.services.PriceMonitoringService
import com.vadhara7.marketwebsocket.ui.theme.MarketWebSocketTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startPriceMonitoringService()
        } else {
            // Дозвіл відхилено
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startPriceMonitoringService()
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        enableEdgeToEdge()
        setContent {
            MarketWebSocketTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AdaptiveCoinListDetailPane(
                        modifier = Modifier.padding(innerPadding),
                        lifecycle = lifecycle
                    )
                }
            }
        }
    }

    /**
     * Метод, який викликається з Composable або ViewModel,
     * щоб або одразу запустити сервіс, або запросити дозвіл.
     */
    fun requestNotificationPermissionOrStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startPriceMonitoringService()
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            startPriceMonitoringService()
        }
    }

    private fun startPriceMonitoringService() {
        val serviceIntent = Intent(this, PriceMonitoringService::class.java)
        startForegroundService(serviceIntent)
    }
}
