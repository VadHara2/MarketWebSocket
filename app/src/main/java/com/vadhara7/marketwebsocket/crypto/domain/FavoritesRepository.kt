package com.vadhara7.marketwebsocket.crypto.domain

interface FavoritesRepository {
    fun getFavoriteCoins(): Set<String>
    fun saveFavoriteCoins(coins: Set<String>)
}