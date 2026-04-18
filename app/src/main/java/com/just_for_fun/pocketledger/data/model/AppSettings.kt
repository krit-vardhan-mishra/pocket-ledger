package com.just_for_fun.pocketledger.data.model

/**
 * Not a Room entity — stored in DataStore Preferences.
 * Controls the daily notification schedule.
 */
data class AppSettings(
    val notificationsEnabled: Boolean = false,
    val notificationHour: Int = 20,
    val notificationMinute: Int = 0,
    val exceedAlertsEnabled: Boolean = true
)
