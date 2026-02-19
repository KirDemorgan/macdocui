package xyz.demorgan.macdockui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.awt.AwtWindow
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.demorgan.macdockui.ui.theme.SurfaceVariant
import xyz.demorgan.macdockui.viewmodel.SettingsViewModel
import javax.swing.JFileChooser

@Composable
fun SettingsScreen(
    onStartDocker: () -> Unit,
    viewModel: SettingsViewModel = viewModel { SettingsViewModel() }
) {
    val macOSVersion by viewModel.macOSVersion.collectAsState()
    val ramSize by viewModel.ramSize.collectAsState()
    val storagePath by viewModel.storagePath.collectAsState()
    val rememberContainer by viewModel.rememberContainer.collectAsState()
    val hasExistingContainer by viewModel.hasExistingContainer.collectAsState()
    val isStarting by viewModel.isStarting.collectAsState()
    val startingProgress by viewModel.startingProgress.collectAsState()

    var showFolderPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Configuration",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        if (hasExistingContainer && rememberContainer) {
            ExistingContainerCard(
                onStart = { viewModel.startExistingContainer(onStartDocker) },
                onNew = viewModel::createNewContainer,
                isStarting = isStarting,
                progress = startingProgress
            )
        }

        SettingSection(title = "System") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SelectionDropdown(
                    label = "macOS Version",
                    options = listOf("15", "14", "13", "12", "11"),
                    selected = macOSVersion,
                    onSelected = viewModel::updateMacOSVersion,
                    modifier = Modifier.weight(1f)
                )
                SelectionDropdown(
                    label = "RAM Allocation",
                    options = listOf("4G", "6G", "8G", "12G", "16G", "24G", "32G"),
                    selected = ramSize,
                    onSelected = viewModel::updateRamSize,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        SettingSection(title = "Storage") {
            OutlinedTextField(
                value = storagePath,
                onValueChange = {},
                readOnly = true,
                label = { Text("Storage Path") },
                trailingIcon = {
                    IconButton(onClick = { showFolderPicker = true }) {
                        Icon(Icons.Default.Folder, contentDescription = "Select Folder")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Requires ~64GB free space",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, start = 8.dp)
            )
        }

        SettingSection(title = "Preferences") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Remember Container", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Quickly resume previous session",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = rememberContainer,
                    onCheckedChange = viewModel::updateRememberContainer
                )
            }
        }

        if (!hasExistingContainer || !rememberContainer) {
            Button(
                onClick = { viewModel.startContainer(onStartDocker) },
                enabled = storagePath.isNotEmpty() && !isStarting,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isStarting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(startingProgress.ifEmpty { "Initializing..." })
                } else {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Launch macOS")
                }
            }
        }
        

         Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Information", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("• Web Interface: http://localhost:8006", style = MaterialTheme.typography.bodySmall)
                Text("• VNC Port: 5900", style = MaterialTheme.typography.bodySmall)
                Text("• First launch may take up to 30 minutes to download and install.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    if (showFolderPicker) {
        val currentPath = if (storagePath.isNotEmpty()) storagePath else null
        FolderPickerDialog(
            title = "Select Storage Directory",
            initialPath = currentPath,
            onResult = { selectedPath ->
                showFolderPicker = false
                if (selectedPath != null) {
                    viewModel.updateStoragePath(selectedPath)
                }
            }
        )
    }
}

@Composable
fun ExistingContainerCard(
    onStart: () -> Unit,
    onNew: () -> Unit,
    isStarting: Boolean,
    progress: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Resume Session",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "A previous macOS container was found.",
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onStart,
                    enabled = !isStarting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        contentColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                     if (isStarting) {
                         CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                         Spacer(Modifier.width(8.dp))
                         Text("Starting...")
                     } else {
                         Text("Resume")
                     }
                }
                OutlinedButton(
                    onClick = onNew,
                    enabled = !isStarting,
                    colors = ButtonDefaults.outlinedButtonColors(
                         contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("New Instance")
                }
            }
            if (isStarting && progress.isNotEmpty()) {
                Text(
                    text = progress,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun SettingSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FolderPickerDialog(
    title: String,
    initialPath: String?,
    onResult: (path: String?) -> Unit,
) {
    val fileChooser = remember {
        JFileChooser(initialPath).apply {
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
