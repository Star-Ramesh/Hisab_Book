package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = EmeraldPrimaryLight,
  onPrimary = Color(0xFF00382B),
  primaryContainer = EmeraldContainerDark,
  onPrimaryContainer = Color(0xFFA7F3D0),
  secondary = GoldAccent,
  onSecondary = Color(0xFF452B00),
  secondaryContainer = Color(0xFF78350F),
  onSecondaryContainer = GoldContainer,
  background = DarkBackground,
  onBackground = Color(0xFFF1F5F9),
  surface = DarkSurface,
  onSurface = Color(0xFFF1F5F9),
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = Color(0xFFCBD5E1),
  outline = Color(0xFF64748B),
  error = MoneyOutRoseDark
)

private val LightColorScheme = lightColorScheme(
  primary = EmeraldPrimary,
  onPrimary = Color.White,
  primaryContainer = EmeraldContainerLight,
  onPrimaryContainer = Color(0xFF064E3B),
  secondary = Color(0xFFB45309),
  onSecondary = Color.White,
  secondaryContainer = GoldContainer,
  onSecondaryContainer = Color(0xFF78350F),
  background = LightBackground,
  onBackground = Color(0xFF0F172A),
  surface = LightSurface,
  onSurface = Color(0xFF0F172A),
  surfaceVariant = LightSurfaceVariant,
  onSurfaceVariant = Color(0xFF475569),
  outline = Color(0xFFCBD5E1),
  error = MoneyOutRose
)

@Composable
fun MyKhataTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our signature emerald brand colors
  content: @Composable () -> Unit
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
