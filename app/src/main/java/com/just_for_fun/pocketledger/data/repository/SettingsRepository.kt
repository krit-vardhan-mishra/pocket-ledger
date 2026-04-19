package com.just_for_fun.pocketledger.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.just_for_fun.pocketledger.data.model.AppThemeMode
import com.just_for_fun.pocketledger.data.model.AppSettings
import com.just_for_fun.pocketledger.di.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val dispatchers: AppCoroutineDispatchers = AppCoroutineDispatchers()
) {

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            notificationsEnabled = prefs[NOTIFICATIONS_ENABLED] ?: false,
            notificationHour = prefs[NOTIFICATION_HOUR] ?: 20,
            notificationMinute = prefs[NOTIFICATION_MINUTE] ?: 0,
            exceedAlertsEnabled = prefs[EXCEED_ALERTS_ENABLED] ?: true,
            useTempData = prefs[USE_TEMP_DATA] ?: false,
            themeMode = prefs[THEME_MODE]
                ?.let { raw ->
                    AppThemeMode.entries.firstOrNull { it.name == raw }
                }
                ?: AppThemeMode.DEFAULT
        )
    }

    suspend fun getSettingsSnapshot(): AppSettings = withContext(dispatchers.io) {
        settings.first()
    }

    suspend fun updateSettings(newSettings: AppSettings) {
        withContext(dispatchers.io) {
            dataStore.edit { prefs ->
                prefs[NOTIFICATIONS_ENABLED] = newSettings.notificationsEnabled
                prefs[NOTIFICATION_HOUR] = newSettings.notificationHour
                prefs[NOTIFICATION_MINUTE] = newSettings.notificationMinute
                prefs[EXCEED_ALERTS_ENABLED] = newSettings.exceedAlertsEnabled
                prefs[USE_TEMP_DATA] = newSettings.useTempData
                prefs[THEME_MODE] = newSettings.themeMode.name
            }
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        withContext(dispatchers.io) {
            dataStore.edit { prefs ->
                prefs[NOTIFICATIONS_ENABLED] = enabled
            }
        }
    }

    suspend fun updateNotificationTime(hour: Int, minute: Int) {
        withContext(dispatchers.io) {
            dataStore.edit { prefs ->
                prefs[NOTIFICATION_HOUR] = hour.coerceIn(0, 23)
                prefs[NOTIFICATION_MINUTE] = minute.coerceIn(0, 59)
            }
        }
    }

    suspend fun updateExceedAlertsEnabled(enabled: Boolean) {
        withContext(dispatchers.io) {
            dataStore.edit { prefs ->
                prefs[EXCEED_ALERTS_ENABLED] = enabled
            }
        }
    }

    suspend fun updateUseTempData(enabled: Boolean) {
        withContext(dispatchers.io) {
            dataStore.edit { prefs ->
                prefs[USE_TEMP_DATA] = enabled
            }
        }
    }

    suspend fun updateThemeMode(mode: AppThemeMode) {
        withContext(dispatchers.io) {
            dataStore.edit { prefs ->
                prefs[THEME_MODE] = mode.name
            }
        }
    }

    private companion object {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
        val EXCEED_ALERTS_ENABLED = booleanPreferencesKey("exceed_alerts_enabled")
        val USE_TEMP_DATA = booleanPreferencesKey("use_temp_data")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
