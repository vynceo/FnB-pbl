package com.dadarberedar.app.ui.cart

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dadarberedar.app.data.model.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

data class StoredCartItem(
    val product: Product,
    val quantity: Int,
    val notes: String? = null
)

@Singleton
class CartStorage @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val gson = Gson()

    private object Keys {
        val ITEMS = stringPreferencesKey("cart_items_json")
        val OUTLET = longPreferencesKey("cart_outlet_id")
    }

    suspend fun save(items: List<CartItem>, outletId: Long?) {
        val stored = items.map { StoredCartItem(it.product, it.quantity, it.notes) }
        dataStore.edit { prefs ->
            prefs[Keys.ITEMS] = gson.toJson(stored)
            if (outletId != null) prefs[Keys.OUTLET] = outletId else prefs.remove(Keys.OUTLET)
        }
    }

    suspend fun load(): Pair<List<CartItem>, Long?> {
        val prefs = dataStore.data.first()
        val json = prefs[Keys.ITEMS]
        val items = if (json.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                val type = object : TypeToken<List<StoredCartItem>>() {}.type
                val stored: List<StoredCartItem> = gson.fromJson(json, type)
                stored.filter { it.product != null }
                    .map { CartItem(product = it.product, quantity = it.quantity, notes = it.notes) }
            } catch (_: Exception) {
                emptyList()
            }
        }
        return items to prefs[Keys.OUTLET]
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.ITEMS)
            prefs.remove(Keys.OUTLET)
        }
    }
}
