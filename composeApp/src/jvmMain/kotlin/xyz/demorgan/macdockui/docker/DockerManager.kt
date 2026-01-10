package xyz.demorgan.macdockui.docker

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.io.File
import java.io.IOException
import java.net.URI
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

enum class DockerStatus {
    CHECKING,
    INSTALLED,
    NOT_INSTALLED,
    ERROR,
    DOCKER_DESKTOP_NOT_RUNNING,
    WSL_REQUIRED
}

data class AppLog(
    val timestamp: LocalDateTime,
    val level: LogLevel,
    val message: String
)

enum class LogLevel {
    INFO, WARNING, ERROR, SUCCESS
}

object DockerManager {
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs
    
    private val _appLogs = MutableStateFlow<List<AppLog>>(emptyList())
    val appLogs: StateFlow<List<AppLog>> = _appLogs
    
    private var dockerProcess: Process? = null
    
    private fun addAppLog(level: LogLevel, message: String) {
        val log = AppLog(LocalDateTime.now(), level, message)
        _appLogs.value = _appLogs.value + log
        logger.info { "[$level] $message" }
    }
    
    fun checkDockerInstallation(): DockerStatus {
        addAppLog(LogLevel.INFO, "Проверка установки Docker...")
        
        return try {
            val dockerVersionProcess = ProcessBuilder("docker", "--version")
                .redirectErrorStream(true)
                .start()
            
            val dockerExitCode = dockerVersionProcess.waitFor()
            if (dockerExitCode != 0) {
                addAppLog(LogLevel.ERROR, "Docker не установлен")
                return DockerStatus.NOT_INSTALLED
            }
            
            addAppLog(LogLevel.SUCCESS, "Docker установлен")
            
            val dockerInfoProcess = ProcessBuilder("docker", "info")
                .redirectErrorStream(true)
                .start()
            
            val dockerInfoExitCode = dockerInfoProcess.waitFor()
            if (dockerInfoExitCode != 0) {
                addAppLog(LogLevel.WARNING, "Docker Desktop не запущен")
                return DockerStatus.DOCKER_DESKTOP_NOT_RUNNING
            }
            
            addAppLog(LogLevel.SUCCESS, "Docker Desktop запущен")
            
            if (isWindows() && !checkWSLAvailability()) {
                addAppLog(LogLevel.WARNING, "WSL не настроен для работы с Docker")
                return DockerStatus.WSL_REQUIRED
            }
            
            addAppLog(LogLevel.SUCCESS, "Все требования выполнены, Docker готов к работе")
            DockerStatus.INSTALLED
            
        } catch (e: IOException) {
            logger.error(e) { "Error checking Docker installation" }
            addAppLog(LogLevel.ERROR, "Ошибка при проверке Docker: ${e.message}")
            DockerStatus.NOT_INSTALLED
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error checking Docker" }
            addAppLog(LogLevel.ERROR, "Неожиданная ошибка: ${e.message}")
            DockerStatus.ERROR
        }
    }
    
    private fun isWindows(): Boolean {
        return System.getProperty("os.name").lowercase().contains("windows")
    }

    private fun checkWSLAvailability(): Boolean {
        return try {
            addAppLog(LogLevel.INFO, "Проверка WSL...")

            val processBuilder = ProcessBuilder("wsl", "--list", "--verbose")
            val wslProcess = processBuilder.start()

            val wslOutput = wslProcess.inputStream.bufferedReader(Charsets.UTF_16LE).use { it.readText() }
                .ifBlank {
                    ProcessBuilder("wsl", "--list", "--verbose").start().inputStream.bufferedReader().readText()
                }

            wslProcess.waitFor()

            logger.debug { "WSL Output: $wslOutput" }

            val hasDistros = wslOutput.contains("Running", ignoreCase = true) ||
                    wslOutput.contains("Stopped", ignoreCase = true) ||
                    wslOutput.contains("Ubuntu", ignoreCase = true)

            if (hasDistros) {
                addAppLog(LogLevel.SUCCESS, "WSL настроен и готов к работе")

                return checkKVMAvailability()
            } else {
                addAppLog(LogLevel.WARNING, "Не найдено дистрибутивов WSL")
                return false
            }
        } catch (e: Exception) {
            logger.error(e) { "Error checking WSL" }
            addAppLog(LogLevel.WARNING, "Ошибка при проверке WSL: ${e.message}")
            false
        }
    }

    
    private fun checkKVMAvailability(): Boolean {
        return try {
            addAppLog(LogLevel.INFO, "Проверка доступности KVM...")
            
            val testProcess = ProcessBuilder(
                "docker", "run", "--rm", "--device=/dev/kvm", 
                "alpine:latest", "ls", "/dev/kvm"
            ).redirectErrorStream(true).start()
            
            val testOutput = testProcess.inputStream.bufferedReader().readText()
            val testExitCode = testProcess.waitFor()
            
            if (testExitCode == 0 && testOutput.contains("/dev/kvm")) {
                addAppLog(LogLevel.SUCCESS, "KVM доступен через Docker")
                true
            } else {
                addAppLog(LogLevel.WARNING, "KVM недоступен: $testOutput")
                false
            }
        } catch (e: Exception) {
            logger.error(e) { "Error checking KVM availability" }
            addAppLog(LogLevel.WARNING, "Не удалось проверить KVM: ${e.message}")
            false
        }
    }
    
    fun openWSLInstallPage() {
        try {
            addAppLog(LogLevel.INFO, "Открытие страницы установки WSL...")
            Desktop.getDesktop().browse(URI("https://docs.microsoft.com/en-us/windows/wsl/install"))
        } catch (e: Exception) {
            addAppLog(LogLevel.ERROR, "Не удалось открыть браузер: ${e.message}")
        }
    }
    
    fun openVirtualizationGuide() {
        try {
            addAppLog(LogLevel.INFO, "Открытие руководства по виртуализации...")
            Desktop.getDesktop().browse(URI("https://docs.docker.com/desktop/troubleshoot/topics/#virtualization"))
        } catch (e: Exception) {
            addAppLog(LogLevel.ERROR, "Не удалось открыть браузер: ${e.message}")
        }
    }
    
    fun openDockerInstallPage() {
        try {
            addAppLog(LogLevel.INFO, "Открытие страницы установки Docker...")
            Desktop.getDesktop().browse(URI("https://www.docker.com/products/docker-desktop/"))
        } catch (e: Exception) {
            addAppLog(LogLevel.ERROR, "Не удалось открыть браузер: ${e.message}")
        }
    }
    
    fun showNotification(title: String, message: String, type: TrayIcon.MessageType = TrayIcon.MessageType.INFO) {
        try {
            if (SystemTray.isSupported()) {
                val systemTray = SystemTray.getSystemTray()
                val image = Toolkit.getDefaultToolkit().createImage("icon.png")
                val trayIcon = TrayIcon(image, "macOS Docker UI")
                trayIcon.isImageAutoSize = true
                trayIcon.toolTip = "macOS Docker UI"
                systemTray.add(trayIcon)
                trayIcon.displayMessage(title, message, type)
            }
        } catch (e: Exception) {
            addAppLog(LogLevel.WARNING, "Не удалось показать уведомление: ${e.message}")
        }
    }
    
    fun clearAppLogs() {
        _appLogs.value = emptyList()
        addAppLog(LogLevel.INFO, "Логи очищены")
    }
    
    suspend fun startMacOSContainer(
        version: String,
        ramSize: String,
        storagePath: String,
        onLogUpdate: (String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            addAppLog(LogLevel.INFO, "Создание docker-compose.yml...")
            
            val dockerComposeContent = createDockerComposeContent(version, ramSize, storagePath)
            val dockerComposeFile = File("docker-compose.yml")
            dockerComposeFile.writeText(dockerComposeContent)
            
            addAppLog(LogLevel.SUCCESS, "docker-compose.yml создан")
            addAppLog(LogLevel.INFO, "Запуск macOS контейнера...")
            
            val processBuilder = ProcessBuilder(
                "docker-compose", "up", "-d"
            ).directory(File("."))
            
            dockerProcess = processBuilder.start()
            
            val errorOutput = dockerProcess?.errorStream?.bufferedReader()?.readText() ?: ""
            
            val exitCode = dockerProcess?.waitFor() ?: -1
            
            if (exitCode == 0) {
                addAppLog(LogLevel.SUCCESS, "Контейнер успешно запущен")
                onLogUpdate("Контейнер запущен успешно")
                
                if (xyz.demorgan.macdockui.config.SettingsManager.rememberContainer) {
                    val containerId = getContainerId()
                    if (containerId.isNotEmpty()) {
                        xyz.demorgan.macdockui.config.SettingsManager.lastContainerId = containerId
                        addAppLog(LogLevel.INFO, "ID контейнера сохранен: $containerId")
                    }
                }
                
                delay(3000)
                
                showNotification(
                    "macOS Docker UI", 
                    "Контейнер запущен! Веб-интерфейс доступен по адресу http://127.0.0.1:8006/",
                    TrayIcon.MessageType.INFO
                )
                
                true
            } else {
                addAppLog(LogLevel.ERROR, "Ошибка запуска контейнера. Код выхода: $exitCode")
                onLogUpdate("Ошибка запуска: $errorOutput")
                false
            }
            
        } catch (e: Exception) {
            logger.error(e) { "Error starting macOS container" }
            addAppLog(LogLevel.ERROR, "Ошибка запуска контейнера: ${e.message}")
            onLogUpdate("Ошибка запуска контейнера: ${e.message}")
            false
        }
    }
    
    suspend fun stopMacOSContainer(): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder("docker-compose", "down")
                .directory(File("."))
                .start()
            
            val exitCode = process.waitFor()
            dockerProcess?.destroy()
            dockerProcess = null
            _logs.value = emptyList()
            
            exitCode == 0
        } catch (e: Exception) {
            logger.error(e) { "Error stopping macOS container" }
            false
        }
    }
    
    suspend fun getContainerLogs(): List<String> = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder(
                "docker-compose", "logs", "--tail=50", "macos"
            ).directory(File(".")).start()
            
            process.inputStream.bufferedReader().readLines()
        } catch (e: Exception) {
            logger.error(e) { "Error getting container logs" }
            listOf("Ошибка получения логов: ${e.message}")
        }
    }
    
    fun isContainerRunning(): Boolean {
        return try {
            val process = ProcessBuilder(
                "docker-compose", "ps", "-q", "macos"
            ).directory(File(".")).start()
            
            val output = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            output.isNotEmpty()
        } catch (e: Exception) {
            logger.error(e) { "Error checking container status" }
            false
        }
    }
    
    private fun getContainerId(): String {
        return try {
            val process = ProcessBuilder(
                "docker-compose", "ps", "-q", "macos"
            ).directory(File(".")).start()
            
            val output = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            output
        } catch (e: Exception) {
            logger.error(e) { "Error getting container ID" }
            ""
        }
    }
    
    fun checkExistingContainer(): Boolean {
        val lastContainerId = xyz.demorgan.macdockui.config.SettingsManager.lastContainerId
        if (lastContainerId.isEmpty() || !xyz.demorgan.macdockui.config.SettingsManager.rememberContainer) {
            return false
        }
        
        return try {
            val process = ProcessBuilder("docker", "ps", "-a", "-q", "--filter", "id=$lastContainerId")
                .redirectErrorStream(true)
                .start()
            
            val output = process.inputStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()
            
            if (exitCode == 0 && output.isNotEmpty()) {
                addAppLog(LogLevel.INFO, "Найден существующий контейнер: $lastContainerId")
                true
            } else {
                addAppLog(LogLevel.INFO, "Существующий контейнер не найден")
                xyz.demorgan.macdockui.config.SettingsManager.lastContainerId = ""
                false
            }
        } catch (e: Exception) {
            logger.error(e) { "Error checking existing container" }
            false
        }
    }
    
    suspend fun startExistingContainer(): Boolean = withContext(Dispatchers.IO) {
        val lastContainerId = xyz.demorgan.macdockui.config.SettingsManager.lastContainerId
        if (lastContainerId.isEmpty()) return@withContext false
        
        try {
            addAppLog(LogLevel.INFO, "Запуск существующего контейнера...")
            
            val process = ProcessBuilder("docker", "start", lastContainerId)
                .redirectErrorStream(true)
                .start()
            
            val exitCode = process.waitFor()
            
            if (exitCode == 0) {
                addAppLog(LogLevel.SUCCESS, "Существующий контейнер запущен")
                
                showNotification(
                    "macOS Docker UI", 
                    "Существующий контейнер запущен! Веб-интерфейс доступен по адресу http://127.0.0.1:8006/",
                    TrayIcon.MessageType.INFO
                )
                
                true
            } else {
                addAppLog(LogLevel.ERROR, "Не удалось запустить существующий контейнер")
                xyz.demorgan.macdockui.config.SettingsManager.lastContainerId = ""
                false
            }
        } catch (e: Exception) {
            logger.error(e) { "Error starting existing container" }
            addAppLog(LogLevel.ERROR, "Ошибка запуска существующего контейнера: ${e.message}")
            false
        }
    }
    private fun createDockerComposeContent(version: String, ramSize: String, storagePath: String): String {
        return """
            services:
              macos:
                image: dockurr/macos
                container_name: macos
                environment:
                  VERSION: "$version"
                  RAM_SIZE: "$ramSize"
                devices:
                  - /dev/kvm
                  - /dev/net/tun
                cap_add:
                  - NET_ADMIN
                ports:
                  - 8006:8006
                  - 5900:5900/tcp
                  - 5900:5900/udp
                volumes:
                  - $storagePath:/storage
                restart: always
                stop_grace_period: 2m
               """.trimIndent()
    }
}