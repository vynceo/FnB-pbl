package com.dadarberedar.app.data.remote

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.dadarberedar.app.data.model.AuthResponse
import com.dadarberedar.app.data.model.LoginRequest
import com.dadarberedar.app.data.model.RegisterRequest
import com.dadarberedar.app.data.model.User
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
            if (response.isSuccessful) {
                val auth = response.body()!!
                saveToken(auth.token)
                saveUser(auth.user)
                Result.success(auth)
            } else {
                val error = response.errorBody()?.string() ?: "Login gagal"
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
            if (response.isSuccessful) {
                val auth = response.body()!!
                saveToken(auth.token)
                saveUser(auth.user)
                Result.success(auth)
            } else {
                val error = response.errorBody()?.string() ?: "Register gagal"
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(e)
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

    suspend fun isLoggedIn(): Boolean = getToken() != null

    suspend fun getUserName(): String? {
        return dataStore.data.map { it[AuthPrefs.USER_NAME_KEY] }.first()
    }

    suspend fun getUserEmail(): String? {
        return dataStore.data.map { it[AuthPrefs.USER_EMAIL_KEY] }.first()
    }

    private suspend fun saveToken(token: String) {
        dataStore.edit { it[AuthPrefs.TOKEN_KEY] = token }
    }

    private suspend fun saveUser(user: User) {
        dataStore.edit {
            it[AuthPrefs.USER_NAME_KEY] = user.name
            it[AuthPrefs.USER_EMAIL_KEY] = user.email
        }
    }

    private suspend fun clearAuth() {
        dataStore.edit { it.clear() }
    }
}
