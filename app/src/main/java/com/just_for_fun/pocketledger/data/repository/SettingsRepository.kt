package com.just_for_fun.pocketledger.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.just_for_fun.pocketledger.data.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            notificationsEnabled = prefs[NOTIFICATIONS_ENABLED] ?: false,
            notificationHour = prefs[NOTIFICATION_HOUR] ?: 20,
            notificationMinute = prefs[NOTIFICATION_MINUTE] ?: 0,
            exceedAlertsEnabled = prefs[EXCEED_ALERTS_ENABLED] ?: true
        )
    }

    suspend fun getSettingsSnapshot(): AppSettings = settings.first()

    suspend fun updateSettings(newSettings: AppSettings) {
        dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = newSettings.notificationsEnabled
            prefs[NOTIFICATION_HOUR] = newSettings.notificationHour
            prefs[NOTIFICATION_MINUTE] = newSettings.notificationMinute
            prefs[EXCEED_ALERTS_ENABLED] = newSettings.exceedAlertsEnabled
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateNotificationTime(hour: Int, minute: Int) {
        dataStore.edit { prefs ->
            prefs[NOTIFICATION_HOUR] = hour.coerceIn(0, 23)
            prefs[NOTIFICATION_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    suspend fun updateExceedAlertsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[EXCEED_ALERTS_ENABLED] = enabled
        }
    }

    private companion object {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
        val EXCEED_ALERTS_ENABLED = booleanPreferencesKey("exceed_alerts_enabled")
    }
}
