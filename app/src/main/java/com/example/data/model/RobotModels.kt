package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 16 Finite States of the Robot State Machine
 */
enum class AutoState(val label: String, val description: String) {
    MANUAL("MANUAL", "Manual direct operator control"),
    AUTO_STARTING("AUTO_STARTING", "Running pre-flight hardware safety checks"),
    AUTO_SEARCHING("AUTO_SEARCHING", "Automated row traversal at SLOW speed"),
    WEED_DETECTED("WEED_DETECTED", "Vision target identified, confirming weed"),
    ROBOT_STOPPED_FOR_WEED("ROBOT_STOPPED_FOR_WEED", "Chassis halted for end-effector targeting"),
    ARM_MOVING("ARM_MOVING", "3-DOF Arm moving to weed coordinates"),
    CUTTING("CUTTING", "Cutter motor active (max 7s limit)"),
    ARM_RETURNING("ARM_RETURNING", "Arm returning to HOME safe position"),
    COLOUR_CHECK("COLOUR_CHECK", "TCS34725 reading RGB values for disease indication"),
    SPRAYING("SPRAYING", "Liquid treatment active (30s duration)"),
    SOIL_CHECK("SOIL_CHECK", "Servo 6 deployed to 90°, reading soil & DHT11"),
    WATERING("WATERING", "Water irrigation pump active (30s duration)"),
    LOW_BATTERY("LOW_BATTERY", "Critical battery voltage threshold reached"),
    EMERGENCY_STOP("EMERGENCY_STOP", "Hardware Emergency Stop switch engaged"),
    FAULT("FAULT", "Hardware subsystem communication or sensor failure"),
    AUTO_STOPPED("AUTO_STOPPED", "Automatic operation cancelled, actuators safe")
}

@JsonClass(generateAdapter = true)
data class RgbcData(
    @Json(name = "red") val red: Int = 0,
    @Json(name = "green") val green: Int = 0,
    @Json(name = "blue") val blue: Int = 0,
    @Json(name = "clear") val clear: Int = 0
)

@JsonClass(generateAdapter = true)
data class WeedDetectionResult(
    @Json(name = "label") val label: String = "UNKNOWN", // WEED, ONION, UNKNOWN
    @Json(name = "confidence") val confidence: Int = 0, // 0-100
    @Json(name = "detectionCount") val detectionCount: Int = 0,
    @Json(name = "x") val x: Int = 0,
    @Json(name = "y") val y: Int = 0
)

@JsonClass(generateAdapter = true)
data class RobotStatus(
    @Json(name = "mode") val mode: String = "MANUAL",
    @Json(name = "robot") val robot: String = "STOPPED", // STOPPED, FORWARD, REVERSE, LEFT, RIGHT, MOVING
    @Json(name = "speed") val speed: String = "SLOW", // SLOW, NORMAL, FAST
    @Json(name = "camera") val camera: Boolean = false,
    @Json(name = "stream") val stream: Boolean = false,
    @Json(name = "aiReady") val aiReady: Boolean = false,
    @Json(name = "emergencyStop") val emergencyStop: Boolean = false,
    @Json(name = "batteryVoltage") val batteryVoltage: Double? = null,
    @Json(name = "batteryPercent") val batteryPercent: Int? = null,
    @Json(name = "soilPercent") val soilPercent: Int? = null,
    @Json(name = "temperature") val temperature: Double? = null,
    @Json(name = "humidity") val humidity: Double? = null,
    @Json(name = "waterLevel") val waterLevel: Int? = null, // e.g. 0-100%
    @Json(name = "sprayLiquidLevel") val sprayLiquidLevel: Int? = null, // e.g. 0-100%
    @Json(name = "armState") val armState: String = "HOME", // HOME, MOVING, TARGET
    @Json(name = "servo1Pan") val servo1Pan: Int = 90,
    @Json(name = "servo2Tilt") val servo2Tilt: Int = 90,
    @Json(name = "servo6Soil") val servo6Soil: Int = 0, // 0=HOME, 90=DEPLOYED
    @Json(name = "cutter") val cutter: Boolean = false,
    @Json(name = "sprayer") val sprayer: Boolean = false,
    @Json(name = "waterPump") val waterPump: Boolean = false,
    @Json(name = "buzzer") val buzzer: Boolean = false,
    @Json(name = "tcs34725") val tcs34725: RgbcData? = null,
    @Json(name = "lastDetection") val lastDetection: WeedDetectionResult? = null,
    @Json(name = "autoState") val autoState: String = "MANUAL",
    @Json(name = "uptimeSeconds") val uptimeSeconds: Long? = null,
    @Json(name = "wifiRssi") val wifiRssi: Int? = null
)

// Request bodies for ESP32 REST API
@JsonClass(generateAdapter = true)
data class ModeRequest(
    @Json(name = "mode") val mode: String // "MANUAL" or "AUTO"
)

@JsonClass(generateAdapter = true)
data class RobotMoveRequest(
    @Json(name = "direction") val direction: String, // "FORWARD", "REVERSE", "LEFT", "RIGHT", "STOP"
    @Json(name = "speed") val speed: String? = null // "SLOW", "NORMAL", "FAST"
)

@JsonClass(generateAdapter = true)
data class SpeedRequest(
    @Json(name = "speed") val speed: String // "SLOW", "NORMAL", "FAST"
)

@JsonClass(generateAdapter = true)
data class ServoRequest(
    @Json(name = "servoId") val servoId: Int, // 1 for PAN, 2 for TILT
    @Json(name = "position") val position: String, // "LEFT", "CENTER", "RIGHT" or "UP", "CENTER", "DOWN"
    @Json(name = "angle") val angle: Int? = null
)

@JsonClass(generateAdapter = true)
data class BuzzerRequest(
    @Json(name = "state") val state: Boolean
)

@JsonClass(generateAdapter = true)
data class EmergencyStopRequest(
    @Json(name = "emergencyStop") val emergencyStop: Boolean
)

@JsonClass(generateAdapter = true)
data class ApiResponse(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "message") val message: String = "OK"
)

/**
 * Result of the pre-flight safety check before entering AUTO mode
 */
data class SafetyCheckItem(
    val title: String,
    val isPassed: Boolean,
    val detail: String,
    val isCritical: Boolean = true
)

data class AutoSafetyReport(
    val checks: List<SafetyCheckItem>,
    val isAllPassed: Boolean
)
