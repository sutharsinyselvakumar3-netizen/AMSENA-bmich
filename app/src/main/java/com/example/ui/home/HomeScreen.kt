package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shower
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutoState
import com.example.data.model.RobotStatus
import com.example.ui.components.EmergencyStopBanner
import com.example.ui.components.MetricCard
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGrey
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    isEsp32Online: Boolean,
    robotStatus: RobotStatus?,
    autoState: AutoState,
    connectionError: String?,
    lastContactTime: Long,
    onNavigate: (String) -> Unit,
    onEmergencyStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEmergencyActive = robotStatus?.emergencyStop == true || autoState == AutoState.EMERGENCY_STOP

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Emergency Alert Banner
        EmergencyStopBanner(
            isEmergencyActive = isEmergencyActive,
            onEStopClick = onEmergencyStop
        )

        // Top Status Header
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AI COMPANION",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Smart Onion Garden Robot",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Emergency Stop Button
                    Button(
                        onClick = onEmergencyStop,
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("home_e_stop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dangerous,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "E-STOP",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isEsp32Online) StatusGreen else StatusRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEsp32Online) "ESP32 ONLINE" else "ESP32 OFFLINE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isEsp32Online) StatusGreen else StatusRed
                        )
                    }

                    if (lastContactTime > 0) {
                        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastContactTime))
                        Text(
                            text = "Last sync: $timeStr",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (!isEsp32Online && !connectionError.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Connection: $connectionError",
                        fontSize = 11.sp,
                        color = StatusRed
                    )
                }
            }
        }

        // Real-Time Grid of 13 Required Cards
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Robot Chassis Card
            item {
                MetricCard(
                    title = "Robot",
                    value = if (!isEsp32Online) "OFFLINE" else (robotStatus?.robot ?: "STOPPED"),
                    subValue = if (isEsp32Online) "${robotStatus?.mode ?: "MANUAL"} • ${robotStatus?.speed ?: "SLOW"}" else "Chassis offline",
                    icon = Icons.Default.Agriculture,
                    statusColor = if (!isEsp32Online) StatusGrey else if (robotStatus?.robot == "MOVING" || robotStatus?.robot == "FORWARD") StatusBlue else StatusGreen,
                    onClick = { onNavigate("MANUAL") }
                )
            }

            // 2. Camera Card
            item {
                val camOnline = robotStatus?.camera == true
                val streamOnline = robotStatus?.stream == true
                MetricCard(
                    title = "Camera",
                    value = if (!isEsp32Online) "OFFLINE" else if (camOnline) "ONLINE" else "OFFLINE",
                    subValue = if (!isEsp32Online) "ESP32 unreached" else if (streamOnline) "Stream Online" else "Stream Offline",
                    icon = Icons.Default.Videocam,
                    statusColor = if (camOnline && streamOnline) StatusGreen else if (camOnline) StatusYellow else StatusGrey,
                    onClick = { onNavigate("MANUAL") }
                )
            }

            // 3. AI Detection Card
            item {
                val aiReady = robotStatus?.aiReady == true
                val detection = robotStatus?.lastDetection
                MetricCard(
                    title = "AI Vision",
                    value = if (!isEsp32Online) "OFFLINE" else if (aiReady) "AI READY" else "NOT READY",
                    subValue = if (detection != null) "${detection.label} (${detection.confidence}%)" else "No target in frame",
                    icon = Icons.Default.AutoAwesome,
                    statusColor = if (aiReady) StatusGreen else StatusGrey,
                    onClick = { onNavigate("AUTO") }
                )
            }

            // 4. Battery Card
            item {
                val volts = robotStatus?.batteryVoltage
                val pct = robotStatus?.batteryPercent
                val valStr = if (!isEsp32Online || volts == null) "OFFLINE" else "%.1f V".format(volts)
                val subStr = if (pct != null) "$pct%" else if (volts != null) if (volts > 11.5) "Good level" else "Low voltage" else "Sensor offline"
                MetricCard(
                    title = "Battery",
                    value = valStr,
                    subValue = subStr,
                    icon = Icons.Default.BatteryChargingFull,
                    statusColor = if (volts == null) StatusGrey else if (volts > 11.8) StatusGreen else if (volts > 11.1) StatusYellow else StatusRed
                )
            }

            // 5. Soil Moisture Card
            item {
                val soil = robotStatus?.soilPercent
                MetricCard(
                    title = "Soil Moisture",
                    value = if (!isEsp32Online || soil == null) "OFFLINE" else "$soil%",
                    subValue = if (soil != null) if (soil < 40) "Soil Dry - Watering needed" else "Optimal range" else "Sensor offline",
                    icon = Icons.Default.Opacity,
                    statusColor = if (soil == null) StatusGrey else if (soil >= 40) StatusGreen else StatusYellow
                )
            }

            // 6. Temperature Card
            item {
                val temp = robotStatus?.temperature
                MetricCard(
                    title = "Temperature",
                    value = if (!isEsp32Online || temp == null) "OFFLINE" else "%.1f °C".format(temp),
                    subValue = if (temp != null) "DHT11 Ambient" else "Sensor offline",
                    icon = Icons.Default.DeviceThermostat,
                    statusColor = if (temp != null) StatusGreen else StatusGrey
                )
            }

            // 7. Humidity Card
            item {
                val hum = robotStatus?.humidity
                MetricCard(
                    title = "Humidity",
                    value = if (!isEsp32Online || hum == null) "OFFLINE" else "%.0f%%".format(hum),
                    subValue = if (hum != null) "Relative Humidity" else "Sensor offline",
                    icon = Icons.Default.WaterDrop,
                    statusColor = if (hum != null) StatusGreen else StatusGrey
                )
            }

            // 8. Water Level Card
            item {
                val wLevel = robotStatus?.waterLevel
                val isNormal = wLevel != null && wLevel >= 20
                MetricCard(
                    title = "Water Level",
                    value = if (!isEsp32Online || wLevel == null) "OFFLINE" else if (isNormal) "NORMAL" else "LOW",
                    subValue = if (wLevel != null) "$wLevel% Tank capacity" else "Ultrasonic offline",
                    icon = Icons.Default.InvertColors,
                    statusColor = if (wLevel == null) StatusGrey else if (isNormal) StatusGreen else StatusRed
                )
            }

            // 9. Arm State Card
            item {
                val arm = robotStatus?.armState ?: "HOME"
                MetricCard(
                    title = "3-DOF Arm",
                    value = if (!isEsp32Online) "OFFLINE" else arm,
                    subValue = if (arm == "HOME") "Safe Home Position" else if (arm == "MOVING") "Traversing" else "Targeting Weed",
                    icon = Icons.Default.Build,
                    statusColor = if (arm == "HOME") StatusGreen else StatusBlue
                )
            }

            // 10. Cutter Motor Card
            item {
                val cutterOn = robotStatus?.cutter == true
                MetricCard(
                    title = "Cutter Motor",
                    value = if (!isEsp32Online) "OFFLINE" else if (cutterOn) "ON" else "OFF",
                    subValue = if (cutterOn) "ACTIVE (7s max limit)" else "Safe / Off",
                    icon = Icons.Default.ContentCut,
                    statusColor = if (cutterOn) StatusYellow else StatusGreen
                )
            }

            // 11. Spray Pump Card
            item {
                val sprayOn = robotStatus?.sprayer == true
                MetricCard(
                    title = "Spray Pump",
                    value = if (!isEsp32Online) "OFFLINE" else if (sprayOn) "ON" else "OFF",
                    subValue = if (sprayOn) "Spraying (30s max)" else "Safe / Off",
                    icon = Icons.Default.Shower,
                    statusColor = if (sprayOn) StatusBlue else StatusGreen
                )
            }

            // 12. Water Pump Card
            item {
                val pumpOn = robotStatus?.waterPump == true
                MetricCard(
                    title = "Water Pump",
                    value = if (!isEsp32Online) "OFFLINE" else if (pumpOn) "ON" else "OFF",
                    subValue = if (pumpOn) "Irrigating (30s max)" else "Safe / Off",
                    icon = Icons.Default.WaterDrop,
                    statusColor = if (pumpOn) StatusBlue else StatusGreen
                )
            }

            // 13. Emergency Interlock Card (Spans 2 columns)
            item(span = { GridItemSpan(2) }) {
                val eStop = robotStatus?.emergencyStop == true
                MetricCard(
                    title = "Safety Interlock",
                    value = if (eStop) "EMERGENCY STOP ENGAGED" else "SAFE",
                    subValue = if (eStop) "Physical hardware switch activated" else "Normal hardware safety status",
                    icon = Icons.Default.Security,
                    statusColor = if (eStop) StatusRed else StatusGreen,
                    onClick = onEmergencyStop
                )
            }
        }
    }
}
