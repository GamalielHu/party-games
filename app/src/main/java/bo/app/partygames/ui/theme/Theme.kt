package bo.app.partygames.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ImpostorRed = Color(0xFFF43F5E)
val CrewGreen = Color(0xFF34D399)

private val PartyColors = darkColorScheme(
    primary = Color(0xFFA78BFA),
    onPrimary = Color(0xFF1E1036),
    primaryContainer = Color(0xFF4C1D95),
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = Color(0xFFFACC15),
    onSecondary = Color(0xFF2A2100),
    secondaryContainer = Color(0xFF3F3410),
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = Color(0xFFF472B6),
    error = ImpostorRed,
    background = Color(0xFF15121F),
    onBackground = Color(0xFFECE8F6),
    surface = Color(0xFF15121F),
    onSurface = Color(0xFFECE8F6),
    surfaceVariant = Color(0xFF2A2538),
    onSurfaceVariant = Color(0xFFC9C2DB),
    surfaceContainer = Color(0xFF211C2E),
    surfaceContainerHigh = Color(0xFF2A2538),
    outline = Color(0xFF6F6880),
)

@Composable
fun PartyGamesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PartyColors,
        typography = Typography(),
        content = content,
    )
}
