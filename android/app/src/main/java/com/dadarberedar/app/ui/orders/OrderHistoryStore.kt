package com.dadarberedar.app.ui.orders

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderHistoryStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val gson = Gson()

    private object Keys {
        val NUMBERS = stringPreferencesKey("order_history_numbers")
    }

    suspend fun saveOrderNumber(orderNumber: String) {
        val current = getNumbers().toMutableList()
        if (!current.contains(orderNumber)) {
            current.add(0, orderNumber)
            if (current.size > MAX_ENTRIES) {
                repeat(current.size - MAX_ENTRIES) { current.removeAt(current.size - 1) }
            }
            dataStore.edit { it[Keys.NUMBERS] = gson.toJson(current) }
        }
    }

    suspend fun getNumbers(): List<String> {
        val json = dataStore.data.first()[Keys.NUMBERS]
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private companion object {
        const val MAX_ENTRIES = 50
    }
}
