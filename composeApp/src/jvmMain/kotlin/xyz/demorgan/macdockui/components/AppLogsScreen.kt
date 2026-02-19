package xyz.demorgan.macdockui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.demorgan.macdockui.docker.AppLog
import xyz.demorgan.macdockui.docker.DockerManager
import xyz.demorgan.macdockui.docker.LogLevel
import java.time.format.DateTimeFormatter

@Composable
fun AppLogsScreen(
    onClose: () -> Unit
) {
    val logs by DockerManager.appLogs.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Application Logs",
                    style = MaterialTheme.typography.titleLarge
                )
                Row {
                    IconButton(onClick = { DockerManager.clearAppLogs() }) {
                        Icon(Icons.Default.Delete, "Clear Logs")
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
            }
            
            Divider()


            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { log ->
                    LogItem(log)
                }
            }
        }
    }
}

@Composable
fun LogItem(log: AppLog) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val color = when (log.level) {
        LogLevel.INFO -> Color.Blue
        LogLevel.WARNING -> Color(0xFFFF9800)
        LogLevel.ERROR -> Color.Red
        LogLevel.SUCCESS -> Color(0xFF4CAF50)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.small)
            .padding(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = log.timestamp.format(formatter),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = "[${log.level}]",
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = log.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace
        )
    }
}