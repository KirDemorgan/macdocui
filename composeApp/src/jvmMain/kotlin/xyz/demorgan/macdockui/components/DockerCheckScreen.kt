package xyz.demorgan.macdockui.components

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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.demorgan.macdockui.docker.DockerManager
import xyz.demorgan.macdockui.docker.DockerStatus

@Composable
fun DockerCheckScreen(
    dockerStatus: DockerStatus,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (dockerStatus) {
            DockerStatus.CHECKING -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 6.dp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Проверка установки Docker...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            DockerStatus.INSTALLED -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Docker установлен",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Docker установлен и готов к работе",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4CAF50)
                )
            }
            
            DockerStatus.NOT_INSTALLED -> {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Docker не установлен",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFF44336)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Docker не установлен",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFF44336)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Пожалуйста, установите Docker Desktop и повторите попытку",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { DockerManager.openDockerInstallPage() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Скачать Docker")
                    }
                    
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Повторить")
                    }
                }
            }
            
            DockerStatus.DOCKER_DESKTOP_NOT_RUNNING -> {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Docker Desktop не запущен",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFFF9800)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Docker Desktop не запущен",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFF9800)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Docker установлен, но Docker Desktop не запущен. Пожалуйста, запустите Docker Desktop и повторите попытку.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Повторить проверку")
                }
            }
            
            DockerStatus.WSL_REQUIRED -> {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "WSL требуется",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFFF9800)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Требуется настройка WSL",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFF9800)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Для работы macOS в Docker на Windows требуется WSL (Windows Subsystem for Linux) с поддержкой виртуализации.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { DockerManager.openWSLInstallPage() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Установить WSL")
                        }
                        
                        Button(
                            onClick = { DockerManager.openVirtualizationGuide() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9C27B0)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Help,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Руководство")
                        }
                    }
                    
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Повторить проверку")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
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
                            text = "📋 Пошаговая инструкция:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Откройте PowerShell от имени администратора\n" +
                                  "2. Выполните: wsl --install\n" +
                                  "3. Перезагрузите компьютер\n" +
                                  "4. Включите виртуализацию в BIOS (Intel VT-x или AMD-V)\n" +
                                  "5. В Docker Desktop включите WSL 2 integration\n" +
                                  "6. Нажмите 'Повторить проверку'",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            DockerStatus.ERROR -> {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Ошибка",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFF44336)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Ошибка при проверке Docker",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFF44336)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Повторить")
                }
            }
        }
    }
}