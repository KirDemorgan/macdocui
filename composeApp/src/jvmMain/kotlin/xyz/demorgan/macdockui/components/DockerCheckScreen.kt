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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.demorgan.macdockui.docker.DockerManager
import xyz.demorgan.macdockui.docker.DockerStatus
import xyz.demorgan.macdockui.ui.theme.Success
import xyz.demorgan.macdockui.ui.theme.Warning
import xyz.demorgan.macdockui.viewmodel.AppViewModel

@Composable
fun DockerCheckScreen(
    viewModel: AppViewModel
) {
    val dockerStatus by viewModel.dockerStatus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (dockerStatus) {
            DockerStatus.CHECKING -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 6.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Checking Docker Installation...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            DockerStatus.INSTALLED -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Success
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Docker is ready",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Success
                )
            }
            
            DockerStatus.NOT_INSTALLED -> {
                StatusErrorView(
                    icon = Icons.Default.Error,
                    title = "Docker not installed",
                    message = "Please install Docker Desktop to continue."
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(onClick = { DockerManager.openDockerInstallPage() }) {
                            Icon(Icons.Default.Download, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Download Docker")
                        }
                        Button(
                            onClick = viewModel::checkDocker,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
            }
            
            DockerStatus.DOCKER_DESKTOP_NOT_RUNNING -> {
                 StatusErrorView(
                    icon = Icons.Default.Warning,
                    title = "Docker Desktop not running",
                    message = "Docker is installed but not running. Please start Docker Desktop.",
                    tint = Warning
                ) {
                    Button(onClick = viewModel::checkDocker) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Check Again")
                    }
                }
            }
            
            DockerStatus.WSL_REQUIRED -> {
                StatusErrorView(
                    icon = Icons.Default.Warning,
                    title = "WSL Configuration Required",
                    message = "WSL 2 is required to run Docker on Windows.",
                    tint = Warning
                ) {
                     Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { DockerManager.openWSLInstallPage() }) {
                                Icon(Icons.Default.Download, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Install WSL")
                            }
                            Button(
                                onClick = { DockerManager.openVirtualizationGuide() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(Icons.Default.Help, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Guide")
                            }
                        }
                        Button(onClick = viewModel::checkDocker) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
            }
            
            DockerStatus.ERROR -> {
                StatusErrorView(
                    icon = Icons.Default.Error,
                    title = "Unexpected Error",
                    message = "An error occurred while checking Docker status."
                ) {
                    Button(onClick = viewModel::checkDocker) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusErrorView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.error,
    action: @Composable () -> Unit
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = tint
    )
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = tint
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = message,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
        modifier = Modifier.padding(bottom = 24.dp)
    )
    action()
}