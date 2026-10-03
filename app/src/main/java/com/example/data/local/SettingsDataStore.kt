package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "onion_robot_settings")

data class RobotSettings(
    val wifiSsid: String = "OnionRobot_AP",
    val wifiPassword: String = "",
    val esp32Ip: String = "192.168.1.50",
    val esp32CamIp: String = "192.168.1.51",
    val streamPort: Int = 81,
    val streamPath: String = "/stream",
    val capturePath: String = "/capture",
    val httpTimeoutMs: Long = 3000L,
    val weedConfidenceThreshold: Int = 85,
    val consecutiveDetectionCount: Int = 3,
    val colourConfirmationCount: Int = 3,
    val soilDryThreshold: Int = 40,
    val soilWetThreshold: Int = 70,
    val soilCheckIntervalSeconds: Int = 300, // 5 min
    val soilMonitoringDurationSeconds: Int = 30,
    val waterLevelThreshold: Int = 20,
    val wateringDurationSeconds: Int = 30,
    val sprayLiquidThreshold: Int = 20,
    val sprayDurationSeconds: Int = 30,
    val cutterMaxSeconds: Int = 7, // Enforced <= 7 seconds
    val criticalBatteryVoltage: Double = 11.1,
    val robotName: String = "OnionBot-01",
    val defaultSpeed: String = "SLOW"
) {
    val streamUrl: String
        get() = "http://$esp32CamIp:$streamPort$streamPath"

    val captureUrl: String
        get() = "http://$esp32CamIp$capturePath"

    val esp32BaseUrl: String
        get() = "http://$esp32Ip/"
}

class SettingsManager(private val context: Context) {

    private object PreferencesKeys {
        val WIFI_SSID = stringPreferencesKey("wifi_ssid")
        val WIFI_PASSWORD = stringPreferencesKey("wifi_password")
        val ESP32_IP = stringPreferencesKey("esp32_ip")
        val ESP32_CAM_IP = stringPreferencesKey("esp32_cam_ip")
        val STREAM_PORT = intPreferencesKey("stream_port")
        val STREAM_PATH = stringPreferencesKey("stream_path")
        val CAPTURE_PATH = stringPreferencesKey("capture_path")
        val HTTP_TIMEOUT = longPreferencesKey("http_timeout")
        val WEED_CONFIDENCE = intPreferencesKey("weed_confidence")
        val CONSECUTIVE_DETECTION = intPreferencesKey("consecutive_detection")
        val COLOUR_CONFIRMATION = intPreferencesKey("colour_confirmation")
        val SOIL_DRY = intPreferencesKey("soil_dry")
        val SOIL_WET = intPreferencesKey("soil_wet")
        val SOIL_INTERVAL = intPreferencesKey("soil_interval")
        val WATER_THRESHOLD = intPreferencesKey("water_threshold")
        val SPRAY_THRESHOLD = intPreferencesKey("spray_threshold")
        val CRITICAL_BATTERY = doublePreferencesKey("critical_battery")
        val ROBOT_NAME = stringPreferencesKey("robot_name")
        val DEFAULT_SPEED = stringPreferencesKey("default_speed")
    }

    val settingsFlow: Flow<RobotSettings> = context.dataStore.data.map { pref ->
        RobotSettings(
            wifiSsid = pref[PreferencesKeys.WIFI_SSID] ?: "OnionRobot_AP",
            wifiPassword = pref[PreferencesKeys.WIFI_PASSWORD] ?: "",
            esp32Ip = pref[PreferencesKeys.ESP32_IP] ?: "192.168.1.50",
            esp32CamIp = pref[PreferencesKeys.ESP32_CAM_IP] ?: "192.168.1.51",
            streamPort = pref[PreferencesKeys.STREAM_PORT] ?: 81,
            streamPath = pref[PreferencesKeys.STREAM_PATH] ?: "/stream",
            capturePath = pref[PreferencesKeys.CAPTURE_PATH] ?: "/capture",
            httpTimeoutMs = pref[PreferencesKeys.HTTP_TIMEOUT] ?: 3000L,
            weedConfidenceThreshold = pref[PreferencesKeys.WEED_CONFIDENCE] ?: 85,
            consecutiveDetectionCount = pref[PreferencesKeys.CONSECUTIVE_DETECTION] ?: 3,
            colourConfirmationCount = pref[PreferencesKeys.COLOUR_CONFIRMATION] ?: 3,
            soilDryThreshold = pref[PreferencesKeys.SOIL_DRY] ?: 40,
            soilWetThreshold = pref[PreferencesKeys.SOIL_WET] ?: 70,
            soilCheckIntervalSeconds = pref[PreferencesKeys.SOIL_INTERVAL] ?: 300,
            waterLevelThreshold = pref[PreferencesKeys.WATER_THRESHOLD] ?: 20,
            sprayLiquidThreshold = pref[PreferencesKeys.SPRAY_THRESHOLD] ?: 20,
            criticalBatteryVoltage = pref[PreferencesKeys.CRITICAL_BATTERY] ?: 11.1,
            robotName = pref[PreferencesKeys.ROBOT_NAME] ?: "OnionBot-01",
            defaultSpeed = pref[PreferencesKeys.DEFAULT_SPEED] ?: "SLOW"
        )
    }

    suspend fun updateNetworkSettings(
        ssid: String,
        pass: String,
        esp32Ip: String,
        camIp: String,
        port: Int
    ) {
        context.dataStore.edit { pref ->
            pref[PreferencesKeys.WIFI_SSID] = ssid
            pref[PreferencesKeys.WIFI_PASSWORD] = pass
            pref[PreferencesKeys.ESP32_IP] = esp32Ip
            pref[PreferencesKeys.ESP32_CAM_IP] = camIp
            pref[PreferencesKeys.STREAM_PORT] = port
        }
    }

    suspend fun updateAiSettings(confidence: Int, consecutive: Int) {
        context.dataStore.edit { pref ->
            pref[PreferencesKeys.WEED_CONFIDENCE] = confidence
            pref[PreferencesKeys.CONSECUTIVE_DETECTION] = consecutive
        }
    }

    suspend fun updateAgronomySettings(
        soilDry: Int,
        waterThreshold: Int,
        sprayThreshold: Int,
        criticalBattery: Double
    ) {
        context.dataStore.edit { pref ->
            pref[PreferencesKeys.SOIL_DRY] = soilDry
            pref[PreferencesKeys.WATER_THRESHOLD] = waterThreshold
            pref[PreferencesKeys.SPRAY_THRESHOLD] = sprayThreshold
            pref[PreferencesKeys.CRITICAL_BATTERY] = criticalBattery
        }
    }

    suspend fun updateRobotName(name: String) {
        context.dataStore.edit { pref ->
            pref[PreferencesKeys.ROBOT_NAME] = name
        }
    }
}
