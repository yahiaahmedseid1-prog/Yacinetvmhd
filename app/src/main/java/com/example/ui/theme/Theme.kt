package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PremiumTeal,
    secondary = NeonCyan,
    tertiary = AccentPurple,
    background = CyberBlack,
    surface = ObsidianGrey,
    onPrimary = CyberBlack,
    onSecondary = CyberBlack,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary
  )

private val LightColorScheme = DarkColorScheme // Keep it consistently premium dark

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for premium cyber aesthetics
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve brand aesthetic
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
