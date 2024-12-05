package com.passer.passwatch.core.repo

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    private companion object {
        val HUB_MAC_ADDRESS = stringPreferencesKey("hub_mac_address")
        val GO_PRO_WIFI = stringPreferencesKey("go_pro_wifi")
        val GO_PRO_PASSWORD = stringPreferencesKey("go_pro_password")
        const val TAG = "UserPreferencesRepo"
    }

    val hubMacAddress: Flow<String> = dataStore.data.catch {
        if (it is IOException) {
            Log.e(TAG, "Error reading preferences.", it)
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->
        preferences[HUB_MAC_ADDRESS] ?: ""
    }

    suspend fun saveHubMacAddress(hubMacAddress: String) {
        dataStore.edit { preferences ->
            preferences[HUB_MAC_ADDRESS] = hubMacAddress
        }
    }

    val goProWiFi: Flow<String> = dataStore.data.catch {
        if (it is IOException) {
            Log.e(TAG, "Error reading preferences.", it)
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->
        preferences[GO_PRO_WIFI] ?: ""
    }

    suspend fun saveGoProWiFi(goProWiFi: String) {
        dataStore.edit { preferences ->
            preferences[GO_PRO_WIFI] = goProWiFi
        }
    }

    val goProPassword: Flow<String> = dataStore.data.catch {
        if (it is IOException) {
            Log.e(TAG, "Error reading preferences.", it)
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->
        preferences[GO_PRO_PASSWORD] ?: ""
    }

    suspend fun saveGoProPassword(goProPassword: String) {
        dataStore.edit { preferences ->
            preferences[GO_PRO_PASSWORD] = goProPassword
        }
    }
}