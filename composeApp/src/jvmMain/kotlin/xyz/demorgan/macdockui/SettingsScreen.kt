package xyz.demorgan.macdockui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.*

@Composable
fun SettingsScreen() {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("General", "Appearance")

    Column {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> GeneralSettings()
            1 -> AppearanceSettings()
        }
    }
}
