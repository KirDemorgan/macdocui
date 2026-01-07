package xyz.demorgan.macdockui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import macdockui.composeapp.generated.resources.Res
import macdockui.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Settings") {
        SettingsScreen()
    }
}