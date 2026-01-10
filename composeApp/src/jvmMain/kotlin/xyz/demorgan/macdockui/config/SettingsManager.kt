package xyz.demorgan.macdockui.config

import java.util.prefs.Preferences

object SettingsManager {
    private val prefs = Preferences.userNodeForPackage(SettingsManager::class.java)

    var storagePath: String
        get() = prefs.get("storage_path", "")
        set(value) = prefs.put("storage_path", value)

    var macOSVersion: String
        get() = prefs.get("macos_version", "14")
        set(value) = prefs.put("macos_version", value)

    var ramSize: String
        get() = prefs.get("ram_size", "8G")
        set(value) = prefs.put("ram_size", value)

    var rememberContainer: Boolean
        get() = prefs.getBoolean("remember_container", true)
        set(value) = prefs.putBoolean("remember_container", value)
        
    var lastContainerId: String
        get() = prefs.get("last_container_id", "")
        set(value) = prefs.put("last_container_id", value)
}