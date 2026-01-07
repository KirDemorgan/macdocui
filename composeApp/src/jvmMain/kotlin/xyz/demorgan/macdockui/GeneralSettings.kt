package xyz.demorgan.macdockui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.AwtWindow
import androidx.compose.ui.unit.dp
import xyz.demorgan.macdockui.config.SettingsManager
import javax.swing.JFileChooser

@Composable
fun GeneralSettings() {
    var showFolderPicker by remember { mutableStateOf(false) }

    val currentPath by remember(SettingsManager.savePath) { mutableStateOf(SettingsManager.savePath) }

    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = {
            showFolderPicker = true
        }) {
            Text("Выбрать папку...")
        }
        Text(
            text = currentPath.ifEmpty { "Папка не выбрана" },
            modifier = Modifier.padding(start = 16.dp)
        )
    }

    if (showFolderPicker) {
        FolderPickerDialog(
            title = "Выберите папку для сохранения",
            onResult = { selectedPath ->
                showFolderPicker = false
                if (selectedPath != null) {
                    SettingsManager.savePath = selectedPath
                }
            }
        )
    }
}

/**
 * Composable-функция для отображения нативного диалога выбора папки.
 * @param title Заголовок окна диалога.
 * @param onResult Лямбда, которая вызывается с результатом выбора (путь к папке или null).
 */
@Composable
private fun FolderPickerDialog(
    title: String,
    onResult: (path: String?) -> Unit,) {
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

