package xyz.demorgan.macdockui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.oshai.kotlinlogging.KotlinLogging
import javax.swing.SwingUtilities
import javax.swing.UIManager

private val logger = KotlinLogging.logger {}

fun main() = application {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (e: Exception) {
            logger.error(e) { "Error setting look and feel ${e.message}" }
        }
    }

    Window(onCloseRequest = ::exitApplication, title = "Settings") {
        SettingsScreen()
    }
}