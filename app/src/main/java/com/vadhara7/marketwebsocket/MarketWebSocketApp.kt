package com.vadhara7.marketwebsocket

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.vadhara7.marketwebsocket.core.services.PriceMonitoringService
import com.vadhara7.marketwebsocket.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MarketWebSocketApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MarketWebSocketApp)
            androidLogger()

            modules(appModule)
        }
        createNotificationChannel()
    }

    private fun createNotificationChannel() {

        val channel = NotificationChannel(
            PriceMonitoringService.CHANNEL_ID,
            getString(R.string.price_monitoring_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.price_monitoring_channel_description)
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

}