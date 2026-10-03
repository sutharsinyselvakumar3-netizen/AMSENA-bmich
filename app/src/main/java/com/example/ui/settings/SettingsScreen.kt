package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shower
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RobotSettings
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun SettingsScreen(
    currentSettings: RobotSettings,
    onSaveNetwork: (ssid: String, pass: String, esp32Ip: String, camIp: String, port: Int) -> Unit,
    onSaveAi: (confidence: Int, consecutive: Int) -> Unit,
    onSaveAgronomy: (soilDry: Int, waterThresh: Int, sprayThresh: Int, criticalBattery: Double) -> Unit,
    onOpenCalibration: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Network states
    var ssid by remember(currentSettings.wifiSsid) { mutableStateOf(currentSettings.wifiSsid) }
    var password by remember(currentSettings.wifiPassword) { mutableStateOf(currentSettings.wifiPassword) }
    var esp32Ip by remember(currentSettings.esp32Ip) { mutableStateOf(currentSettings.esp32Ip) }
    var camIp by remember(currentSettings.esp32CamIp) { mutableStateOf(currentSettings.esp32CamIp) }
    var streamPort by remember(currentSettings.streamPort) { mutableStateOf(currentSettings.streamPort.toString()) }

    // AI states
    var weedConfidence by remember(currentSettings.weedConfidenceThreshold) {
        mutableIntStateOf(currentSettings.weedConfidenceThreshold)
    }
    var consecutiveDetections by remember(currentSettings.consecutiveDetectionCount) {
        mutableIntStateOf(currentSettings.consecutiveDetectionCount)
    }

    // Agronomy & Safety states
    var soilDryThresh by remember(currentSettings.soilDryThreshold) {
        mutableIntStateOf(currentSettings.soilDryThreshold)
    }
    var waterThresh by remember(currentSettings.waterLevelThreshold) {
        mutableIntStateOf(currentSettings.waterLevelThreshold)
    }
    var sprayThresh by remember(currentSettings.sprayLiquidThreshold) {
        mutableIntStateOf(currentSettings.sprayLiquidThreshold)
    }
    var critBattery by remember(currentSettings.criticalBatteryVoltage) {
        mutableDoubleStateOf(currentSettings.criticalBatteryVoltage)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "SETTINGS & HARDWARE CONFIGURATION",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Configure IP addresses, thresholds, and sensor calibrations",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Dedicated Button to open TCS34725 Colour Calibration
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TCS34725 Disease Calibration",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Fusarium, Purple Blotch, Downy Mildew & Neck Rot",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = onOpenCalibration,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("open_calibration_screen_button")
                    ) {
                        Text("Calibrate", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Section 1: Network & Wi-Fi Configuration
        item {
            SettingsCard(title = "NETWORK & IP CONNECTIONS", icon = Icons.Default.Wifi) {
                OutlinedTextField(
                    value = esp32Ip,
                    onValueChange = { esp32Ip = it },
                    label = { Text("ESP32 Controller IP Address") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_esp32_ip"),
                    singleLine = true,
                    colors = textFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = camIp,
                    onValueChange = { camIp = it },
                    label = { Text("ESP32-CAM IP Address") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_cam_ip"),
                    singleLine = true,
                    colors = textFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = streamPort,
                        onValueChange = { streamPort = it },
                        label = { Text("Stream Port") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_stream_port"),
                        singleLine = true,
                        colors = textFieldColors()
                    )
                    OutlinedTextField(
                        value = ssid,
                        onValueChange = { ssid = it },
                        label = { Text("Wi-Fi SSID") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = textFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Stream URL: http://$camIp:${streamPort}/stream\nCapture URL: http://$camIp/capture",
                    fontSize = 11.sp,
                    color = TextTertiary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val portInt = streamPort.toIntOrNull() ?: 81
                        onSaveNetwork(ssid, password, esp32Ip, camIp, portInt)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_network_settings_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Network Settings", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Section 2: AI Weed Detection Settings
        item {
            SettingsCard(title = "AI WEED VISION THRESHOLDS", icon = Icons.Default.AutoAwesome) {
                Text(
                    text = "Weed Confidence Threshold: $weedConfidence%",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = weedConfidence.toFloat(),
                    onValueChange = { weedConfidence = it.toInt() },
                    valueRange = 50f..99f,
                    steps = 48
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Consecutive Detections Required: $consecutiveDetections frames",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = consecutiveDetections.toFloat(),
                    onValueChange = { consecutiveDetections = it.toInt() },
                    valueRange = 1f..10f,
                    steps = 8
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onSaveAi(weedConfidence, consecutiveDetections) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save AI Vision Settings", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Section 3: Soil & Irrigation Parameters
        item {
            SettingsCard(title = "SOIL & WATER IRRIGATION", icon = Icons.Default.Opacity) {
                Text(
                    text = "Soil Dry Threshold: $soilDryThresh% (Triggers watering routine)",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = soilDryThresh.toFloat(),
                    onValueChange = { soilDryThresh = it.toInt() },
                    valueRange = 10f..60f,
                    steps = 49
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Water Tank Low Level Cutoff: $waterThresh%",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = waterThresh.toFloat(),
                    onValueChange = { waterThresh = it.toInt() },
                    valueRange = 5f..40f,
                    steps = 34
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Fixed Routines:\n• Soil Check Interval: 5 minutes (300s)\n• Watering Duration: 30 seconds max\n• Soil Monitoring Deployment: 30 seconds",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }
        }

        // Section 4: Spraying & Disease Treatment
        item {
            SettingsCard(title = "SPRAY TREATMENT PARAMETERS", icon = Icons.Default.Shower) {
                Text(
                    text = "Spray Tank Cutoff Threshold: $sprayThresh%",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = sprayThresh.toFloat(),
                    onValueChange = { sprayThresh = it.toInt() },
                    valueRange = 5f..40f,
                    steps = 34
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Spray Duration: 30 seconds per confirmed foliage target\n• Colour Confirmation: 3 consecutive readings required\n• Cutter Safety Limit: 7 seconds maximum (hardcoded in firmware)",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }
        }

        // Section 5: Battery Safety & Save All
        item {
            SettingsCard(title = "BATTERY CRITICAL VOLTAGE", icon = Icons.Default.SmartToy) {
                Text(
                    text = "Critical Battery Threshold: %.1f V".format(critBattery),
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = critBattery.toFloat(),
                    onValueChange = { critBattery = it.toDouble() },
                    valueRange = 9.5f..12.0f,
                    steps = 24
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onSaveAgronomy(soilDryThresh, waterThresh, sprayThresh, critBattery) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Agronomy & Battery Thresholds", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)
