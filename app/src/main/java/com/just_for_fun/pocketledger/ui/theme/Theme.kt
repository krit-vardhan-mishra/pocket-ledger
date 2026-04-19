package com.just_for_fun.pocketledger.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary              = Amber40,
    onPrimary            = Color.White,
    primaryContainer     = Amber90,
    onPrimaryContainer   = Amber10,
    secondary            = Taupe40,
    onSecondary          = Color.White,
    secondaryContainer   = Taupe90,
    onSecondaryContainer = Taupe10,
    tertiary             = Sage40,
    onTertiary           = Color.White,
    tertiaryContainer    = Sage90,
    onTertiaryContainer  = Sage10,
    error                = Red40,
    onError              = Color.White,
    errorContainer       = Red90,
    onErrorContainer     = Red10,
    background           = Neutral99,
    onBackground         = Neutral10,
    surface              = Neutral99,
    onSurface            = Neutral10,
    surfaceVariant       = NeutralVar90,
    onSurfaceVariant     = NeutralVar30,
    outline              = NeutralVar50,
    outlineVariant       = NeutralVar80,
    inverseSurface       = Neutral20,
    inverseOnSurface     = Neutral95,
    inversePrimary       = Amber80,
    surfaceTint          = Amber40,
)

private val DarkColorScheme = darkColorScheme(
    primary              = Amber80,
    onPrimary            = Amber20,
    primaryContainer     = Amber30,
    onPrimaryContainer   = Amber90,
    secondary            = Taupe80,
    onSecondary          = Taupe20,
    secondaryContainer   = Taupe30,
    onSecondaryContainer = Taupe90,
    tertiary             = Sage80,
    onTertiary           = Sage20,
    tertiaryContainer    = Sage30,
    onTertiaryContainer  = Sage90,
    error                = Red80,
    onError              = Red20,
    errorContainer       = Red30,
    onErrorContainer     = Red90,
    background           = Neutral10,
    onBackground         = Neutral90,
    surface              = Neutral10,
    onSurface            = Neutral90,
    surfaceVariant       = NeutralVar30,
    onSurfaceVariant     = NeutralVar80,
    outline              = NeutralVar60,
    outlineVariant       = NeutralVar30,
    inverseSurface       = Neutral90,
    inverseOnSurface     = Neutral10,
    inversePrimary       = Amber40,
    surfaceTint          = Amber80,
)

@Composable
fun PocketLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set to false to use the hand-crafted brand palette; set to true on
    // Android 12+ to adopt the user's wallpaper-derived scheme.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}