package xyz.demorgan.macdockui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.demorgan.macdockui.docker.DockerManager
import xyz.demorgan.macdockui.docker.DockerStatus

class AppViewModel : ViewModel() {
    private val _currentScreen = MutableStateFlow(Screen.DOCKER_CHECK)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _dockerStatus = MutableStateFlow(DockerStatus.CHECKING)
    val dockerStatus: StateFlow<DockerStatus> = _dockerStatus.asStateFlow()

    private val _showAppLogs = MutableStateFlow(false)
    val showAppLogs: StateFlow<Boolean> = _showAppLogs.asStateFlow()

    fun checkDocker() {
        viewModelScope.launch {
            _dockerStatus.value = DockerStatus.CHECKING
            _dockerStatus.value = DockerManager.checkDockerInstallation()
            
            if (_dockerStatus.value == DockerStatus.INSTALLED) {
                if (DockerManager.isContainerRunning()) {
                    _currentScreen.value = Screen.RUNNING
                } else {
                    _currentScreen.value = Screen.SETTINGS
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun toggleAppLogs() {
        _showAppLogs.value = !_showAppLogs.value
    }
}

enum class Screen {
    DOCKER_CHECK,
    SETTINGS,
    RUNNING
}
