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
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface Esp32ApiService {

    @GET("api/status")
    suspend fun getStatus(): Response<RobotStatus>

    @GET("api/sensors")
    suspend fun getSensors(): Response<RobotStatus>

    @POST("api/mode")
    suspend fun setMode(@Body request: ModeRequest): Response<ApiResponse>

    @POST("api/robot")
    suspend fun moveRobot(@Body request: RobotMoveRequest): Response<ApiResponse>

    @POST("api/speed")
    suspend fun setSpeed(@Body request: SpeedRequest): Response<ApiResponse>

    @POST("api/servo")
    suspend fun setServo(@Body request: ServoRequest): Response<ApiResponse>

    @POST("api/buzzer")
    suspend fun setBuzzer(@Body request: BuzzerRequest): Response<ApiResponse>

    @POST("api/auto/start")
    suspend fun startAuto(): Response<ApiResponse>

    @POST("api/auto/stop")
    suspend fun stopAuto(): Response<ApiResponse>

    @POST("api/emergency_stop")
    suspend fun triggerEmergencyStop(@Body request: EmergencyStopRequest): Response<ApiResponse>

    @POST("api/detection")
    suspend fun reportDetection(@Body request: WeedDetectionResult): Response<ApiResponse>
}
