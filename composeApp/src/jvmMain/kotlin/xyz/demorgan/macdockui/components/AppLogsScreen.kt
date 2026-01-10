package xyz.demorgan.macdockui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(logs.size - 1)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Логи приложения",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { DockerManager.clearAppLogs() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Очистить")
                }
                
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть"
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E1E)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color(0xFF0D1117),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Логи пусты",
                            color = Color(0xFF7D8590),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs) { log ->
                            LogItem(log = log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogItem(log: AppLog) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val color = when (log.level) {
        LogLevel.INFO -> Color(0xFF58A6FF)
        LogLevel.SUCCESS -> Color(0xFF3FB950)
        LogLevel.WARNING -> Color(0xFFD29922)
        LogLevel.ERROR -> Color(0xFFF85149)
    }
    
    val icon = when (log.level) {
        LogLevel.INFO -> "ℹ️"
        LogLevel.SUCCESS -> "✅"
        LogLevel.WARNING -> "⚠️"
        LogLevel.ERROR -> "❌"
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = log.timestamp.format(timeFormatter),
            color = Color(0xFF7D8590),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.width(60.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = icon,
            fontSize = 12.sp,
            modifier = Modifier.width(20.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = log.message,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
    }
}