package com.jackscanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.jackscanner.domain.model.AppTheme

data class BlueMeanieColors(
    val primary: androidx.compose.ui.graphics.Color,
    val primaryVariant: androidx.compose.ui.graphics.Color,
    val secondary: androidx.compose.ui.graphics.Color,
    val background: androidx.compose.ui.graphics.Color,
    val surface: androidx.compose.ui.graphics.Color,
    val surfaceVariant: androidx.compose.ui.graphics.Color,
    val card: androidx.compose.ui.graphics.Color,
    val textPrimary: androidx.compose.ui.graphics.Color,
    val textSecondary: androidx.compose.ui.graphics.Color,
    val textTertiary: androidx.compose.ui.graphics.Color,
    val accentGlow: androidx.compose.ui.graphics.Color,
    val border: androidx.compose.ui.graphics.Color,
    val statusActive: androidx.compose.ui.graphics.Color,
    val statusWarning: androidx.compose.ui.graphics.Color,
    val statusDanger: androidx.compose.ui.graphics.Color,
    val statusGold: androidx.compose.ui.graphics.Color,
    val statusSilver: androidx.compose.ui.graphics.Color,
    val statusBronze: androidx.compose.ui.graphics.Color
)

val LocalBlueMeanieColors = staticCompositionLocalOf {
    BlueMeanieClassic.let {
        BlueMeanieColors(
            primary = it.Primary,
            primaryVariant = it.PrimaryVariant,
            secondary = it.Secondary,
            background = it.Background,
            surface = it.Surface,
            surfaceVariant = it.SurfaceVariant,
            card = it.Card,
            textPrimary = it.TextPrimary,
            textSecondary = it.TextSecondary,
            textTertiary = it.TextTertiary,
            accentGlow = it.AccentGlow,
            border = it.Border,
            statusActive = it.StatusActive,
            statusWarning = it.StatusWarning,
            statusDanger = it.StatusDanger,
            statusGold = it.StatusGold,
            statusSilver = it.StatusSilver,
            statusBronze = it.StatusBronze
        )
    }
}

fun getThemeColors(theme: AppTheme): BlueMeanieColors {
    return when (theme) {
        AppTheme.BLUE_MEANIE_CLASSIC -> BlueMeanieColors(
            primary = BlueMeanieClassic.Primary,
            primaryVariant = BlueMeanieClassic.PrimaryVariant,
            secondary = BlueMeanieClassic.Secondary,
            background = BlueMeanieClassic.Background,
            surface = BlueMeanieClassic.Surface,
            surfaceVariant = BlueMeanieClassic.SurfaceVariant,
            card = BlueMeanieClassic.Card,
            textPrimary = BlueMeanieClassic.TextPrimary,
            textSecondary = BlueMeanieClassic.TextSecondary,
            textTertiary = BlueMeanieClassic.TextTertiary,
            accentGlow = BlueMeanieClassic.AccentGlow,
            border = BlueMeanieClassic.Border,
            statusActive = BlueMeanieClassic.StatusActive,
            statusWarning = BlueMeanieClassic.StatusWarning,
            statusDanger = BlueMeanieClassic.StatusDanger,
            statusGold = BlueMeanieClassic.StatusGold,
            statusSilver = BlueMeanieClassic.StatusSilver,
            statusBronze = BlueMeanieClassic.StatusBronze
        )
        AppTheme.CARBON -> BlueMeanieColors(
            primary = Carbon.Primary,
            primaryVariant = Carbon.PrimaryVariant,
            secondary = Carbon.Secondary,
            background = Carbon.Background,
            surface = Carbon.Surface,
            surfaceVariant = Carbon.SurfaceVariant,
            card = Carbon.Card,
            textPrimary = Carbon.TextPrimary,
            textSecondary = Carbon.TextSecondary,
            textTertiary = Carbon.TextTertiary,
            accentGlow = Carbon.AccentGlow,
            border = Carbon.Border,
            statusActive = Carbon.StatusActive,
            statusWarning = Carbon.StatusWarning,
            statusDanger = Carbon.StatusDanger,
            statusGold = Carbon.StatusGold,
            statusSilver = Carbon.StatusSilver,
            statusBronze = Carbon.StatusBronze
        )
        AppTheme.TITANIUM -> BlueMeanieColors(
            primary = Titanium.Primary,
            primaryVariant = Titanium.PrimaryVariant,
            secondary = Titanium.Secondary,
            background = Titanium.Background,
            surface = Titanium.Surface,
            surfaceVariant = Titanium.SurfaceVariant,
            card = Titanium.Card,
            textPrimary = Titanium.TextPrimary,
            textSecondary = Titanium.TextSecondary,
            textTertiary = Titanium.TextTertiary,
            accentGlow = Titanium.AccentGlow,
            border = Titanium.Border,
            statusActive = Titanium.StatusActive,
            statusWarning = Titanium.StatusWarning,
            statusDanger = Titanium.StatusDanger,
            statusGold = Titanium.StatusGold,
            statusSilver = Titanium.StatusSilver,
            statusBronze = Titanium.StatusBronze
        )
        AppTheme.AURORA -> BlueMeanieColors(
            primary = Aurora.Primary,
            primaryVariant = Aurora.PrimaryVariant,
            secondary = Aurora.Secondary,
            background = Aurora.Background,
            surface = Aurora.Surface,
            surfaceVariant = Aurora.SurfaceVariant,
            card = Aurora.Card,
            textPrimary = Aurora.TextPrimary,
            textSecondary = Aurora.TextSecondary,
            textTertiary = Aurora.TextTertiary,
            accentGlow = Aurora.AccentGlow,
            border = Aurora.Border,
            statusActive = Aurora.StatusActive,
            statusWarning = Aurora.StatusWarning,
            statusDanger = Aurora.StatusDanger,
            statusGold = Aurora.StatusGold,
            statusSilver = Aurora.StatusSilver,
            statusBronze = Aurora.StatusBronze
        )
        AppTheme.MONOLITH -> BlueMeanieColors(
            primary = Monolith.Primary,
            primaryVariant = Monolith.PrimaryVariant,
            secondary = Monolith.Secondary,
            background = Monolith.Background,
            surface = Monolith.Surface,
            surfaceVariant = Monolith.SurfaceVariant,
            card = Monolith.Card,
            textPrimary = Monolith.TextPrimary,
            textSecondary = Monolith.TextSecondary,
            textTertiary = Monolith.TextTertiary,
            accentGlow = Monolith.AccentGlow,
            border = Monolith.Border,
            statusActive = Monolith.StatusActive,
            statusWarning = Monolith.StatusWarning,
            statusDanger = Monolith.StatusDanger,
            statusGold = Monolith.StatusGold,
            statusSilver = Monolith.StatusSilver,
            statusBronze = Monolith.StatusBronze
        )
        AppTheme.ARCTIC -> BlueMeanieColors(
            primary = Arctic.Primary,
            primaryVariant = Arctic.PrimaryVariant,
            secondary = Arctic.Secondary,
            background = Arctic.Background,
            surface = Arctic.Surface,
            surfaceVariant = Arctic.SurfaceVariant,
            card = Arctic.Card,
            textPrimary = Arctic.TextPrimary,
            textSecondary = Arctic.TextSecondary,
            textTertiary = Arctic.TextTertiary,
            accentGlow = Arctic.AccentGlow,
            border = Arctic.Border,
            statusActive = Arctic.StatusActive,
            statusWarning = Arctic.StatusWarning,
            statusDanger = Arctic.StatusDanger,
            statusGold = Arctic.StatusGold,
            statusSilver = Arctic.StatusSilver,
            statusBronze = Arctic.StatusBronze
        )
        AppTheme.MIDNIGHT -> BlueMeanieColors(
            primary = Midnight.Primary,
            primaryVariant = Midnight.PrimaryVariant,
            secondary = Midnight.Secondary,
            background = Midnight.Background,
            surface = Midnight.Surface,
            surfaceVariant = Midnight.SurfaceVariant,
            card = Midnight.Card,
            textPrimary = Midnight.TextPrimary,
            textSecondary = Midnight.TextSecondary,
            textTertiary = Midnight.TextTertiary,
            accentGlow = Midnight.AccentGlow,
            border = Midnight.Border,
            statusActive = Midnight.StatusActive,
            statusWarning = Midnight.StatusWarning,
            statusDanger = Midnight.StatusDanger,
            statusGold = Midnight.StatusGold,
            statusSilver = Midnight.StatusSilver,
            statusBronze = Midnight.StatusBronze
        )
        AppTheme.QUANTUM -> BlueMeanieColors(
            primary = Quantum.Primary,
            primaryVariant = Quantum.PrimaryVariant,
            secondary = Quantum.Secondary,
            background = Quantum.Background,
            surface = Quantum.Surface,
            surfaceVariant = Quantum.SurfaceVariant,
            card = Quantum.Card,
            textPrimary = Quantum.TextPrimary,
            textSecondary = Quantum.TextSecondary,
            textTertiary = Quantum.TextTertiary,
            accentGlow = Quantum.AccentGlow,
            border = Quantum.Border,
            statusActive = Quantum.StatusActive,
            statusWarning = Quantum.StatusWarning,
            statusDanger = Quantum.StatusDanger,
            statusGold = Quantum.StatusGold,
            statusSilver = Quantum.StatusSilver,
            statusBronze = Quantum.StatusBronze
        )
        AppTheme.NOVA -> BlueMeanieColors(
            primary = Nova.Primary,
            primaryVariant = Nova.PrimaryVariant,
            secondary = Nova.Secondary,
            background = Nova.Background,
            surface = Nova.Surface,
            surfaceVariant = Nova.SurfaceVariant,
            card = Nova.Card,
            textPrimary = Nova.TextPrimary,
            textSecondary = Nova.TextSecondary,
            textTertiary = Nova.TextTertiary,
            accentGlow = Nova.AccentGlow,
            border = Nova.Border,
            statusActive = Nova.StatusActive,
            statusWarning = Nova.StatusWarning,
            statusDanger = Nova.StatusDanger,
            statusGold = Nova.StatusGold,
            statusSilver = Nova.StatusSilver,
            statusBronze = Nova.StatusBronze
        )
        AppTheme.GLASS -> BlueMeanieColors(
            primary = GlassTheme.Primary,
            primaryVariant = GlassTheme.PrimaryVariant,
            secondary = GlassTheme.Secondary,
            background = GlassTheme.Background,
            surface = GlassTheme.Surface,
            surfaceVariant = GlassTheme.SurfaceVariant,
            card = GlassTheme.Card,
            textPrimary = GlassTheme.TextPrimary,
            textSecondary = GlassTheme.TextSecondary,
            textTertiary = GlassTheme.TextTertiary,
            accentGlow = GlassTheme.AccentGlow,
            border = GlassTheme.Border,
            statusActive = GlassTheme.StatusActive,
            statusWarning = GlassTheme.StatusWarning,
            statusDanger = GlassTheme.StatusDanger,
            statusGold = GlassTheme.StatusGold,
            statusSilver = GlassTheme.StatusSilver,
            statusBronze = GlassTheme.StatusBronze
        )
        AppTheme.SIREN -> BlueMeanieColors(
            primary = SirenTheme.Primary,
            primaryVariant = SirenTheme.PrimaryVariant,
            secondary = SirenTheme.Secondary,
            background = SirenTheme.Background,
            surface = SirenTheme.Surface,
            surfaceVariant = SirenTheme.SurfaceVariant,
            card = SirenTheme.Card,
            textPrimary = SirenTheme.TextPrimary,
            textSecondary = SirenTheme.TextSecondary,
            textTertiary = SirenTheme.TextTertiary,
            accentGlow = SirenTheme.AccentGlow,
            border = SirenTheme.Border,
            statusActive = SirenTheme.StatusActive,
            statusWarning = SirenTheme.StatusWarning,
            statusDanger = SirenTheme.StatusDanger,
            statusGold = SirenTheme.StatusGold,
            statusSilver = SirenTheme.StatusSilver,
            statusBronze = SirenTheme.StatusBronze
        )
    }
}

private fun createMaterialColorScheme(colors: BlueMeanieColors) = darkColorScheme(
    primary = colors.primary,
    onPrimary = colors.textPrimary,
    primaryContainer = colors.primaryVariant,
    secondary = colors.secondary,
    onSecondary = colors.textPrimary,
    background = colors.background,
    onBackground = colors.textPrimary,
    surface = colors.surface,
    onSurface = colors.textPrimary,
    surfaceVariant = colors.surfaceVariant,
    onSurfaceVariant = colors.textSecondary,
    outline = colors.border,
    error = colors.statusDanger,
    onError = colors.textPrimary
)

@Composable
fun BlueMeanieTheme(
    appTheme: AppTheme = AppTheme.BLUE_MEANIE_CLASSIC,
    content: @Composable () -> Unit
) {
    val colors = getThemeColors(appTheme)
    
    val materialColorScheme = createMaterialColorScheme(colors)
    
    CompositionLocalProvider(
        LocalBlueMeanieColors provides colors
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            content = content
        )
    }
}

object BlueMeanieTheme {
    val colors: BlueMeanieColors
        @Composable
        get() = LocalBlueMeanieColors.current
}