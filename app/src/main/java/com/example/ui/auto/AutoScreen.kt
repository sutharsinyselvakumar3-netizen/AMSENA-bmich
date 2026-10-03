package com.example.ui.auto

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutoSafetyReport
import com.example.data.model.AutoState
import com.example.data.model.RobotStatus
import com.example.data.model.WeedDetectionResult
import com.example.data.remote.CameraStreamState
import com.example.ui.components.CameraStreamView
import com.example.ui.components.EmergencyStopBanner
import com.example.ui.components.PreflightSafetyCheckDialog
import com.example.ui.theme.AutoAccent
import com.example.ui.theme.AutoBg
import com.example.ui.theme.AutoSurface
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGrey
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AutoScreen(
    robotStatus: RobotStatus?,
    autoState: AutoState,
    streamState: CameraStreamState,
    soilCountdownSeconds: Int,
    currentWeedDetection: WeedDetectionResult?,
    suspectedDisease: String?,
    showStartDialog: Boolean,
    showStopDialog: Boolean,
    safetyReport: AutoSafetyReport?,
    onRequestStartAuto: () -> Unit,
    onConfirmStartAuto: () -> Unit,
    onDismissStartDialog: () -> Unit,
    onRequestStopAuto: () -> Unit,
    onConfirmStopAuto: () -> Unit,
    onDismissStopDialog: () -> Unit,
    onRetryStream: () -> Unit,
    onSettingsClick: () -> Unit,
    onEmergencyStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEmergencyActive = robotStatus?.emergencyStop == true || autoState == AutoState.EMERGENCY_STOP
    val isAutoActive = autoState != AutoState.MANUAL && autoState != AutoState.EMERGENCY_STOP && autoState != AutoState.AUTO_STOPPED

    // Confirmation dialog before stopping AUTO
    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = onDismissStopDialog,
            title = {
                Text("Stop Automatic Operation?", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Text(
                    "Stop automatic operation and return to manual mode?\nMotors will stop, cutter/sprayers will disengage, and arm will return to HOME safe position.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmStopAuto,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("STOP AUTO", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismissStopDialog,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CANCEL", color = TextSecondary)
                }
            },
            containerColor = AutoSurface
        )
    }

    // Pre-flight Safety Checklist Dialog before starting AUTO
    if (showStartDialog) {
        PreflightSafetyCheckDialog(
            report = safetyReport,
            onConfirm = onConfirmStartAuto,
            onDismiss = onDismissStartDialog
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AutoBg)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        // Emergency Banner
        EmergencyStopBanner(
            isEmergencyActive = isEmergencyActive,
            onEStopClick = onEmergencyStop
        )

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AUTO / AI VISION MODE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Autonomous Weed Removal & Agronomic Care",
                    fontSize = 12.sp,
                    color = AutoAccent
                )
            }

            if (isAutoActive) {
                Button(
                    onClick = onRequestStopAuto,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("stop_auto_button")
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("STOP AUTO", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onRequestStartAuto,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("start_auto_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("START AUTO", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Live Vision Camera Stream
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            CameraStreamView(
                streamState = streamState,
                onRetry = onRetryStream,
                onSettingsClick = onSettingsClick
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // State Machine Live Status Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = AutoSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
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
                                .background(if (isAutoActive) StatusBlue else StatusGrey)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "STATE: ${autoState.label}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isAutoActive) AutoAccent else TextSecondary
                        )
                    }
                    Text(
                        text = "Speed: SLOW (Locked)",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = autoState.description,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Soil + DHT11 5-Minute Routine Card
        Card(
            colors = CardDefaults.cardColors(containerColor = AutoSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = AutoAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SOIL + DHT11 5-MINUTE ROUTINE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }

                    val mins = soilCountdownSeconds / 60
                    val secs = soilCountdownSeconds % 60
                    val timeStr = "%02d:%02d".format(mins, secs)
                    Text(
                        text = "Next: $timeStr",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (soilCountdownSeconds <= 10) StatusYellow else AutoAccent
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Every 5 min: Robot halts -> Servo 6 deploys to 90° -> Reads soil moisture & DHT11. If soil is dry (< 40%) and water is available: irrigates 30s max.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Moisture", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = "${robotStatus?.soilPercent ?: "--"}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                    Column {
                        Text("Temp", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = "${robotStatus?.temperature ?: "--"} °C",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                    Column {
                        Text("Humidity", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = "${robotStatus?.humidity ?: "--"}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                    Column {
                        Text("Water Tank", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = if ((robotStatus?.waterLevel ?: 0) >= 20) "AVAILABLE" else "LOW",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if ((robotStatus?.waterLevel ?: 0) >= 20) StatusGreen else StatusYellow
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // TCS34725 Colour System & Disease Indication
        Card(
            colors = CardDefaults.cardColors(containerColor = AutoSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = AutoAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TCS34725 COLOUR SYSTEM",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }

                    if (!suspectedDisease.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StatusYellow.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "DISEASE SUSPECTED",
                                color = StatusYellow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val rgbc = robotStatus?.tcs34725
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Red" to (rgbc?.red ?: 0),
                        "Green" to (rgbc?.green ?: 0),
                        "Blue" to (rgbc?.blue ?: 0),
                        "Clear" to (rgbc?.clear ?: 0)
                    ).forEach { (channel, value) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(channel, fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = if (rgbc != null) "$value" else "--",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Suspected Disease Warning Banner if active
                if (!suspectedDisease.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusYellow.copy(alpha = 0.15f))
                            .border(1.dp, StatusYellow, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "DISEASE COLOUR INDICATION: $suspectedDisease",
                                fontWeight = FontWeight.Bold,
                                color = StatusYellow,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Target abnormal colour pattern confirmed over consecutive readings. Halting chassis for automated spray application.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Mandatory Spray Guidance Disclaimer
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "“Use only an appropriate, legally permitted product according to its label and local agricultural guidance.”",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Automated Weed Removal Sequence State
        Card(
            colors = CardDefaults.cardColors(containerColor = AutoSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = AutoAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WEED IDENTIFICATION & TARGETING",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val detection = currentWeedDetection ?: robotStatus?.lastDetection
                if (detection != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Target: ${detection.label}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (detection.label == "ONION") StatusGreen else StatusRed
                        )
                        Text(
                            text = "Confidence: ${detection.confidence}%",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Count: ${detection.detectionCount}/3",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (detection.label == "ONION")
                            "ONION DETECTED: PROTECT - DO NOT CUT"
                        else if (detection.label == "WEED")
                            "WEED CONFIRMED: Engaging targeting & cutting sequence (max 7s)"
                        else
                            "UNKNOWN: IGNORED",
                        fontSize = 12.sp,
                        color = if (detection.label == "ONION") StatusGreen else if (detection.label == "WEED") StatusRed else TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "Searching onion row... Vision pipeline monitoring for weed species.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hardware Priority Hierarchy Card
        Card(
            colors = CardDefaults.cardColors(containerColor = AutoSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = AutoAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ACTUATOR OPERATION PRIORITY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "1. EMERGENCY STOP  >  2. CRITICAL BATTERY  >  3. WEED CUTTING  >  4. DISEASE SPRAYING  >  5. SOIL WATERING  >  6. SLOW TRAVERSAL\nOnly one automated actuator sequence executes at a time.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
