package xyz.demorgan.macdockui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.AwtWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.demorgan.macdockui.config.SettingsManager
import xyz.demorgan.macdockui.docker.DockerManager
import java.awt.Desktop
import java.awt.TrayIcon
import java.net.URI
import javax.swing.JFileChooser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onStartDocker: () -> Unit
) {
    var showFolderPicker by remember { mutableStateOf(false) }
    var macOSVersion by remember { mutableStateOf(SettingsManager.macOSVersion) }
    var ramSize by remember { mutableStateOf(SettingsManager.ramSize) }
    var storagePath by remember { mutableStateOf(SettingsManager.storagePath) }
    var rememberContainer by remember { mutableStateOf(SettingsManager.rememberContainer) }
    var isStarting by remember { mutableStateOf(false) }
    var startingProgress by remember { mutableStateOf("") }
    var hasExistingContainer by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        hasExistingContainer = DockerManager.checkExistingContainer()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Настройки macOS контейнера",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        if (hasExistingContainer && rememberContainer) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🔄 Существующий контейнер",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Найден ранее созданный контейнер. Вы можете запустить его или создать новый.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isStarting = true
                                startingProgress = "Запуск существующего контейнера..."
                                
                                scope.launch {
                                    val success = DockerManager.startExistingContainer()
                                    if (success) {
                                        onStartDocker()
                                        try {
                                            Desktop.getDesktop().browse(URI("http://127.0.0.1:8006/"))
                                        } catch (e: Exception) {
                                            DockerManager.showNotification(
                                                "Ошибка",
                                                "Не удалось открыть браузер. Откройте http://127.0.0.1:8006/ вручную",
                                                TrayIcon.MessageType.WARNING
                                            )
                                        }
                                    }
                                    isStarting = false
                                    startingProgress = ""
                                }
                            },
                            enabled = !isStarting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Запустить существующий")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                hasExistingContainer = false
                                SettingsManager.lastContainerId = ""
                            },
                            enabled = !isStarting
                        ) {
                            Text("Создать новый")
                        }
                    }
                }
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Версия macOS",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val versions = listOf("15", "14", "13", "12", "11")
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = "macOS $macOSVersion",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        versions.forEach { version ->
                            DropdownMenuItem(
                                text = { Text("macOS $version") },
                                onClick = {
                                    macOSVersion = version
                                    SettingsManager.macOSVersion = version
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Размер RAM",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val ramSizes = listOf("4G", "6G", "8G", "12G", "16G", "24G", "32G")
                var ramExpanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = ramExpanded,
                    onExpandedChange = { ramExpanded = !ramExpanded }
                ) {
                    OutlinedTextField(
                        value = ramSize,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ramExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = ramExpanded,
                        onDismissRequest = { ramExpanded = false }
                    ) {
                        ramSizes.forEach { size ->
                            DropdownMenuItem(
                                text = { Text(size) },
                                onClick = {
                                    ramSize = size
                                    SettingsManager.ramSize = size
                                    ramExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Путь для хранения системы",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = storagePath.ifEmpty { "Выберите папку..." },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showFolderPicker = true },
                        enabled = !isStarting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Выбрать папку"
                        )
                    }
                }
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Запоминать контейнер",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Позволяет быстро запускать ранее созданный контейнер",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Switch(
                    checked = rememberContainer,
                    onCheckedChange = { 
                        rememberContainer = it
                        SettingsManager.rememberContainer = it
                        if (!it) {
                            SettingsManager.lastContainerId = ""
                            hasExistingContainer = false
                        }
                    }
                )
            }
        }
        
        if (isStarting) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = startingProgress.ifEmpty { "Запуск контейнера..." },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        if (!hasExistingContainer || !rememberContainer) {
            Button(
                onClick = {
                    if (storagePath.isNotEmpty()) {
                        isStarting = true
                        startingProgress = "Создание docker-compose.yml..."
                        
                        scope.launch {
                            val success = DockerManager.startMacOSContainer(
                                version = macOSVersion,
                                ramSize = ramSize,
                                storagePath = storagePath
                            ) { progress ->
                                startingProgress = progress
                            }
                            
                            if (success) {
                                startingProgress = "Открытие веб-интерфейса..."
                                delay(1000)
                                
                                onStartDocker()
                                try {
                                    Desktop.getDesktop().browse(URI("http://127.0.0.1:8006/"))
                                } catch (e: Exception) {
                                    DockerManager.showNotification(
                                        "Ошибка",
                                        "Не удалось открыть браузер. Откройте http://127.0.0.1:8006/ вручную",
                                        TrayIcon.MessageType.WARNING
                                    )
                                }
                            } else {
                                DockerManager.showNotification(
                                    "Ошибка",
                                    "Не удалось запустить контейнер. Проверьте логи приложения.",
                                    TrayIcon.MessageType.ERROR
                                )
                            }
                            isStarting = false
                            startingProgress = ""
                        }
                    }
                },
                enabled = storagePath.isNotEmpty() && !isStarting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isStarting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Запуск...")
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Запустить macOS")
                }
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "ℹ️ Информация",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• После запуска откроется веб-интерфейс по адресу http://127.0.0.1:8006/\n" +
                          "• VNC доступен на порту 5900\n" +
                          "• Первый запуск может занять длительное время (до 30 минут)\n" +
                          "• Рекомендуемый размер RAM: 8G или больше\n" +
                          "• Убедитесь, что выбранная папка имеет достаточно свободного места (минимум 64 ГБ)\n" +
                          "• Для работы требуется включенная виртуализация в BIOS",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
    
    if (showFolderPicker) {
        FolderPickerDialog(
            title = "Выберите папку для хранения macOS",
            onResult = { selectedPath ->
                showFolderPicker = false
                if (selectedPath != null) {
                    storagePath = selectedPath
                    SettingsManager.storagePath = selectedPath
                }
            }
        )
    }
}

@Composable
private fun FolderPickerDialog(
    title: String,
    onResult: (path: String?) -> Unit,
) {
    val fileChooser = remember {
        JFileChooser().apply {
            dialogTitle = title
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            isAcceptAllFileFilterUsed = false
        }
    }

    AwtWindow(
        create = {
            object : java.awt.Frame() {
                init {
                    isVisible = false
                }
            }
        },
        dispose = {
            it.dispose()
        },
        update = { owner ->
            if (!fileChooser.isShowing) {
                val result = fileChooser.showOpenDialog(owner)

                if (result == JFileChooser.APPROVE_OPTION) {
                    onResult(fileChooser.selectedFile.absolutePath)
                } else {
                    onResult(null)
                }
            }
        }
    )
}
