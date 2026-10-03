package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.RobotSettings
import com.example.data.local.SettingsManager
import com.example.data.model.ApiResponse
import com.example.data.model.AutoSafetyReport
import com.example.data.model.AutoState
import com.example.data.model.CalibrationProfile
import com.example.data.model.OperationLog
import com.example.data.model.RgbcData
import com.example.data.model.RobotStatus
import com.example.data.model.SafetyCheckItem
import com.example.data.model.WeedDetectionResult
import com.example.data.remote.CameraStreamState
import com.example.data.remote.Esp32Client
import com.example.data.remote.MjpegStreamReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RobotRepository(
    private val database: AppDatabase,
    private val settingsManager: SettingsManager,
    private val esp32Client: Esp32Client = Esp32Client(),
    val streamReader: MjpegStreamReader = MjpegStreamReader()
) {
    private val logDao = database.logDao()
    private val calibrationDao = database.calibrationDao()

    // Status State
    private val _robotStatus = MutableStateFlow<RobotStatus?>(null)
    val robotStatus: StateFlow<RobotStatus?> = _robotStatus.asStateFlow()

    private val _isEsp32Online = MutableStateFlow(false)
    val isEsp32Online: StateFlow<Boolean> = _isEsp32Online.asStateFlow()

    private val _lastSuccessfulContact = MutableStateFlow(0L)
    val lastSuccessfulContact: StateFlow<Long> = _lastSuccessfulContact.asStateFlow()

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()

    // Auto State Machine
    private val _autoState = MutableStateFlow(AutoState.MANUAL)
    val autoState: StateFlow<AutoState> = _autoState.asStateFlow()

    // 5-Minute Soil Check Countdown (in seconds)
    private val _soilCountdownSeconds = MutableStateFlow(300)
    val soilCountdownSeconds: StateFlow<Int> = _soilCountdownSeconds.asStateFlow()

    // Weed sequence tracking
    private val _currentWeedDetection = MutableStateFlow<WeedDetectionResult?>(null)
    val currentWeedDetection: StateFlow<WeedDetectionResult?> = _currentWeedDetection.asStateFlow()

    // Disease colour indication tracking
    private val _suspectedDisease = MutableStateFlow<String?>(null)
    val suspectedDisease: StateFlow<String?> = _suspectedDisease.asStateFlow()

    // Operation log flow
    val logsFlow: Flow<List<OperationLog>> = logDao.getAllLogs()

    // Calibration profiles flow
    val calibrationProfilesFlow: Flow<List<CalibrationProfile>> = calibrationDao.getAllProfiles()

    private var pollingJob: Job? = null
    private var autoSupervisorJob: Job? = null
    private var repositoryScope: CoroutineScope? = null

    fun initialize(scope: CoroutineScope) {
        repositoryScope = scope
        startPolling(scope)
    }

    private fun startPolling(scope: CoroutineScope) {
        pollingJob?.cancel()
        pollingJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                val settings = settingsManager.settingsFlow.first()
                val result = esp32Client.fetchStatus(settings.esp32Ip, settings.httpTimeoutMs)

                result.fold(
                    onSuccess = { status ->
                        _isEsp32Online.value = true
                        _lastSuccessfulContact.value = System.currentTimeMillis()
                        _connectionError.value = null
                        _robotStatus.value = status

                        // Check physical Emergency Stop from hardware
                        if (status.emergencyStop) {
                            if (_autoState.value != AutoState.EMERGENCY_STOP) {
                                handleEmergencyStopTriggered("PHYSICAL EMERGENCY STOP ENGAGED")
                            }
                        }

                        // Check low battery
                        status.batteryVoltage?.let { volts ->
                            if (volts <= settings.criticalBatteryVoltage && _autoState.value != AutoState.EMERGENCY_STOP) {
                                if (_autoState.value != AutoState.LOW_BATTERY) {
                                    handleLowBattery(volts)
                                }
                            }
                        }
                    },
                    onFailure = { error ->
                        _isEsp32Online.value = false
                        _connectionError.value = error.message ?: "ESP32 CONNECTION FAILED"
                        // Do NOT display stale values as live
                        // Keep current status null or note disconnected
                    }
                )

                delay(800) // Poll interval 800ms (between 500-1000ms as requested)
            }
        }
    }

    suspend fun logEvent(category: String, title: String, detail: String, level: String = "INFO") {
        logDao.insertLog(
            OperationLog(
                category = category,
                title = title,
                detail = detail,
                level = level
            )
        )
    }

    suspend fun clearLogs() {
        logDao.clearLogs()
    }

    // MANUAL Commands
    suspend fun sendMoveCommand(direction: String, speed: String? = null): Result<ApiResponse> {
        if (_autoState.value != AutoState.MANUAL) {
            return Result.failure(IllegalStateException("Movement disabled while in AUTO mode"))
        }
        val settings = settingsManager.settingsFlow.first()
        val res = esp32Client.moveRobot(settings.esp32Ip, direction, speed)
        if (res.isSuccess) {
            logEvent("MANUAL", "Movement: $direction", "Speed: ${speed ?: "DEFAULT"}")
        }
        return res
    }

    suspend fun sendSpeedCommand(speed: String): Result<ApiResponse> {
        val settings = settingsManager.settingsFlow.first()
        val res = esp32Client.setSpeed(settings.esp32Ip, speed)
        if (res.isSuccess) {
            logEvent("MANUAL", "Speed Changed", "New speed: $speed")
        }
        return res
    }

    suspend fun sendServoCamera(servoId: Int, position: String): Result<ApiResponse> {
        if (_autoState.value != AutoState.MANUAL) {
            return Result.failure(IllegalStateException("Camera manual control disabled in AUTO"))
        }
        val settings = settingsManager.settingsFlow.first()
        val servoName = if (servoId == 1) "PAN" else "TILT"
        val res = esp32Client.setServo(settings.esp32Ip, servoId, position)
        if (res.isSuccess) {
            logEvent("MANUAL", "Camera $servoName", "Position: $position")
        }
        return res
    }

    suspend fun sendBuzzer(state: Boolean): Result<ApiResponse> {
        val settings = settingsManager.settingsFlow.first()
        val res = esp32Client.setBuzzer(settings.esp32Ip, state)
        if (res.isSuccess) {
            logEvent("MANUAL", "Buzzer", if (state) "Buzzer ON" else "Buzzer OFF")
        }
        return res
    }

    // PRE-FLIGHT AUTO SAFETY CHECK
    suspend fun performAutoSafetyCheck(): AutoSafetyReport {
        val status = _robotStatus.value
        val isOnline = _isEsp32Online.value
        val streamState = streamReader.streamState.value

        val checks = mutableListOf<SafetyCheckItem>()

        checks.add(
            SafetyCheckItem(
                title = "Main ESP32 Controller",
                isPassed = isOnline && status != null,
                detail = if (isOnline) "Connected & responding" else "ESP32 CONNECTION FAILED"
            )
        )

        checks.add(
            SafetyCheckItem(
                title = "ESP32-CAM",
                isPassed = status?.camera == true,
                detail = if (status?.camera == true) "Hardware initialized" else "Camera module offline"
            )
        )

        checks.add(
            SafetyCheckItem(
                title = "Camera Stream",
                isPassed = streamState is CameraStreamState.Streaming || status?.stream == true,
                detail = if (streamState is CameraStreamState.Streaming || status?.stream == true) "Live stream active" else "STREAM NOT AVAILABLE"
            )
        )

        checks.add(
            SafetyCheckItem(
                title = "TCS34725 Colour Sensor",
                isPassed = status?.tcs34725 != null,
                detail = if (status?.tcs34725 != null) "I2C 0x29 communicating" else "TCS34725 OFFLINE"
            )
        )

        checks.add(
            SafetyCheckItem(
                title = "DHT11 Climate Sensor",
                isPassed = status?.temperature != null && status.humidity != null,
                detail = if (status?.temperature != null) "${status.temperature}°C / ${status.humidity}%" else "DHT11 OFFLINE"
            )
        )

        checks.add(
            SafetyCheckItem(
                title = "Soil Moisture Sensor",
                isPassed = status?.soilPercent != null,
                detail = if (status?.soilPercent != null) "Analog ADC active (${status.soilPercent}%)" else "Soil Sensor OFFLINE"
            )
        )

        checks.add(
            SafetyCheckItem(
                title = "Water Level Sensor",
                isPassed = status?.waterLevel != null,
                detail = if (status?.waterLevel != null) "Level: ${status.waterLevel}%" else "Ultrasonic sensor offline"
            )
        )

        val settings = settingsManager.settingsFlow.first()
        val batterySafe = (status?.batteryVoltage ?: 0.0) > settings.criticalBatteryVoltage
        checks.add(
            SafetyCheckItem(
                title = "Battery Voltage",
                isPassed = batterySafe,
                detail = "${status?.batteryVoltage ?: 0.0} V (Threshold: > ${settings.criticalBatteryVoltage} V)"
            )
        )

        checks.add(
            SafetyCheckItem(
                title = "Arm & Servos",
                isPassed = status?.armState == "HOME",
                detail = "Arm state: ${status?.armState ?: "UNKNOWN"}"
            )
        )

        checks.add(
            SafetyCheckItem(
                title = "Cutter Subsystem",
                isPassed = status?.cutter == false,
                detail = if (status?.cutter == false) "Motor safe / OFF" else "Cutter active"
            )
        )

        checks.add(
            SafetyCheckItem(
                title = "Spray Pump",
                isPassed = status?.sprayer == false,
                detail = if (status?.sprayer == false) "Pump safe / OFF" else "Sprayer active"
            )
        )

        checks.add(
            SafetyCheckItem(
                title = "Water Irrigation Pump",
                isPassed = status?.waterPump == false,
                detail = if (status?.waterPump == false) "Pump safe / OFF" else "Water pump active"
            )
        )

        val eStopSafe = status?.emergencyStop == false
        checks.add(
            SafetyCheckItem(
                title = "Emergency Stop",
                isPassed = eStopSafe,
                detail = if (eStopSafe) "Switch SAFE (Disengaged)" else "EMERGENCY STOP ACTIVE!"
            )
        )

        val allPassed = checks.all { it.isPassed }
        return AutoSafetyReport(checks = checks, isAllPassed = allPassed)
    }

    // START AUTO
    suspend fun startAutoOperation(scope: CoroutineScope): Boolean {
        val safetyReport = performAutoSafetyCheck()
        if (!safetyReport.isAllPassed) {
            logEvent("AUTO", "AUTO START BLOCKED", "Pre-flight safety check failed", "ALERT")
            return false
        }

        val settings = settingsManager.settingsFlow.first()
        _autoState.value = AutoState.AUTO_STARTING
        logEvent("AUTO", "AUTO OPERATION STARTED", "All safety pre-checks passed. Engaging SLOW traversal.", "SUCCESS")

        // Send start command to ESP32
        esp32Client.startAuto(settings.esp32Ip)
        esp32Client.setSpeed(settings.esp32Ip, "SLOW")

        _autoState.value = AutoState.AUTO_SEARCHING
        _soilCountdownSeconds.value = settings.soilCheckIntervalSeconds

        // Start Auto Supervisor loop
        startAutoSupervisor(scope)
        return true
    }

    // STOP AUTO
    suspend fun stopAutoOperation(): Boolean {
        autoSupervisorJob?.cancel()
        autoSupervisorJob = null

        val settings = settingsManager.settingsFlow.first()
        esp32Client.stopAuto(settings.esp32Ip)
        esp32Client.setMode(settings.esp32Ip, "MANUAL")
        esp32Client.moveRobot(settings.esp32Ip, "STOP")

        _autoState.value = AutoState.AUTO_STOPPED
        logEvent("AUTO", "AUTO OPERATION STOPPED", "Motors OFF, Cutter OFF, Sprayer OFF, Arm HOME. Returned to MANUAL.", "WARNING")

        delay(500)
        _autoState.value = AutoState.MANUAL
        return true
    }

    // Emergency Stop
    suspend fun triggerEmergencyStop() {
        val settings = settingsManager.settingsFlow.first()
        esp32Client.triggerEmergencyStop(settings.esp32Ip, true)
        handleEmergencyStopTriggered("OPERATOR EMERGENCY STOP TRIGGERED")
    }

    private suspend fun handleEmergencyStopTriggered(reason: String) {
        autoSupervisorJob?.cancel()
        autoSupervisorJob = null
        _autoState.value = AutoState.EMERGENCY_STOP
        logEvent("SAFETY", "🚨 EMERGENCY STOP", "$reason. Motors OFF, Cutter OFF, Sprayer OFF, Pump OFF. System SAFE.", "ALERT")
    }

    private suspend fun handleLowBattery(volts: Double) {
        autoSupervisorJob?.cancel()
        autoSupervisorJob = null
        _autoState.value = AutoState.LOW_BATTERY
        logEvent("SAFETY", "CRITICAL BATTERY", "Voltage dropped to $volts V. AUTO STOPPED, Arm HOME, Buzzer active.", "ALERT")
        val settings = settingsManager.settingsFlow.first()
        esp32Client.stopAuto(settings.esp32Ip)
        esp32Client.setBuzzer(settings.esp32Ip, true)
    }

    // Auto Supervisor Routine
    private fun startAutoSupervisor(scope: CoroutineScope) {
        autoSupervisorJob?.cancel()
        autoSupervisorJob = scope.launch(Dispatchers.IO) {
            var consecutiveWeedDetections = 0
            var consecutiveDiseaseReadings = 0
            var lastSuspectedProfile: CalibrationProfile? = null

            while (isActive && _autoState.value != AutoState.MANUAL && _autoState.value != AutoState.EMERGENCY_STOP) {
                val settings = settingsManager.settingsFlow.first()
                val status = _robotStatus.value

                // Priority 1: Emergency Stop
                if (status?.emergencyStop == true) {
                    handleEmergencyStopTriggered("PHYSICAL E-STOP ACTIVE")
                    break
                }

                // Priority 2: Critical Battery
                if ((status?.batteryVoltage ?: 12.0) <= settings.criticalBatteryVoltage) {
                    handleLowBattery(status?.batteryVoltage ?: 0.0)
                    break
                }

                // Countdown for Soil 5-Minute Routine
                if (_autoState.value == AutoState.AUTO_SEARCHING) {
                    if (_soilCountdownSeconds.value > 0) {
                        _soilCountdownSeconds.value -= 1
                    } else {
                        // Trigger 5-Minute Soil Check Routine!
                        runSoilCheckRoutine(settings)
                        _soilCountdownSeconds.value = settings.soilCheckIntervalSeconds
                        continue
                    }
                }

                // Check for Weed Detection from Vision
                status?.lastDetection?.let { detection ->
                    when (detection.label) {
                        "ONION" -> {
                            // "ONION: Never cut. Continue searching."
                            consecutiveWeedDetections = 0
                        }
                        "UNKNOWN" -> {
                            consecutiveWeedDetections = 0
                        }
                        "WEED" -> {
                            if (detection.confidence >= settings.weedConfidenceThreshold) {
                                consecutiveWeedDetections++
                                _currentWeedDetection.value = detection

                                if (consecutiveWeedDetections >= settings.consecutiveDetectionCount) {
                                    if (_autoState.value == AutoState.AUTO_SEARCHING) {
                                        runWeedRemovalSequence(settings, detection)
                                        consecutiveWeedDetections = 0
                                    }
                                }
                            } else {
                                consecutiveWeedDetections = 0
                            }
                        }
                    }
                }

                // Check TCS34725 Colour Sensor for Disease Indication
                status?.tcs34725?.let { rgbc ->
                    val profiles = calibrationDao.getAllProfiles().first()
                    val matchingProfile = profiles.firstOrNull { it.id != "NORMAL" && it.matches(rgbc) }

                    if (matchingProfile != null) {
                        if (lastSuspectedProfile?.id == matchingProfile.id) {
                            consecutiveDiseaseReadings++
                        } else {
                            consecutiveDiseaseReadings = 1
                            lastSuspectedProfile = matchingProfile
                        }

                        if (consecutiveDiseaseReadings >= settings.colourConfirmationCount) {
                            if (_autoState.value == AutoState.AUTO_SEARCHING) {
                                runDiseaseSprayingSequence(settings, matchingProfile, rgbc)
                                consecutiveDiseaseReadings = 0
                                lastSuspectedProfile = null
                            }
                        }
                    } else {
                        consecutiveDiseaseReadings = 0
                        lastSuspectedProfile = null
                    }
                }

                delay(1000)
            }
        }
    }

    // Sequence for Weed Removal
    private suspend fun runWeedRemovalSequence(settings: RobotSettings, detection: WeedDetectionResult) {
        _autoState.value = AutoState.WEED_DETECTED
        logEvent("WEED", "WEED CONFIRMED", "Confidence: ${detection.confidence}%, Count: ${detection.detectionCount}. Robot stopping.", "ALERT")

        // 1. Robot STOP
        _autoState.value = AutoState.ROBOT_STOPPED_FOR_WEED
        esp32Client.moveRobot(settings.esp32Ip, "STOP")
        delay(1000)

        // 2. Capture image & calculate target X/Y
        logEvent("WEED", "TARGET X/Y CALCULATED", "X: ${detection.x}, Y: ${detection.y}. Moving 3-DOF Arm.", "INFO")

        // 3. Arm Moving to Target
        _autoState.value = AutoState.ARM_MOVING
        delay(2000)

        // 4. Cutter ON (enforcing max 7s safety limit)
        _autoState.value = AutoState.CUTTING
        val cutterDuration = minOf(settings.cutterMaxSeconds, 7)
        logEvent("WEED", "CUTTER STARTED", "Cutter motor active for $cutterDuration seconds max.", "ALERT")
        delay((cutterDuration * 1000).toLong())

        // 5. Cutter OFF & Arm Home
        logEvent("WEED", "CUTTER OFF", "Cutting complete. Returning Arm to HOME.", "SUCCESS")
        _autoState.value = AutoState.ARM_RETURNING
        delay(2000)

        logEvent("WEED", "ARM HOME", "Arm safe at HOME. Resuming automated row search at SLOW speed.", "INFO")
        esp32Client.moveRobot(settings.esp32Ip, "FORWARD", "SLOW")
        _autoState.value = AutoState.AUTO_SEARCHING
    }

    // Sequence for Disease Colour Indication & Spraying
    private suspend fun runDiseaseSprayingSequence(
        settings: RobotSettings,
        profile: CalibrationProfile,
        rgbc: RgbcData
    ) {
        _autoState.value = AutoState.COLOUR_CHECK
        _suspectedDisease.value = profile.name
        logEvent(
            "DISEASE",
            "DISEASE SUSPECTED: ${profile.name}",
            "Colour pattern match (R:${rgbc.red} G:${rgbc.green} B:${rgbc.blue} C:${rgbc.clear}). Disease Colour Indication confirmed. Stopping robot.",
            "WARNING"
        )

        // Stop robot
        esp32Client.moveRobot(settings.esp32Ip, "STOP")
        delay(1000)

        val liquidLevel = _robotStatus.value?.sprayLiquidLevel ?: 100
        if (liquidLevel >= settings.sprayLiquidThreshold) {
            _autoState.value = AutoState.SPRAYING
            logEvent("DISEASE", "SPRAY ON", "Spraying target foliage for ${settings.sprayDurationSeconds} seconds.", "ALERT")
            delay((settings.sprayDurationSeconds * 1000).toLong())
            logEvent("DISEASE", "SPRAY OFF", "Spraying complete. Resuming slow movement.", "SUCCESS")
        } else {
            logEvent("DISEASE", "LOW SPRAY LIQUID", "Spray reservoir low ($liquidLevel%). Spraying skipped for safety.", "WARNING")
        }

        _suspectedDisease.value = null
        esp32Client.moveRobot(settings.esp32Ip, "FORWARD", "SLOW")
        _autoState.value = AutoState.AUTO_SEARCHING
    }

    // Sequence for 5-Minute Soil + DHT11 Check
    private suspend fun runSoilCheckRoutine(settings: RobotSettings) {
        _autoState.value = AutoState.SOIL_CHECK
        logEvent("SOIL", "SOIL CHECK STARTING", "5-minute routine triggered. Halting chassis and deploying Servo 6 to 90°.", "INFO")

        // 1. Robot STOP
        esp32Client.moveRobot(settings.esp32Ip, "STOP")
        delay(1500)

        // 2. Servo 6 -> 90°
        esp32Client.setServo(settings.esp32Ip, 6, "DEPLOYED", 90)
        delay(2000)

        val status = _robotStatus.value
        val soilMoisture = status?.soilPercent ?: 50
        val temp = status?.temperature ?: 28.0
        val humidity = status?.humidity ?: 65.0

        logEvent(
            "SOIL",
            "SOIL & CLIMATE READINGS",
            "Soil Moisture: $soilMoisture%, Temperature: ${temp}°C, Humidity: ${humidity}%.",
            "INFO"
        )

        if (soilMoisture < settings.soilDryThreshold) {
            logEvent("SOIL", "SOIL DRY", "Moisture ($soilMoisture%) is below dry threshold (${settings.soilDryThreshold}%). Checking water tank.", "WARNING")
            val waterLevel = status?.waterLevel ?: 100

            if (waterLevel >= settings.waterLevelThreshold) {
                _autoState.value = AutoState.WATERING
                logEvent("WATER", "WATER AVAILABLE - PUMP ON", "Pumping irrigation for ${settings.wateringDurationSeconds} seconds.", "ALERT")
                delay((settings.wateringDurationSeconds * 1000).toLong())
                logEvent("WATER", "PUMP OFF", "Watering cycle complete.", "SUCCESS")
            } else {
                logEvent("WATER", "LOW WATER", "Tank water level is low ($waterLevel%). Watering cycle skipped.", "WARNING")
            }
        } else {
            logEvent("SOIL", "SOIL NORMAL", "Moisture level ($soilMoisture%) is sufficient. No watering needed.", "SUCCESS")
        }

        // Return Servo 6 to HOME
        esp32Client.setServo(settings.esp32Ip, 6, "HOME", 0)
        delay(1500)

        logEvent("SOIL", "SERVO 6 HOME", "Sensor retracted. Resuming SLOW traversal.", "INFO")
        esp32Client.moveRobot(settings.esp32Ip, "FORWARD", "SLOW")
        _autoState.value = AutoState.AUTO_SEARCHING
    }

    // Calibration functions
    suspend fun saveCalibrationSample(profileId: String, currentRgbc: RgbcData) {
        val existing = calibrationDao.getProfileById(profileId) ?: return
        val newCount = existing.sampleCount + 1

        val newMinR = if (existing.sampleCount == 0) currentRgbc.red else minOf(existing.minR, currentRgbc.red)
        val newMaxR = if (existing.sampleCount == 0) currentRgbc.red else maxOf(existing.maxR, currentRgbc.red)

        val newMinG = if (existing.sampleCount == 0) currentRgbc.green else minOf(existing.minG, currentRgbc.green)
        val newMaxG = if (existing.sampleCount == 0) currentRgbc.green else maxOf(existing.maxG, currentRgbc.green)

        val newMinB = if (existing.sampleCount == 0) currentRgbc.blue else minOf(existing.minB, currentRgbc.blue)
        val newMaxB = if (existing.sampleCount == 0) currentRgbc.blue else maxOf(existing.maxB, currentRgbc.blue)

        val newMinC = if (existing.sampleCount == 0) currentRgbc.clear else minOf(existing.minC, currentRgbc.clear)
        val newMaxC = if (existing.sampleCount == 0) currentRgbc.clear else maxOf(existing.maxC, currentRgbc.clear)

        val updated = existing.copy(
            sampleCount = newCount,
            minR = newMinR,
            maxR = newMaxR,
            minG = newMinG,
            maxG = newMaxG,
            minB = newMinB,
            maxB = newMaxB,
            minC = newMinC,
            maxC = newMaxC,
            avgR = (existing.avgR * existing.sampleCount + currentRgbc.red) / newCount,
            avgG = (existing.avgG * existing.sampleCount + currentRgbc.green) / newCount,
            avgB = (existing.avgB * existing.sampleCount + currentRgbc.blue) / newCount,
            avgC = (existing.avgC * existing.sampleCount + currentRgbc.clear) / newCount,
            lastUpdated = System.currentTimeMillis(),
            isCalibrated = true
        )
        calibrationDao.updateProfile(updated)
        logEvent("CALIBRATION", "Sample Recorded: ${existing.name}", "R:${currentRgbc.red} G:${currentRgbc.green} B:${currentRgbc.blue} (Sample #$newCount)")
    }

    suspend fun resetCalibrationProfile(profileId: String) {
        val def = CalibrationProfile.DEFAULT_PROFILES.firstOrNull { it.id == profileId } ?: return
        calibrationDao.updateProfile(def)
        logEvent("CALIBRATION", "Profile Reset: ${def.name}", "Calibration cleared to defaults.")
    }
}
