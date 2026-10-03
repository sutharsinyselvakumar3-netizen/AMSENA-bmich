package com.example.ui.status

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutoState
import com.example.data.model.HardwarePinConfig
import com.example.data.model.OperationLog
import com.example.data.model.RobotStatus
import com.example.data.remote.CameraStreamState
import com.example.ui.components.EmergencyStopBanner
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGrey
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun SystemStatusScreen(
    isEsp32Online: Boolean,
    robotStatus: RobotStatus?,
    autoState: AutoState,
    streamState: CameraStreamState,
    logs: List<OperationLog>,
    onClearLogs: () -> Unit,
    onEmergencyStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showGpioTable by remember { mutableStateOf(false) }
    val isEmergencyActive = robotStatus?.emergencyStop == true || autoState == AutoState.EMERGENCY_STOP

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Emergency Stop Banner
        item {
            EmergencyStopBanner(
                isEmergencyActive = isEmergencyActive,
                onEStopClick = onEmergencyStop
            )
        }

        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SYSTEM STATUS & DIAGNOSTICS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Hardware Subsystems, Sensors, Actuators & Logs",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = onEmergencyStop,
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("status_e_stop_button")
                ) {
                    Icon(Icons.Default.Dangerous, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("E-STOP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // SECTION 1: CONNECTION STATUS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CONNECTION STATUS", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    StatusRow("ESP32 Main Controller", isEsp32Online, if (isEsp32Online) "Online & Polling" else "ESP32 CONNECTION FAILED")
                    StatusRow("ESP32-CAM Board", robotStatus?.camera == true, if (robotStatus?.camera == true) "Hardware Connected" else "Module Unreachable")
                    StatusRow("Wi-Fi Signal", isEsp32Online, if (isEsp32Online) "RSSI: ${robotStatus?.wifiRssi ?: -65} dBm" else "No Connection")
                    StatusRow("Camera Stream", streamState is CameraStreamState.Streaming, if (streamState is CameraStreamState.Streaming) "Live (Port 81)" else "STREAM NOT AVAILABLE")
                }
            }
        }

        // SECTION 2: SENSORS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Sensors, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SENSORS SUBSYSTEM", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val rgbc = robotStatus?.tcs34725
                    StatusRow("TCS34725 RGB Sensor", rgbc != null, if (rgbc != null) "R:${rgbc.red} G:${rgbc.green} B:${rgbc.blue} C:${rgbc.clear}" else "TCS34725 OFFLINE")
                    val temp = robotStatus?.temperature
                    StatusRow("DHT11 Climate Sensor", temp != null, if (temp != null) "${temp}°C / ${robotStatus.humidity}% RH" else "DHT11 OFFLINE")
                    val soil = robotStatus?.soilPercent
                    StatusRow("Soil Moisture Sensor", soil != null, if (soil != null) "$soil% (ADC Pin 34)" else "Soil Sensor OFFLINE")
                    val water = robotStatus?.waterLevel
                    StatusRow("Water Level Sensor", water != null, if (water != null) "$water% Tank" else "Ultrasonic OFFLINE")
                    val battery = robotStatus?.batteryVoltage
                    StatusRow("Battery Voltage", battery != null, if (battery != null) "%.2f V (%d%%)".format(battery, robotStatus.batteryPercent ?: 80) else "Battery Sensor OFFLINE")
                    StatusRow("Emergency Stop Input", robotStatus?.emergencyStop == false, if (robotStatus?.emergencyStop == false) "Normal / Safe" else "EMERGENCY ENGAGED")
                }
            }
        }

        // SECTION 3: ACTUATORS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ACTUATORS & SERVOS", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    StatusRow("Motors (L & R DC)", isEsp32Online, "${robotStatus?.robot ?: "STOPPED"} (${robotStatus?.speed ?: "SLOW"})")
                    StatusRow("Servo 1 (Camera PAN)", isEsp32Online, "${robotStatus?.servo1Pan ?: 90}° (GPIO 13)")
                    StatusRow("Servo 2 (Camera TILT)", isEsp32Online, "${robotStatus?.servo2Tilt ?: 90}° (GPIO 14)")
                    StatusRow("Servo 3 (Arm BASE)", isEsp32Online, "Arm: ${robotStatus?.armState ?: "HOME"} (GPIO 16)")
                    StatusRow("Servo 4 (Arm SHOULDER)", isEsp32Online, "Arm: ${robotStatus?.armState ?: "HOME"} (GPIO 17)")
                    StatusRow("Servo 5 (Cutter Pos)", isEsp32Online, "Arm: ${robotStatus?.armState ?: "HOME"} (GPIO 18)")
                    StatusRow("Servo 6 (Soil Deployment)", isEsp32Online, if (robotStatus?.servo6Soil == 90) "DEPLOYED (90°)" else "HOME (0°)")
                    StatusRow("Cutter Motor", robotStatus?.cutter != true, if (robotStatus?.cutter == true) "CUTTING (7s MAX)" else "OFF (Safe)")
                    StatusRow("Sprayer Pump", robotStatus?.sprayer != true, if (robotStatus?.sprayer == true) "SPRAYING (30s)" else "OFF (Safe)")
                    StatusRow("Water Pump", robotStatus?.waterPump != true, if (robotStatus?.waterPump == true) "IRRIGATING (30s)" else "OFF (Safe)")
                    StatusRow("Buzzer Driver", true, if (robotStatus?.buzzer == true) "AUDIBLE" else "SILENT")
                }
            }
        }

        // SECTION 4: GPIO HARDWARE PINOUT TABLE (Expandable)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showGpioTable = !showGpioTable }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ESP32 GPIO HARDWARE PINOUT (24 PINS)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        }
                        Icon(
                            imageVector = if (showGpioTable) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }

                    AnimatedVisibility(visible = showGpioTable) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            HardwarePinConfig.PINS.forEach { pin ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "GPIO ${pin.gpio}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.width(64.dp)
                                    )
                                    Text(
                                        text = pin.component,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = pin.type.name,
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION 5: APP LOG / OPERATION HISTORY
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("OPERATION HISTORY & AUDIT LOG", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        }

                        if (logs.isNotEmpty()) {
                            OutlinedButton(
                                onClick = onClearLogs,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Clear", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (logs.isEmpty()) {
                        Text(
                            text = "No recorded operations yet. Actions like Weed Removal, Soil Checks, and Spraying will appear here with timestamps.",
                            fontSize = 11.sp,
                            color = TextTertiary,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    } else {
                        logs.take(15).forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = log.timeFormatted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(58.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = log.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = when (log.level) {
                                            "ALERT" -> StatusRed
                                            "WARNING" -> StatusYellow
                                            "SUCCESS" -> StatusGreen
                                            else -> TextPrimary
                                        }
                                    )
                                    Text(
                                        text = log.detail,
                                        fontSize = 11.sp,
                                        color = TextSecondary
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

@Composable
private fun StatusRow(
    title: String,
    isGood: Boolean,
    detail: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isGood) StatusGreen else StatusRed)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, fontSize = 12.sp, color = TextPrimary)
        }
        Text(text = detail, fontSize = 11.sp, color = if (isGood) TextSecondary else StatusRed)
    }
}
