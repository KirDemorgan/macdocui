package xyz.demorgan.macdockui.config

import java.util.prefs.Preferences

object SettingsManager {
    private val prefs = Preferences.userNodeForPackage(SettingsManager::class.java)

    var savePath: String
        get() = prefs.get("save_path", "")
        set(value) = prefs.put("save_path", value)

    var textSetting: String
        get() = prefs.get("text_setting", "Default Value")
        set(value) = prefs.put("text_setting", value)

    var booleanSetting: Boolean
        get() = prefs.getBoolean("boolean_setting", false)
        set(value) = prefs.putBoolean("boolean_setting", value)
}