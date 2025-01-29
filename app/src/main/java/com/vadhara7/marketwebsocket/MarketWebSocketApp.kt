package com.vadhara7.marketwebsocket

import android.app.Application
import com.vadhara7.marketwebsocket.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MarketWebSocketApp: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MarketWebSocketApp)
            androidLogger()

            modules(appModule)
        }
    }
}