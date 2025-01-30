package com.vadhara7.marketwebsocket.crypto.data

import android.content.Context
import com.vadhara7.marketwebsocket.crypto.domain.FavoritesRepository

class FavoritesRepositoryImpl(
    context: Context
) : FavoritesRepository {

    private val sharedPrefs =
        context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)

    override fun getFavoriteCoins(): Set<String> {
        return sharedPrefs.getStringSet("favorites_key", emptySet()) ?: emptySet()
    }

    override fun saveFavoriteCoins(coins: Set<String>) {
        sharedPrefs.edit()
            .putStringSet("favorites_key", coins)
        .apply()
    }
}