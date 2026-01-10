package xyz.demorgan.macdockui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import xyz.demorgan.macdockui.components.AppLogsScreen
import xyz.demorgan.macdockui.components.DockerCheckScreen
import xyz.demorgan.macdockui.components.RunningScreen
import xyz.demorgan.macdockui.docker.DockerManager
import xyz.demorgan.macdockui.docker.DockerStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacOSDockerApp() {
    var currentScreen by remember { mutableStateOf(Screen.DOCKER_CHECK) }
    var dockerStatus by remember { mutableStateOf(DockerStatus.CHECKING) }
    var showLogs by remember { mutableStateOf(false) }
    var showAppLogs by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        dockerStatus = DockerManager.checkDockerInstallation()
        if (dockerStatus == DockerStatus.INSTALLED) {
            currentScreen = if (DockerManager.isContainerRunning()) {
                Screen.RUNNING
            } else {
                Screen.SETTINGS
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("macOS Docker UI") },
                actions = {
                    IconButton(onClick = { showAppLogs = !showAppLogs }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Article,
                            contentDescription = "Логи приложения"
                        )
                    }
                    
                    if (currentScreen == Screen.RUNNING) {
                        IconButton(onClick = { showLogs = !showLogs }) {
                            Icon(
                                imageVector = if (showLogs) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showLogs) "Скрыть логи контейнера" else "Показать логи контейнера"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                showAppLogs -> AppLogsScreen(
                    onClose = { showAppLogs = false }
                )
                else -> {
                    when (currentScreen) {
                        Screen.DOCKER_CHECK -> DockerCheckScreen(
                            dockerStatus = dockerStatus,
                            onRetry = {
                                dockerStatus = DockerStatus.CHECKING
                                dockerStatus = DockerManager.checkDockerInstallation()
                                if (dockerStatus == DockerStatus.INSTALLED) {
                                    currentScreen = if (DockerManager.isContainerRunning()) {
                                        Screen.RUNNING
                                    } else {
                                        Screen.SETTINGS
                                    }
                                }
                            }
                        )
                        Screen.SETTINGS -> SettingsScreen(
                            onStartDocker = { currentScreen = Screen.RUNNING }
                        )
                        Screen.RUNNING -> RunningScreen(
                            showLogs = showLogs,
                            onStop = { currentScreen = Screen.SETTINGS }
                        )
                    }
                }
            }
        }
    }
}

enum class Screen {
    DOCKER_CHECK,
    SETTINGS,
    RUNNING
}