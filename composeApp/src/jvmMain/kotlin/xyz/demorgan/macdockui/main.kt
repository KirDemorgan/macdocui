package xyz.demorgan.macdockui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.oshai.kotlinlogging.KotlinLogging
import macdockui.composeapp.generated.resources.Res
import macdockui.composeapp.generated.resources.icon
import org.jetbrains.compose.resources.painterResource
import xyz.demorgan.macdockui.ui.theme.AppTheme
import javax.swing.SwingUtilities
import javax.swing.UIManager

private val logger = KotlinLogging.logger {}

fun main() = application {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (e: Exception) {
            logger.error(e) { "Error setting LookAndFeel" }
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "macOS Docker UI",
        icon = painterResource(Res.drawable.icon)
    ) {
        AppTheme {
            MacOSDockerApp()
        }
    }
}
