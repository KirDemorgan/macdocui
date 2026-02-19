package xyz.demorgan.macdockui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.demorgan.macdockui.components.AppLogsScreen
import xyz.demorgan.macdockui.components.DockerCheckScreen
import xyz.demorgan.macdockui.components.RunningScreen
import xyz.demorgan.macdockui.viewmodel.AppViewModel
import xyz.demorgan.macdockui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacOSDockerApp() {
    val viewModel: AppViewModel = viewModel { AppViewModel() }
    val currentScreen by viewModel.currentScreen.collectAsState()
    val showAppLogs by viewModel.showAppLogs.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.checkDocker()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("macOS Docker UI") },
                colors = TopAppBarDefaults.topAppBarColors(),
                actions = {
                    IconButton(onClick = viewModel::toggleAppLogs) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Article,
                            contentDescription = "App Logs"
                        )
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
            if (showAppLogs) {
                AppLogsScreen(
                    onClose = viewModel::toggleAppLogs
                )
            } else {
                Crossfade(targetState = currentScreen) { screen ->
                    when (screen) {
                        Screen.DOCKER_CHECK -> DockerCheckScreen(
                            viewModel = viewModel
                        )
                        Screen.SETTINGS -> SettingsScreen(
                            onStartDocker = { viewModel.navigateTo(Screen.RUNNING) }
                        )
                        Screen.RUNNING -> RunningScreen(
                            onStop = { viewModel.navigateTo(Screen.SETTINGS) }
                        )
                    }
                }
            }
        }
    }
}