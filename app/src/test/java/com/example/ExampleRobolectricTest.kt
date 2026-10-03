package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CalibrationProfile
import com.example.data.model.HardwarePinConfig
import com.example.data.model.RgbcData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext_matchesAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AI Companion", appName)
    }

    @Test
    fun verifyHardwareGpioPinCount() {
        // Must contain all 24 mapped pins
        assertEquals(24, HardwarePinConfig.PINS.size)
        // Verify key pins
        assertTrue(HardwarePinConfig.PINS.any { it.gpio == 39 && it.component.contains("Emergency") })
        assertTrue(HardwarePinConfig.PINS.any { it.gpio == 21 && it.component.contains("TCS34725") })
        assertTrue(HardwarePinConfig.PINS.any { it.gpio == 19 && it.component.contains("Soil Sensor") })
        assertTrue(HardwarePinConfig.PINS.any { it.gpio == 2 && it.component.contains("Cutter") })
    }

    @Test
    fun testDiseaseProfileMatching() {
        val fusarium = CalibrationProfile(
            id = "FUSARIUM",
            name = "Fusarium Basal Rot",
            suspectedPattern = "Yellow / brown abnormal pattern",
            description = "Test profile",
            sampleCount = 3,
            minR = 120,
            maxR = 160,
            minG = 80,
            maxG = 110,
            minB = 40,
            maxB = 70,
            minC = 200,
            maxC = 300,
            isCalibrated = true
        )

        val matchingSample = RgbcData(red = 135, green = 95, blue = 55, clear = 250)
        assertTrue(fusarium.matches(matchingSample))

        val outsideSample = RgbcData(red = 20, green = 200, blue = 20, clear = 400)
        assertFalse(fusarium.matches(outsideSample))
    }
}
