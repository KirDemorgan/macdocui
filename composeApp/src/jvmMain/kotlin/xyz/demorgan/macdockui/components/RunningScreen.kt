package xyz.demorgan.macdockui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import xyz.demorgan.macdockui.docker.DockerManager
import xyz.demorgan.macdockui.ui.theme.Success

@Composable
fun RunningScreen(
    onStop: () -> Unit
) {
    var showLogs by remember { mutableStateOf(false) }
    var logs by remember { mutableStateOf<List<String>>(emptyList()) }
    var isStopping by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    

    LaunchedEffect(showLogs) {
        if (showLogs) {
            while (isActive) {
                logs = DockerManager.getContainerLogs()
                delay(2000)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        

        Card(
             modifier = Modifier.fillMaxWidth(),
             colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = Success,
                    strokeWidth = 4.dp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "macOS is Running",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Success
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Container is active and health checks are passing.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(Modifier.height(32.dp))
        

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { isStopping = true; scope.launch { if(DockerManager.stopMacOSContainer()) onStop() } },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                enabled = !isStopping,
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                if (isStopping) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Stopping...")
                } else {
                    Icon(Icons.Default.Close, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Stop Instance")
                }
            }
            
            OutlinedButton(
                onClick = { showLogs = !showLogs },
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                 Icon(if (showLogs) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                 Spacer(Modifier.width(8.dp))
                 Text(if (showLogs) "Hide Logs" else "Show Logs")
            }
        }
        
        if (showLogs) {
            Spacer(Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         Icon(Icons.Default.Terminal, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                         Spacer(Modifier.width(8.dp))
                         Text("Container Logs", color = Color.Gray, fontSize = 12.sp)
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)
                    SelectionContainer {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (logs.isEmpty()) {
                                Text("Loading logs...", color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                            } else {
                                logs.forEach { log ->
                                    Text(
                                        text = log,
                                        color = Color.Green,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}