package com.example.data.remote

import com.example.data.model.ApiResponse
import com.example.data.model.BuzzerRequest
import com.example.data.model.EmergencyStopRequest
import com.example.data.model.ModeRequest
import com.example.data.model.RobotMoveRequest
import com.example.data.model.RobotStatus
import com.example.data.model.ServoRequest
import com.example.data.model.SpeedRequest
import com.example.data.model.WeedDetectionResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class Esp32Client {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.NONE
    }

    private var currentBaseUrl = ""
    private var cachedService: Esp32ApiService? = null

    private fun getService(ip: String, timeoutMs: Long = 3000L): Esp32ApiService {
        val cleanIp = ip.trim().removePrefix("http://").removePrefix("https://").trimEnd('/')
        val formattedUrl = "http://$cleanIp/"

        if (cachedService != null && currentBaseUrl == formattedUrl) {
            return cachedService!!
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .addInterceptor(logging)
            .retryOnConnectionFailure(false)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(formattedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val service = retrofit.create(Esp32ApiService::class.java)
        cachedService = service
        currentBaseUrl = formattedUrl
        return service
    }

    suspend fun fetchStatus(ip: String, timeoutMs: Long = 3000L): Result<RobotStatus> {
        return runCatching {
            val response = getService(ip, timeoutMs).getStatus()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception("ESP32 HTTP ${response.code()}: ${response.message()}")
            }
        }
    }

    suspend fun setMode(ip: String, mode: String): Result<ApiResponse> {
        return runCatching {
            val res = getService(ip).setMode(ModeRequest(mode))
            if (res.isSuccessful && res.body() != null) res.body()!! else throw Exception("HTTP ${res.code()}")
        }
    }

    suspend fun moveRobot(ip: String, direction: String, speed: String? = null): Result<ApiResponse> {
        return runCatching {
            val res = getService(ip).moveRobot(RobotMoveRequest(direction, speed))
            if (res.isSuccessful && res.body() != null) res.body()!! else throw Exception("HTTP ${res.code()}")
        }
    }

    suspend fun setSpeed(ip: String, speed: String): Result<ApiResponse> {
        return runCatching {
            val res = getService(ip).setSpeed(SpeedRequest(speed))
            if (res.isSuccessful && res.body() != null) res.body()!! else throw Exception("HTTP ${res.code()}")
        }
    }

    suspend fun setServo(ip: String, servoId: Int, position: String, angle: Int? = null): Result<ApiResponse> {
        return runCatching {
            val res = getService(ip).setServo(ServoRequest(servoId, position, angle))
            if (res.isSuccessful && res.body() != null) res.body()!! else throw Exception("HTTP ${res.code()}")
        }
    }

    suspend fun setBuzzer(ip: String, state: Boolean): Result<ApiResponse> {
        return runCatching {
            val res = getService(ip).setBuzzer(BuzzerRequest(state))
            if (res.isSuccessful && res.body() != null) res.body()!! else throw Exception("HTTP ${res.code()}")
        }
    }

    suspend fun startAuto(ip: String): Result<ApiResponse> {
        return runCatching {
            val res = getService(ip).startAuto()
            if (res.isSuccessful && res.body() != null) res.body()!! else throw Exception("HTTP ${res.code()}")
        }
    }

    suspend fun stopAuto(ip: String): Result<ApiResponse> {
        return runCatching {
            val res = getService(ip).stopAuto()
            if (res.isSuccessful && res.body() != null) res.body()!! else throw Exception("HTTP ${res.code()}")
        }
    }

    suspend fun triggerEmergencyStop(ip: String, isEmergency: Boolean = true): Result<ApiResponse> {
        return runCatching {
            val res = getService(ip).triggerEmergencyStop(EmergencyStopRequest(isEmergency))
            if (res.isSuccessful && res.body() != null) res.body()!! else throw Exception("HTTP ${res.code()}")
        }
    }

    suspend fun reportDetection(ip: String, detection: WeedDetectionResult): Result<ApiResponse> {
        return runCatching {
            val res = getService(ip).reportDetection(detection)
            if (res.isSuccessful && res.body() != null) res.body()!! else throw Exception("HTTP ${res.code()}")
        }
    }
}
