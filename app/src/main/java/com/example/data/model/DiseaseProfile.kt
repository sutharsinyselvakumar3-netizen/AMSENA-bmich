package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calibration_profiles")
data class CalibrationProfile(
    @PrimaryKey
    val id: String, // "NORMAL", "FUSARIUM", "PURPLE_BLOTCH", "DOWNY_MILDEW", "NECK_ROT"
    val name: String,
    val suspectedPattern: String,
    val description: String,
    val sampleCount: Int = 0,
    val minR: Int = 0,
    val maxR: Int = 255,
    val minG: Int = 0,
    val maxG: Int = 255,
    val minB: Int = 0,
    val maxB: Int = 255,
    val minC: Int = 0,
    val maxC: Int = 1000,
    val avgR: Int = 0,
    val avgG: Int = 0,
    val avgB: Int = 0,
    val avgC: Int = 0,
    val lastUpdated: Long = 0L,
    val isCalibrated: Boolean = false
) {
    /**
     * Checks if a sensor reading falls within calibrated tolerance envelope
     */
    fun matches(rgbc: RgbcData, tolerance: Int = 15): Boolean {
        if (!isCalibrated || sampleCount == 0) return false
        val rMatch = rgbc.red in (minR - tolerance)..(maxR + tolerance)
        val gMatch = rgbc.green in (minG - tolerance)..(maxG + tolerance)
        val bMatch = rgbc.blue in (minB - tolerance)..(maxB + tolerance)
        val cMatch = rgbc.clear in (minC - tolerance * 2)..(maxC + tolerance * 2)
        return rMatch && gMatch && bMatch && cMatch
    }

    companion object {
        val DEFAULT_PROFILES = listOf(
            CalibrationProfile(
                id = "NORMAL",
                name = "Healthy Onion Foliage",
                suspectedPattern = "Vibrant deep green foliage",
                description = "Baseline calibration for healthy onion leaves without chlorosis or necrosis."
            ),
            CalibrationProfile(
                id = "FUSARIUM",
                name = "Fusarium Basal Rot",
                suspectedPattern = "Yellow / brown abnormal pattern",
                description = "Suspected colour pattern associated with progressive yellowing and curving of leaves at base."
            ),
            CalibrationProfile(
                id = "PURPLE_BLOTCH",
                name = "Purple Blotch",
                suspectedPattern = "Purple / red / blue abnormal pattern",
                description = "Suspected colour pattern with sunken elliptical purple-brown lesions and red-violet halos."
            ),
            CalibrationProfile(
                id = "DOWNY_MILDEW",
                name = "Downy Mildew",
                suspectedPattern = "Pale green / abnormal mixed colour pattern",
                description = "Suspected colour pattern with pale yellowish chlorotic lesions and subtle violet-grey cast."
            ),
            CalibrationProfile(
                id = "NECK_ROT",
                name = "Neck Rot",
                suspectedPattern = "Brown / dark abnormal pattern",
                description = "Suspected colour pattern characterized by dark brown, water-soaked tissue softening."
            )
        )
    }
}
