package com.example.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

sealed class CameraStreamState {
    object Idle : CameraStreamState()
    object Connecting : CameraStreamState()
    data class Streaming(val frame: Bitmap, val fps: Int = 0) : CameraStreamState()
    data class Error(
        val title: String = "STREAM IP FAILED",
        val message: String = "STREAM NOT AVAILABLE",
        val details: String = ""
    ) : CameraStreamState()
}

class MjpegStreamReader {

    private val client = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .retryOnConnectionFailure(false)
        .build()

    private val _streamState = MutableStateFlow<CameraStreamState>(CameraStreamState.Idle)
    val streamState: StateFlow<CameraStreamState> = _streamState.asStateFlow()

    private var streamJob: Job? = null
    private var lastStreamUrl: String = ""

    fun startStream(scope: CoroutineScope, streamUrl: String, captureFallbackUrl: String? = null) {
        if (streamJob?.isActive == true && lastStreamUrl == streamUrl && _streamState.value is CameraStreamState.Streaming) {
            return
        }
        stopStream()
        lastStreamUrl = streamUrl

        streamJob = scope.launch(Dispatchers.IO) {
            _streamState.value = CameraStreamState.Connecting
            val success = tryMjpegStream(streamUrl)
            if (!success && isActive && !captureFallbackUrl.isNullOrBlank()) {
                // If stream port failed, try periodic single snapshot capture as fallback
                tryPeriodicCapture(captureFallbackUrl)
            }
        }
    }

    private suspend fun tryMjpegStream(streamUrl: String): Boolean {
        var inputStream: BufferedInputStream? = null
        try {
            val request = Request.Builder()
                .url(streamUrl)
                .addHeader("Accept", "multipart/x-mixed-replace, image/jpeg, */*")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                _streamState.value = CameraStreamState.Error(
                    title = "STREAM IP FAILED",
                    message = "STREAM NOT AVAILABLE",
                    details = "HTTP ${response.code}"
                )
                response.close()
                return false
            }

            val body = response.body
            if (body == null) {
                _streamState.value = CameraStreamState.Error(
                    title = "STREAM IP FAILED",
                    message = "STREAM NOT AVAILABLE",
                    details = "Empty response body"
                )
                return false
            }

            inputStream = BufferedInputStream(body.byteStream())
            val buffer = ByteArray(4096)
            val byteStream = ByteArrayOutputStream()
            var prevByte = 0
            var inJpeg = false

            while (kotlin.coroutines.coroutineContext.isActive) {
                val byteRead = inputStream.read()
                if (byteRead == -1) break

                val currentByte = byteRead and 0xFF

                if (!inJpeg) {
                    if (prevByte == 0xFF && currentByte == 0xD8) { // JPEG SOI marker
                        inJpeg = true
                        byteStream.reset()
                        byteStream.write(0xFF)
                        byteStream.write(0xD8)
                    }
                } else {
                    byteStream.write(currentByte)
                    if (prevByte == 0xFF && currentByte == 0xD9) { // JPEG EOI marker
                        inJpeg = false
                        val jpegBytes = byteStream.toByteArray()
                        val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
                        if (bitmap != null) {
                            _streamState.value = CameraStreamState.Streaming(bitmap)
                        }
                    }
                }
                prevByte = currentByte
            }
            return true
        } catch (e: Exception) {
            _streamState.value = CameraStreamState.Error(
                title = "STREAM IP FAILED",
                message = "STREAM NOT AVAILABLE",
                details = e.localizedMessage ?: "Connection error"
            )
            return false
        } finally {
            try {
                inputStream?.close()
            } catch (_: Exception) {}
        }
    }

    private suspend fun tryPeriodicCapture(captureUrl: String) {
        while (kotlin.coroutines.coroutineContext.isActive) {
            try {
                val request = Request.Builder().url(captureUrl).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful && response.body != null) {
                    val bytes = response.body!!.bytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bitmap != null) {
                        _streamState.value = CameraStreamState.Streaming(bitmap)
                    }
                } else {
                    _streamState.value = CameraStreamState.Error(
                        title = "STREAM IP FAILED",
                        message = "STREAM NOT AVAILABLE",
                        details = "Capture HTTP ${response.code}"
                    )
                }
                response.close()
            } catch (e: Exception) {
                _streamState.value = CameraStreamState.Error(
                    title = "STREAM IP FAILED",
                    message = "STREAM NOT AVAILABLE",
                    details = e.localizedMessage ?: "Capture error"
                )
            }
            delay(1000)
        }
    }

    fun stopStream() {
        streamJob?.cancel()
        streamJob = null
        _streamState.value = CameraStreamState.Idle
    }
}
