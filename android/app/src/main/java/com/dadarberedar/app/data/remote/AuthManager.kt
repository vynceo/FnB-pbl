package com.dadarberedar.app.data.remote

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.dadarberedar.app.data.model.AuthResponse
import com.dadarberedar.app.data.model.LoginRequest
import com.dadarberedar.app.data.model.RegisterRequest
import com.dadarberedar.app.data.model.User
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    private val apiService: ApiService,
    private val dataStore: DataStore<Preferences>
) {
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!
                saveToken(auth.token)
                saveUser(auth.user)
                Result.success(auth)
            } else {
                val error = parseErrorMessage(response.errorBody()?.string(), "Email atau password salah")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Gagal terhubung ke server"))
        }
    }

    suspend fun register(name: String, email: String, password: String, phone: String?): Result<AuthResponse> {
        return try {
            val response = apiService.register(
                RegisterRequest(
                    name = name,
                    email = email,
                    password = password,
                    passwordConfirmation = password,
                    phone = phone
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!
                saveToken(auth.token)
                saveUser(auth.user)
                Result.success(auth)
            } else {
                val error = parseErrorMessage(response.errorBody()?.string(), "Registrasi gagal")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Gagal terhubung ke server"))
        }
    }

    suspend fun logout() {
        val token = getToken()
        if (token != null) {
            try { apiService.logout("Bearer $token") } catch (_: Exception) {}
        }
        clearAuth()
    }

    suspend fun getToken(): String? {
        return dataStore.data.map { it[AuthPrefs.TOKEN_KEY] }.first()
    }

    suspend fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    suspend fun getUserName(): String? {
        return dataStore.data.map { it[AuthPrefs.USER_NAME_KEY] }.first()
    }

    suspend fun getUserEmail(): String? {
        return dataStore.data.map { it[AuthPrefs.USER_EMAIL_KEY] }.first()
    }

    suspend fun getUserPhone(): String? {
        return dataStore.data.map { it[AuthPrefs.USER_PHONE_KEY] }.first()
    }

    private suspend fun saveToken(token: String) {
        dataStore.edit { it[AuthPrefs.TOKEN_KEY] = token }
    }

    private suspend fun saveUser(user: User) {
        dataStore.edit {
            it[AuthPrefs.USER_NAME_KEY] = user.name
            it[AuthPrefs.USER_EMAIL_KEY] = user.email
            if (!user.phone.isNullOrBlank()) {
                it[AuthPrefs.USER_PHONE_KEY] = user.phone
            } else {
                it.remove(AuthPrefs.USER_PHONE_KEY)
            }
        }
    }

    private suspend fun clearAuth() {
        dataStore.edit {
            it.remove(AuthPrefs.TOKEN_KEY)
            it.remove(AuthPrefs.USER_NAME_KEY)
            it.remove(AuthPrefs.USER_EMAIL_KEY)
            it.remove(AuthPrefs.USER_PHONE_KEY)
        }
    }

    private fun parseErrorMessage(raw: String?, fallback: String): String {
        if (raw.isNullOrBlank()) return fallback
        return try {
            val json = JsonParser.parseString(raw).asJsonObject
            if (json.has("errors")) {
                val errors = json.getAsJsonObject("errors")
                val firstEntry = errors.entrySet().firstOrNull()
                val firstVal = firstEntry?.value
                if (firstVal != null && firstVal.isJsonArray && firstVal.asJsonArray.size() > 0) {
                    return firstVal.asJsonArray[0].asString
                }
            }
            json.get("message")?.asString ?: fallback
        } catch (_: Exception) {
            fallback
        }
    }
}
