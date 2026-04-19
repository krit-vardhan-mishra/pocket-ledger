package com.just_for_fun.pocketledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.just_for_fun.pocketledger.data.model.AppThemeMode
import com.just_for_fun.pocketledger.ui.PocketLedgerNavGraph
import com.just_for_fun.pocketledger.ui.settings.SettingsViewModel
import com.just_for_fun.pocketledger.ui.theme.PocketLedgerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settings by settingsViewModel.settings.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val selectedDarkTheme = when (settings.themeMode) {
                AppThemeMode.DEFAULT -> systemDark
                AppThemeMode.DARK -> true
                AppThemeMode.LIGHT -> false
            }

            PocketLedgerTheme(darkTheme = selectedDarkTheme) {
                PocketLedgerNavGraph(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}