package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.RobotSettings
import com.example.data.local.SettingsManager
import com.example.data.model.AutoSafetyReport
import com.example.data.model.AutoState
import com.example.data.model.CalibrationProfile
import com.example.data.model.OperationLog
import com.example.data.model.RgbcData
import com.example.data.model.RobotStatus
import com.example.data.model.WeedDetectionResult
import com.example.data.remote.CameraStreamState
import com.example.data.repository.RobotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val database = AppDatabase.getInstance(application)
    val settingsManager = SettingsManager(application)
    val repository = RobotRepository(database, settingsManager)

    val robotStatus: StateFlow<RobotStatus?> = repository.robotStatus
    val isEsp32Online: StateFlow<Boolean> = repository.isEsp32Online
    val connectionError: StateFlow<String?> = repository.connectionError
    val lastSuccessfulContact: StateFlow<Long> = repository.lastSuccessfulContact
    val autoState: StateFlow<AutoState> = repository.autoState
    val soilCountdownSeconds: StateFlow<Int> = repository.soilCountdownSeconds
    val currentWeedDetection: StateFlow<WeedDetectionResult?> = repository.currentWeedDetection
    val suspectedDisease: StateFlow<String?> = repository.suspectedDisease

    val streamState: StateFlow<CameraStreamState> = repository.streamReader.streamState

    val settings: StateFlow<RobotSettings> = settingsManager.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = RobotSettings()
    )

    val logs: StateFlow<List<OperationLog>> = repository.logsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    val calibrationProfiles: StateFlow<List<CalibrationProfile>> = repository.calibrationProfilesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = CalibrationProfile.DEFAULT_PROFILES
    )

    // Current navigation tab
    private val _currentScreen = MutableStateFlow("SPLASH")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Safety check state for Auto Start dialog
    private val _safetyCheckReport = MutableStateFlow<AutoSafetyReport?>(null)
    val safetyCheckReport: StateFlow<AutoSafetyReport?> = _safetyCheckReport.asStateFlow()

    private val _showAutoStartDialog = MutableStateFlow(false)
    val showAutoStartDialog: StateFlow<Boolean> = _showAutoStartDialog.asStateFlow()

    private val _showAutoStopDialog = MutableStateFlow(false)
    val showAutoStopDialog: StateFlow<Boolean> = _showAutoStopDialog.asStateFlow()

    // Calibration target selection
    private val _selectedCalibrationProfileId = MutableStateFlow("NORMAL")
    val selectedCalibrationProfileId: StateFlow<String> = _selectedCalibrationProfileId.asStateFlow()

    init {
        repository.initialize(viewModelScope)
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
        if (screen == "MANUAL" || screen == "AUTO") {
            startCameraStream()
        }
    }

    fun startCameraStream() {
        val currentSettings = settings.value
        repository.streamReader.startStream(
            scope = viewModelScope,
            streamUrl = currentSettings.streamUrl,
            captureFallbackUrl = currentSettings.captureUrl
        )
    }

    fun stopCameraStream() {
        repository.streamReader.stopStream()
    }

    fun retryCameraStream() {
        val currentSettings = settings.value
        repository.streamReader.startStream(
            scope = viewModelScope,
            streamUrl = currentSettings.streamUrl,
            captureFallbackUrl = currentSettings.captureUrl
        )
    }

    // Manual Driving
    fun sendMove(direction: String) {
        viewModelScope.launch {
            repository.sendMoveCommand(direction)
        }
    }

    fun sendSpeed(speed: String) {
        viewModelScope.launch {
            repository.sendSpeedCommand(speed)
        }
    }

    fun sendCameraPan(position: String) {
        viewModelScope.launch {
            repository.sendServoCamera(1, position)
        }
    }

    fun sendCameraTilt(position: String) {
        viewModelScope.launch {
            repository.sendServoCamera(2, position)
        }
    }

    fun toggleBuzzer() {
        val current = robotStatus.value?.buzzer == true
        viewModelScope.launch {
            repository.sendBuzzer(!current)
        }
    }

    fun triggerEmergencyStop() {
        viewModelScope.launch {
            repository.triggerEmergencyStop()
        }
    }

    // Auto Mode Requests
    fun requestStartAuto() {
        viewModelScope.launch {
            val report = repository.performAutoSafetyCheck()
            _safetyCheckReport.value = report
            _showAutoStartDialog.value = true
        }
    }

    fun confirmStartAuto() {
        _showAutoStartDialog.value = false
        viewModelScope.launch {
            val started = repository.startAutoOperation(viewModelScope)
            if (started) {
                _currentScreen.value = "AUTO"
            }
        }
    }

    fun dismissAutoStartDialog() {
        _showAutoStartDialog.value = false
    }

    fun requestStopAuto() {
        _showAutoStopDialog.value = true
    }

    fun confirmStopAuto() {
        _showAutoStopDialog.value = false
        viewModelScope.launch {
            repository.stopAutoOperation()
            _currentScreen.value = "MANUAL"
        }
    }

    fun dismissAutoStopDialog() {
        _showAutoStopDialog.value = false
    }

    // Calibration
    fun selectCalibrationProfile(profileId: String) {
        _selectedCalibrationProfileId.value = profileId
    }

    fun saveCalibrationForCurrentSensor() {
        val status = robotStatus.value ?: return
        val rgbc = status.tcs34725 ?: return
        val profileId = _selectedCalibrationProfileId.value
        viewModelScope.launch {
            repository.saveCalibrationSample(profileId, rgbc)
        }
    }

    fun resetCalibration(profileId: String) {
        viewModelScope.launch {
            repository.resetCalibrationProfile(profileId)
        }
    }

    // Settings
    fun updateNetworkSettings(ssid: String, pass: String, esp32Ip: String, camIp: String, port: Int) {
        viewModelScope.launch {
            settingsManager.updateNetworkSettings(ssid, pass, esp32Ip, camIp, port)
            // Reconnect stream if updated
            retryCameraStream()
        }
    }

    fun updateAiSettings(confidence: Int, consecutive: Int) {
        viewModelScope.launch {
            settingsManager.updateAiSettings(confidence, consecutive)
        }
    }

    fun updateAgronomySettings(soilDry: Int, water: Int, spray: Int, battery: Double) {
        viewModelScope.launch {
            settingsManager.updateAgronomySettings(soilDry, water, spray, battery)
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }
}
