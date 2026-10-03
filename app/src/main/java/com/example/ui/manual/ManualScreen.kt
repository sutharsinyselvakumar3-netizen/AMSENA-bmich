package com.example.ui.manual

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutoState
import com.example.data.model.RobotStatus
import com.example.data.remote.CameraStreamState
import com.example.ui.components.CameraStreamView
import com.example.ui.components.EmergencyStopBanner
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.ManualBg
import com.example.ui.theme.ManualSurface
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ManualScreen(
    robotStatus: RobotStatus?,
    autoState: AutoState,
    streamState: CameraStreamState,
    onMove: (String) -> Unit,
    onSpeed: (String) -> Unit,
    onCameraPan: (String) -> Unit,
    onCameraTilt: (String) -> Unit,
    onBuzzerToggle: () -> Unit,
    onRetryStream: () -> Unit,
    onSettingsClick: () -> Unit,
    onEmergencyStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEmergencyActive = robotStatus?.emergencyStop == true || autoState == AutoState.EMERGENCY_STOP
    val currentSpeed = robotStatus?.speed ?: "SLOW"
    val buzzerOn = robotStatus?.buzzer == true

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ManualBg)
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
                    text = "MANUAL CONTROL",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Chassis, Speed, Camera Pan/Tilt & Buzzer",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = onEmergencyStop,
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("manual_e_stop_button")
            ) {
                Icon(Icons.Default.Dangerous, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("E-STOP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Live Camera Stream at the TOP
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            CameraStreamView(
                streamState = streamState,
                onRetry = onRetryStream,
                onSettingsClick = onSettingsClick
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Movement Controls Card: Exactly 4 Directional Buttons (No Joystick, No normal stop button)
        Card(
            colors = CardDefaults.cardColors(containerColor = ManualSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ROBOT MOVEMENT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))

                // FORWARD (▲)
                Button(
                    onClick = { onMove("FORWARD") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .width(130.dp)
                        .height(52.dp)
                        .testTag("move_forward_button")
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("FORWARD", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // LEFT (◀) and RIGHT (▶)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onMove("LEFT") },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .width(120.dp)
                            .height(52.dp)
                            .testTag("move_left_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LEFT", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.width(28.dp))

                    Button(
                        onClick = { onMove("RIGHT") },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .width(120.dp)
                            .height(52.dp)
                            .testTag("move_right_button")
                    ) {
                        Text("RIGHT", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // REVERSE (▼)
                Button(
                    onClick = { onMove("REVERSE") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .width(130.dp)
                        .height(52.dp)
                        .testTag("move_reverse_button")
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("REVERSE", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Speed Control (SLOW, NORMAL, FAST)
        Card(
            colors = CardDefaults.cardColors(containerColor = ManualSurface),
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SPEED SELECTION (Current: $currentSpeed)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("SLOW", "NORMAL", "FAST").forEach { speedOption ->
                        val isSelected = currentSpeed.equals(speedOption, ignoreCase = true)
                        Button(
                            onClick = { onSpeed(speedOption) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("speed_$speedOption")
                        ) {
                            Text(
                                text = speedOption,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Camera Pan & Tilt (Servo 1 and Servo 2)
        Card(
            colors = CardDefaults.cardColors(containerColor = ManualSurface),
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
                        imageVector = Icons.Default.Camera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CAMERA GIMBAL (Servos 1 & 2)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "PAN (Servo 1)",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("LEFT", "CENTER", "RIGHT").forEach { pos ->
                        OutlinedButton(
                            onClick = { onCameraPan(pos) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("cam_pan_$pos")
                        ) {
                            Text(pos, fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "TILT (Servo 2)",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("UP", "CENTER", "DOWN").forEach { pos ->
                        OutlinedButton(
                            onClick = { onCameraTilt(pos) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("cam_tilt_$pos")
                        ) {
                            Text(pos, fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Buzzer Control Card
        Card(
            colors = CardDefaults.cardColors(containerColor = ManualSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = if (buzzerOn) StatusYellow else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AUDIO BUZZER",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = if (buzzerOn) "State: ACTIVE (Audible)" else "State: SILENT",
                            fontSize = 11.sp,
                            color = if (buzzerOn) StatusYellow else TextSecondary
                        )
                    }
                }

                Button(
                    onClick = onBuzzerToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (buzzerOn) StatusYellow else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("buzzer_toggle_button")
                ) {
                    Text(
                        text = if (buzzerOn) "TURN OFF" else "TURN ON",
                        fontWeight = FontWeight.Bold,
                        color = if (buzzerOn) Color.Black else TextPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
