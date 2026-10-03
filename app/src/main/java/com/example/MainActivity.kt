package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.model.AutoState
import com.example.ui.MainViewModel
import com.example.ui.auto.AutoScreen
import com.example.ui.calibration.ColourCalibrationScreen
import com.example.ui.components.AppBottomNavBar
import com.example.ui.home.HomeScreen
import com.example.ui.manual.ManualScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.status.SystemStatusScreen
import com.example.ui.theme.OnionRobotTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val autoState by viewModel.autoState.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()

            // Dynamic theme: Dark Blue in AUTO mode, Dark Green in MANUAL/other modes
            val isAutoTheme = autoState != AutoState.MANUAL && autoState != AutoState.EMERGENCY_STOP && autoState != AutoState.AUTO_STOPPED

            OnionRobotTheme(isAutoMode = isAutoTheme) {
                MainContent(
                    viewModel = viewModel,
                    currentScreen = currentScreen,
                    autoState = autoState
                )
            }
        }
    }
}

@Composable
fun MainContent(
    viewModel: MainViewModel,
    currentScreen: String,
    autoState: AutoState
) {
    val robotStatus by viewModel.robotStatus.collectAsState()
    val isEsp32Online by viewModel.isEsp32Online.collectAsState()
    val connectionError by viewModel.connectionError.collectAsState()
    val lastContact by viewModel.lastSuccessfulContact.collectAsState()
    val streamState by viewModel.streamState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val profiles by viewModel.calibrationProfiles.collectAsState()
    val selectedProfileId by viewModel.selectedCalibrationProfileId.collectAsState()
    val soilCountdown by viewModel.soilCountdownSeconds.collectAsState()
    val weedDetection by viewModel.currentWeedDetection.collectAsState()
    val suspectedDisease by viewModel.suspectedDisease.collectAsState()
    val showAutoStartDialog by viewModel.showAutoStartDialog.collectAsState()
    val showAutoStopDialog by viewModel.showAutoStopDialog.collectAsState()
    val safetyReport by viewModel.safetyCheckReport.collectAsState()

    // Handle back button for sub-screens
    if (currentScreen == "CALIBRATION") {
        BackHandler {
            viewModel.navigateTo("SETTINGS")
        }
    } else if (currentScreen != "HOME" && currentScreen != "SPLASH") {
        BackHandler {
            viewModel.navigateTo("HOME")
        }
    }

    if (currentScreen == "SPLASH") {
        SplashScreen(
            isEsp32Online = isEsp32Online,
            robotStatus = robotStatus,
            onProceed = { viewModel.navigateTo("HOME") }
        )
    } else {
        Scaffold(
            bottomBar = {
                if (currentScreen != "CALIBRATION") {
                    AppBottomNavBar(
                        currentScreen = currentScreen,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    "HOME" -> {
                        HomeScreen(
                            isEsp32Online = isEsp32Online,
                            robotStatus = robotStatus,
                            autoState = autoState,
                            connectionError = connectionError,
                            lastContactTime = lastContact,
                            onNavigate = { viewModel.navigateTo(it) },
                            onEmergencyStop = { viewModel.triggerEmergencyStop() }
                        )
                    }

                    "MANUAL" -> {
                        ManualScreen(
                            robotStatus = robotStatus,
                            autoState = autoState,
                            streamState = streamState,
                            onMove = { viewModel.sendMove(it) },
                            onSpeed = { viewModel.sendSpeed(it) },
                            onCameraPan = { viewModel.sendCameraPan(it) },
                            onCameraTilt = { viewModel.sendCameraTilt(it) },
                            onBuzzerToggle = { viewModel.toggleBuzzer() },
                            onRetryStream = { viewModel.retryCameraStream() },
                            onSettingsClick = { viewModel.navigateTo("SETTINGS") },
                            onEmergencyStop = { viewModel.triggerEmergencyStop() }
                        )
                    }

                    "AUTO" -> {
                        AutoScreen(
                            robotStatus = robotStatus,
                            autoState = autoState,
                            streamState = streamState,
                            soilCountdownSeconds = soilCountdown,
                            currentWeedDetection = weedDetection,
                            suspectedDisease = suspectedDisease,
                            showStartDialog = showAutoStartDialog,
                            showStopDialog = showAutoStopDialog,
                            safetyReport = safetyReport,
                            onRequestStartAuto = { viewModel.requestStartAuto() },
                            onConfirmStartAuto = { viewModel.confirmStartAuto() },
                            onDismissStartDialog = { viewModel.dismissAutoStartDialog() },
                            onRequestStopAuto = { viewModel.requestStopAuto() },
                            onConfirmStopAuto = { viewModel.confirmStopAuto() },
                            onDismissStopDialog = { viewModel.dismissAutoStopDialog() },
                            onRetryStream = { viewModel.retryCameraStream() },
                            onSettingsClick = { viewModel.navigateTo("SETTINGS") },
                            onEmergencyStop = { viewModel.triggerEmergencyStop() }
                        )
                    }

                    "STATUS" -> {
                        SystemStatusScreen(
                            isEsp32Online = isEsp32Online,
                            robotStatus = robotStatus,
                            autoState = autoState,
                            streamState = streamState,
                            logs = logs,
                            onClearLogs = { viewModel.clearLogs() },
                            onEmergencyStop = { viewModel.triggerEmergencyStop() }
                        )
                    }

                    "SETTINGS" -> {
                        SettingsScreen(
                            currentSettings = settings,
                            onSaveNetwork = { ssid, pass, esp32Ip, camIp, port ->
                                viewModel.updateNetworkSettings(ssid, pass, esp32Ip, camIp, port)
                            },
                            onSaveAi = { conf, consec ->
                                viewModel.updateAiSettings(conf, consec)
                            },
                            onSaveAgronomy = { soilDry, water, spray, battery ->
                                viewModel.updateAgronomySettings(soilDry, water, spray, battery)
                            },
                            onOpenCalibration = { viewModel.navigateTo("CALIBRATION") }
                        )
                    }

                    "CALIBRATION" -> {
                        ColourCalibrationScreen(
                            robotStatus = robotStatus,
                            profiles = profiles,
                            selectedProfileId = selectedProfileId,
                            onSelectProfile = { viewModel.selectCalibrationProfile(it) },
                            onSaveCurrentSample = { viewModel.saveCalibrationForCurrentSensor() },
                            onResetProfile = { viewModel.resetCalibration(it) },
                            onBack = { viewModel.navigateTo("SETTINGS") }
                        )
                    }
                }
            }
        }
    }
}
