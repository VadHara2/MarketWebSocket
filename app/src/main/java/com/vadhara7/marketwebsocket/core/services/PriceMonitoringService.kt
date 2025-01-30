package com.vadhara7.marketwebsocket.core.services

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.vadhara7.marketwebsocket.R
import com.vadhara7.marketwebsocket.core.domain.util.onError
import com.vadhara7.marketwebsocket.core.domain.util.onSuccess
import com.vadhara7.marketwebsocket.crypto.domain.Coin
import com.vadhara7.marketwebsocket.crypto.domain.CoinDataSource
import com.vadhara7.marketwebsocket.crypto.domain.FavoritesRepository
import com.vadhara7.marketwebsocket.crypto.presentation.models.toCoinUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.math.abs

class PriceMonitoringService : Service() {

    private val coinDataSource by inject<CoinDataSource>()
    private val favoritesRepository by inject<FavoritesRepository>()

    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        startMonitoring()
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setOngoing(true)
            .setContentTitle(getString(R.string.monitoring_favorite_coins))
            .setContentText(getString(R.string.service_is_running))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startMonitoring() {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                checkPriceChanges()
                delay(60_000) // Перевіряємо ціни раз на хвилину
            }
        }
    }

    private suspend fun checkPriceChanges() {
        val favorites = favoritesRepository.getFavoriteCoins()
        if (favorites.isEmpty()) return

        val coinsResult = coinDataSource.getCoins(refreshInterval = 1)
        coinsResult.onSuccess { flowCoins ->
            val latestCoins = flowCoins.firstOrNull() ?: return@onSuccess
            val favoriteCoins = latestCoins.filter { it.id in favorites }

            favoriteCoins.forEach { coin ->
                val change = coin.changePercent24Hr
                if (change >= 5.0 || change <= -5.0) {
                    sendNotification(coin)
                }
            }
        }.onError {
            Log.e(TAG, "checkPriceChanges: $it")
        }
    }

    private fun sendNotification(coin: Coin) {
        val absChange = abs(coin.changePercent24Hr)
        val coinUi = coin.toCoinUi()

        val title = getString(R.string.price_alert_title, coinUi.name)
        val text = getString(R.string.price_alert_text, coinUi.name, absChange)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(coinUi.iconRes)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(coin.id.hashCode(), notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "PriceMonitoringService"
        const val CHANNEL_ID = "price_monitoring_channel"
        const val NOTIFICATION_ID = 1
    }
}
