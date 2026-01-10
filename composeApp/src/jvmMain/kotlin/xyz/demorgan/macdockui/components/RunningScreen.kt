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
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.demorgan.macdockui.docker.DockerManager
import java.awt.Desktop
import java.net.URI

@Composable
fun RunningScreen(
    showLogs: Boolean,
    onStop: () -> Unit
) {
    var logs by remember { mutableStateOf<List<String>>(emptyList()) }
    var isRunning by remember { mutableStateOf(true) }
    var isStopping by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    LaunchedEffect(Unit) {
        while (isRunning) {
            logs = DockerManager.getContainerLogs()
            delay(2000)
        }
    }
    
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "macOS контейнер запущен",
                            fontSize = 18.sp,
                            color = Color(0xFF4CAF50)
                        )
                        Text(
                            text = "Веб-интерфейс: http://127.0.0.1:8006/",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    Desktop.getDesktop().browse(URI("http://127.0.0.1:8006/"))
                                } catch (e: Exception) {
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Launch,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Открыть")
                        }
                        
                        Button(
                            onClick = {
                                isStopping = true
                                scope.launch {
                                    val success = DockerManager.stopMacOSContainer()
                                    if (success) {
                                        isRunning = false
                                        onStop()
                                    }
                                    isStopping = false
                                }
                            },
                            enabled = !isStopping,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF44336)
                            )
                        ) {
                            if (isStopping) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isStopping) "Остановка..." else "Остановить")
                        }
                    }
                }
            }
        }
        
        if (showLogs) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Логи контейнера",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
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
                            Text(
                                text = "Загрузка логов...",
                                color = Color(0xFF7D8590),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        } else {
                            LazyColumn(
                                state = listState,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                items(logs) { log ->
                                    Text(
                                        text = log,
                                        color = Color(0xFFE6EDF3),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
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
                        text = "💡 Подсказка",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Нажмите на иконку глаза в верхней панели, чтобы показать/скрыть логи\n" +
                              "• Веб-интерфейс откроется автоматически в браузере\n" +
                              "• Для подключения через VNC используйте адрес 127.0.0.1:5900",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}