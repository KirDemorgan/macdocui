package xyz.demorgan.macdockui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.demorgan.macdockui.config.SettingsManager
import xyz.demorgan.macdockui.docker.DockerManager
import java.awt.Desktop
import java.net.URI

class SettingsViewModel : ViewModel() {
    private val _macOSVersion = MutableStateFlow(SettingsManager.macOSVersion)
    val macOSVersion: StateFlow<String> = _macOSVersion.asStateFlow()

    private val _ramSize = MutableStateFlow(SettingsManager.ramSize)
    val ramSize: StateFlow<String> = _ramSize.asStateFlow()

    private val _storagePath = MutableStateFlow(SettingsManager.storagePath)
    val storagePath: StateFlow<String> = _storagePath.asStateFlow()

    private val _rememberContainer = MutableStateFlow(SettingsManager.rememberContainer)
    val rememberContainer: StateFlow<Boolean> = _rememberContainer.asStateFlow()
    
    private val _hasExistingContainer = MutableStateFlow(false)
    val hasExistingContainer: StateFlow<Boolean> = _hasExistingContainer.asStateFlow()

    private val _isStarting = MutableStateFlow(false)
    val isStarting: StateFlow<Boolean> = _isStarting.asStateFlow()

    private val _startingProgress = MutableStateFlow("")
    val startingProgress: StateFlow<String> = _startingProgress.asStateFlow()

    init {
        checkExistingContainer()
    }

    private fun checkExistingContainer() {
        viewModelScope.launch {
            _hasExistingContainer.value = DockerManager.checkExistingContainer()
        }
    }

    fun updateMacOSVersion(version: String) {
        _macOSVersion.value = version
        SettingsManager.macOSVersion = version
    }

    fun updateRamSize(size: String) {
        _ramSize.value = size
        SettingsManager.ramSize = size
    }

    fun updateStoragePath(path: String) {
        _storagePath.value = path
        SettingsManager.storagePath = path
    }

    fun updateRememberContainer(remember: Boolean) {
        _rememberContainer.value = remember
        SettingsManager.rememberContainer = remember
        if (!remember) {
            SettingsManager.lastContainerId = ""
            _hasExistingContainer.value = false
        }
    }
    
    fun createNewContainer() {
        _hasExistingContainer.value = false
        SettingsManager.lastContainerId = ""
    }

    fun startContainer(onSuccess: () -> Unit) {
        if (_storagePath.value.isEmpty()) return

        viewModelScope.launch {
            _isStarting.value = true
            _startingProgress.value = "Starting..."
            
            val success = DockerManager.startMacOSContainer(
                version = _macOSVersion.value,
                ramSize = _ramSize.value,
                storagePath = _storagePath.value
            ) { progress ->
                _startingProgress.value = progress
            }

            if (success) {
                _startingProgress.value = "Opening web interface..."
                onSuccess()
                try {
                    Desktop.getDesktop().browse(URI("http://127.0.0.1:8006/"))
                } catch (e: Exception) {

                }
            }
            _isStarting.value = false
            _startingProgress.value = ""
        }
    }
    
    fun startExistingContainer(onSuccess: () -> Unit) {
         viewModelScope.launch {
            _isStarting.value = true
            _startingProgress.value = "Starting existing container..."
            
            val success = DockerManager.startExistingContainer()
            
            if (success) {
                onSuccess()
                try {
                    Desktop.getDesktop().browse(URI("http://127.0.0.1:8006/"))
                } catch (e: Exception) {
                    // Ignore
                }
            }
            _isStarting.value = false
            _startingProgress.value = ""
        }
    }
}
