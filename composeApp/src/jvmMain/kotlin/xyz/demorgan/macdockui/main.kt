package xyz.demorgan.macdockui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.oshai.kotlinlogging.KotlinLogging
import macdockui.composeapp.generated.resources.Res
import macdockui.composeapp.generated.resources.icon
import org.jetbrains.compose.resources.painterResource
import javax.swing.SwingUtilities
import javax.swing.UIManager

private val logger = KotlinLogging.logger {}

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6750A4),
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFFEFBFF),
    onSurface = Color(0xFFFEFBFF),
)

fun main() = application {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (e: Exception) {
            logger.error(e) { "Error setting LookAndFeel: ${e.message}" }
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "macOS Docker UI",
        icon = painterResource(Res.drawable.icon)
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme
        ) {
            MacOSDockerApp()
        }
    }
}
