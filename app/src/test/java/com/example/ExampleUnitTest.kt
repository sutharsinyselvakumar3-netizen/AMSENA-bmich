package com.example

import com.example.data.model.AutoState
import com.example.data.model.WeedDetectionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun verifyAutoStateFiniteStatesCount() {
        // Must contain all 16 finite states
        assertEquals(16, AutoState.values().size)
        assertNotNull(AutoState.valueOf("MANUAL"))
        assertNotNull(AutoState.valueOf("AUTO_SEARCHING"))
        assertNotNull(AutoState.valueOf("WEED_DETECTED"))
        assertNotNull(AutoState.valueOf("CUTTING"))
        assertNotNull(AutoState.valueOf("COLOUR_CHECK"))
        assertNotNull(AutoState.valueOf("SPRAYING"))
        assertNotNull(AutoState.valueOf("SOIL_CHECK"))
        assertNotNull(AutoState.valueOf("WATERING"))
        assertNotNull(AutoState.valueOf("EMERGENCY_STOP"))
        assertNotNull(AutoState.valueOf("LOW_BATTERY"))
    }

    @Test
    fun verifyWeedDetectionModel() {
        val detection = WeedDetectionResult(
            label = "WEED",
            confidence = 94,
            detectionCount = 3,
            x = 320,
            y = 185
        )
        assertEquals("WEED", detection.label)
        assertEquals(94, detection.confidence)
        assertEquals(3, detection.detectionCount)
        assertEquals(320, detection.x)
        assertEquals(185, detection.y)
    }
}
