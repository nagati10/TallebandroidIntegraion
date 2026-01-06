package sim2.app.talleb_5edma.network

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.graphics.YuvImage
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.Image
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.util.Base64
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import org.webrtc.Camera2Enumerator
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

class MediaStreamManager(private val context: Context) {

    // Camera2 components
    private var cameraManager: android.hardware.camera2.CameraManager? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null

    // Background threads
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    // Camera state
    private var isFrontCamera = true
    private var isStreaming = false
    private var currentCameraId: String? = null

    // Audio
    private var audioRecord: AudioRecord? = null
    private var isRecordingAudio = false

    // Voice Activity Detection (VAD)
    private var isSpeaking = false
    private var silenceFrames = 0
    private var silenceThreshold = 5
    private var voiceThreshold = 10.0
    private var audioLevel = 0.0

    // Stream configuration - Reduced resolution for better performance
    private val streamWidth = 480
    private val streamHeight = 360

    // Performance optimization
    private var lastFrameTime = 0L
    private val minFrameInterval = 100 // ms = ~10 FPS

    // Rotation correction
    private var rotationCorrection = 90

    // Local preview
    private var previewSurface: Surface? = null
    private var isPreviewEnabled = false

    // Callbacks
    var onVideoFrame: ((String) -> Unit)? = null
    var onAudioData: ((String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onCameraStateChanged: ((String) -> Unit)? = null
    var onSpeakingStateChanged: ((Boolean) -> Unit)? = null

    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            onCameraStateChanged?.invoke("Camera opened - creating session")
            createCameraPreviewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice?.close()
            cameraDevice = null
            isStreaming = false
            onCameraStateChanged?.invoke("Camera disconnected")
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice?.close()
            cameraDevice = null
            isStreaming = false
            onError?.invoke("Camera error: $error")
        }
    }

    private val sessionStateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            cameraCaptureSession = session
            try {
                val captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                captureRequestBuilder?.addTarget(imageReader!!.surface)

                // Add preview surface if enabled
                if (isPreviewEnabled && previewSurface != null) {
                    captureRequestBuilder?.addTarget(previewSurface!!)
                }

                captureRequestBuilder?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO)
                captureRequestBuilder?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                captureRequestBuilder?.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)

                captureRequestBuilder?.build()?.let { captureRequest ->
                    session.setRepeatingRequest(captureRequest, null, backgroundHandler)
                    isStreaming = true
                    onCameraStateChanged?.invoke("✅ Camera streaming started (Rotation: ${rotationCorrection}°)")
                }
            } catch (e: CameraAccessException) {
                onError?.invoke("Failed to start camera preview: ${e.message}")
            } catch (e: Exception) {
                onError?.invoke("Camera session error: ${e.message}")
            }
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            onError?.invoke("Camera session configuration failed")
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            Log.e("MediaStreamManager", "Error stopping background thread", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun startVideoStream() {
        if (isStreaming) return
        if (!hasCameraPermission()) {
            onError?.invoke("Camera permission required")
            return
        }

        try {
            startBackgroundThread()
            cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager

            // Use the improved camera detection
            val cameraId = getCameraIdWithFallback(isFrontCamera)
            if (cameraId == null) {
                onError?.invoke("No camera found")
                return
            }

            currentCameraId = cameraId

            // Auto-detect optimal rotation
            determineOptimalRotation(cameraId)

            onCameraStateChanged?.invoke("Opening camera: $cameraId (Rotation: ${rotationCorrection}°)")

            // Setup ImageReader for capturing frames with proper configuration
            imageReader = ImageReader.newInstance(streamWidth, streamHeight, ImageFormat.YUV_420_888, 2) // Reduced buffer
            imageReader?.setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                image?.let { processCamera2Image(it) }
            }, backgroundHandler)

            cameraManager?.openCamera(cameraId, cameraStateCallback, backgroundHandler)

        } catch (e: Exception) {
            onError?.invoke("Failed to start video: ${e.message}")
            Log.e("MediaStreamManager", "Video start error", e)
        }
    }

    /**
     * Enable local camera preview for self-view
     */
    fun enableLocalPreview(surface: Surface) {
        previewSurface = surface
        isPreviewEnabled = true

        // If we're already streaming, update the session to include preview
        if (isStreaming) {
            updatePreviewSession()
        }

        log("📱 Local preview enabled")
    }

    /**
     * Disable local camera preview
     */
    fun disableLocalPreview() {
        isPreviewEnabled = false
        previewSurface = null
        log("📱 Local preview disabled")
    }

    /**
     * Update camera session to include preview surface
     */
    private fun updatePreviewSession() {
        try {
            cameraCaptureSession?.close()
            cameraCaptureSession = null
            createCameraPreviewSession()
        } catch (e: Exception) {
            logError("Failed to update preview session: ${e.message}")
        }
    }

    /**
     * Modified createCameraPreviewSession to support both preview and capture
     */
    private fun createCameraPreviewSession() {
        try {
            val targets = mutableListOf<Surface>()

            // Always add image reader for frame capture
            targets.add(imageReader!!.surface)

            // Add preview surface if enabled
            if (isPreviewEnabled && previewSurface != null) {
                targets.add(previewSurface!!)
                log("📱 Creating session with preview surface")
            } else {
                log("📱 Creating session without preview surface")
            }

            cameraDevice?.createCaptureSession(targets, sessionStateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            onError?.invoke("Failed to create camera session: ${e.message}")
        } catch (e: Exception) {
            onError?.invoke("Camera session creation error: ${e.message}")
        }
    }

    /**
     * Set rotation correction for testing different orientations
     * Common values: 0, 90, 180, 270
     */
    fun setRotationCorrection(degrees: Int) {
        rotationCorrection = degrees
        log("Rotation correction set to: $degrees°")
    }

    /**
     * Get current rotation correction
     */
    fun getRotationCorrection(): Int {
        return rotationCorrection
    }

    /**
     * Test different rotations - call this to cycle through options
     */
    fun testNextRotation() {
        val rotations = arrayOf(0, 90, 180, 270, -90, -180)
        val currentIndex = rotations.indexOf(rotationCorrection)
        val nextIndex = (currentIndex + 1) % rotations.size
        rotationCorrection = rotations[nextIndex]
        log("Testing rotation: ${rotationCorrection}°")

        // Restart stream if currently streaming to apply new rotation
        if (isStreaming) {
            stopVideoStream()
            backgroundHandler?.postDelayed({
                startVideoStream()
            }, 300)
        }
    }

    /**
     * Auto-detect optimal rotation
     */
    private fun determineOptimalRotation(cameraId: String) {
        try {
            val characteristics = cameraManager?.getCameraCharacteristics(cameraId)
            val sensorOrientation = characteristics?.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 90
            rotationCorrection = sensorOrientation
            log("Auto-detected rotation: $rotationCorrection° for camera $cameraId")
        } catch (e: Exception) {
            log("Using default rotation: 90°")
            rotationCorrection = 90
        }
    }

    /**
     * Improved camera detection using WebRTC's Camera2Enumerator logic
     */
    private fun getCameraIdWithFallback(preferFront: Boolean): String? {
        return try {
            val cameraEnumerator = Camera2Enumerator(context)
            val cameraNames = cameraEnumerator.deviceNames

            log("Available cameras: ${cameraNames.joinToString()}")

            if (preferFront) {
                // Try front camera first
                val frontCamera = cameraNames.find { cameraEnumerator.isFrontFacing(it) }
                if (frontCamera != null) {
                    log("Using front camera: $frontCamera")
                    return frontCamera
                }
            } else {
                // Try back camera
                val backCamera = cameraNames.find { cameraEnumerator.isBackFacing(it) }
                if (backCamera != null) {
                    log("Using back camera: $backCamera")
                    return backCamera
                }
            }

            // Fallback: use any available camera
            if (cameraNames.isNotEmpty()) {
                log("Using available camera: ${cameraNames[0]}")
                return cameraNames[0]
            }

            logError("No cameras available")
            null
        } catch (e: Exception) {
            logError("Error creating camera capturer: ${e.message}")
            // Fallback to original method
            getCameraId(preferFront)
        }
    }

    /**
     * Original camera detection method (kept as fallback)
     */
    private fun getCameraId(useFront: Boolean): String? {
        return try {
            cameraManager?.let { manager ->
                manager.cameraIdList.find { id ->
                    val characteristics = manager.getCameraCharacteristics(id)
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    if (useFront) {
                        facing == CameraCharacteristics.LENS_FACING_FRONT
                    } else {
                        facing == CameraCharacteristics.LENS_FACING_BACK
                    }
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Optimized image processing with frame rate control
     */
    private fun processCamera2Image(image: Image) {
        if (!isStreaming) {
            image.close()
            return
        }

        // Frame rate limiting
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastFrameTime < minFrameInterval) {
            image.close()
            return
        }
        lastFrameTime = currentTime

        try {
            // Convert YUV_420_888 to NV21 properly
            val nv21Data = convertYUV420ToNV21(image)

            // Create YuvImage and compress to JPEG
            val yuvImage = YuvImage(nv21Data, ImageFormat.NV21, image.width, image.height, null)
            val out = ByteArrayOutputStream()

            // Use reduced quality to improve performance
            val rect = Rect(0, 0, image.width, image.height)
            yuvImage.compressToJpeg(rect, 60, out) // Reduced from 80 to 60

            var imageBytes = out.toByteArray()

            // Apply rotation correction if needed
            if (rotationCorrection != 0) {
                imageBytes = rotateJPEGOptimized(imageBytes, rotationCorrection)
            }

            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            onVideoFrame?.invoke(base64Image)
            out.close()
        } catch (e: Exception) {
            Log.e("MediaStreamManager", "Frame processing error", e)
        } finally {
            image.close()
        }
    }

    /**
     * Optimized JPEG rotation
     */
    private fun rotateJPEGOptimized(jpegData: ByteArray, degrees: Int): ByteArray {
        if (degrees == 0) return jpegData

        return try {
            val options = BitmapFactory.Options().apply {
                inSampleSize = 2 // Downsample during decode
                inPreferredConfig = Bitmap.Config.RGB_565 // Use less memory
            }

            val originalBitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size, options)
            val matrix = Matrix()
            matrix.postRotate(degrees.toFloat())

            val rotatedBitmap = Bitmap.createBitmap(
                originalBitmap, 0, 0,
                originalBitmap.width, originalBitmap.height,
                matrix, true
            )

            val out = ByteArrayOutputStream()
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, out) // Reduced quality
            val rotatedData = out.toByteArray()

            // Recycle bitmaps immediately to free memory
            originalBitmap.recycle()
            rotatedBitmap.recycle()
            out.close()

            rotatedData
        } catch (e: Exception) {
            Log.e("MediaStreamManager", "Error rotating image", e)
            jpegData // Return original if rotation fails
        }
    }

    /**
     * Proper YUV to NV21 conversion that handles orientation and color issues
     */
    private fun convertYUV420ToNV21(image: Image): ByteArray {
        val planes = image.planes
        val width = image.width
        val height = image.height
        val nv21 = ByteArray(width * height * 3 / 2)

        // Get Y plane
        val yBuffer = planes[0].buffer
        val yRowStride = planes[0].rowStride
        val yPixelStride = planes[0].pixelStride

        // Get U and V planes
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer
        val uRowStride = planes[1].rowStride
        val vRowStride = planes[2].rowStride
        val uPixelStride = planes[1].pixelStride
        val vPixelStride = planes[2].pixelStride

        // Copy Y plane
        var yIndex = 0
        for (row in 0 until height) {
            for (col in 0 until width) {
                val yOffset = row * yRowStride + col * yPixelStride
                nv21[yIndex++] = yBuffer.get(yOffset)
            }
        }

        // Copy UV planes (interleaved as VU for NV21)
        var uvIndex = width * height
        for (row in 0 until height / 2) {
            for (col in 0 until width / 2) {
                val uOffset = row * uRowStride + col * uPixelStride
                val vOffset = row * vRowStride + col * vPixelStride

                // NV21 format: V then U
                nv21[uvIndex++] = vBuffer.get(vOffset)
                nv21[uvIndex++] = uBuffer.get(uOffset)
            }
        }

        return nv21
    }

    @SuppressLint("MissingPermission")
    fun switchCamera() {
        if (!isStreaming) return

        try {
            log("🔄 Starting camera switch...")

            // Store current streaming state
            val wasStreaming = isStreaming

            // Close current session but don't change streaming state
            cameraCaptureSession?.close()
            cameraCaptureSession = null
            cameraDevice?.close()
            cameraDevice = null

            isFrontCamera = !isFrontCamera

            // Get new camera ID
            val newCameraId = getCameraIdWithFallback(isFrontCamera)
            if (newCameraId == null) {
                onError?.invoke("No camera found for switching")
                // Try to revert to original camera
                isFrontCamera = !isFrontCamera
                restartCamera()
                return
            }

            currentCameraId = newCameraId

            // Auto-detect optimal rotation for new camera
            determineOptimalRotation(newCameraId)

            log("📸 Switching to ${if (isFrontCamera) "front" else "rear"} camera: $newCameraId")

            // Reopen camera immediately - no delay needed
            cameraManager?.openCamera(newCameraId, cameraStateCallback, backgroundHandler)

            // Keep streaming state true during switch
            isStreaming = wasStreaming

        } catch (e: Exception) {
            logError("Camera switch failed: ${e.message}")
            // Try to restart with original camera on failure
            isFrontCamera = !isFrontCamera
            restartCamera()
        }
    }

    /**
     * Restart camera without changing the streaming state
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    private fun restartCamera() {
        try {
            val cameraId = getCameraIdWithFallback(isFrontCamera)
            if (cameraId != null) {
                cameraManager?.openCamera(cameraId, cameraStateCallback, backgroundHandler)
            }
        } catch (e: Exception) {
            logError("Camera restart failed: ${e.message}")
            isStreaming = false
        }
    }

    fun stopVideoStream() {
        try {
            isStreaming = false
            cameraCaptureSession?.close()
            cameraCaptureSession = null

            cameraDevice?.close()
            cameraDevice = null

            imageReader?.close()
            imageReader = null

            // Disable local preview
            disableLocalPreview()

            stopBackgroundThread()

            onCameraStateChanged?.invoke("Video stream stopped")
        } catch (e: Exception) {
            Log.e("MediaStreamManager", "Error stopping video stream", e)
        }
    }

    // Audio methods
    @SuppressLint("MissingPermission")
    fun startAudioStream() {
        if (isRecordingAudio) return
        if (!hasAudioPermission()) {
            onError?.invoke("Microphone permission required")
            return
        }

        try {
            val sampleRate = 16000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT

            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            if (minBufferSize <= 0) {
                onError?.invoke("Audio buffer config failed")
                return
            }

            // Use a larger buffer to prevent audio glitches
            val bufferSize = minBufferSize * 4

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION, // Better for voice calls
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                onError?.invoke("AudioRecord init failed")
                return
            }

            audioRecord?.startRecording()
            isRecordingAudio = true

            // Reset VAD logic
            isSpeaking = false
            silenceFrames = 0

            Thread {
                val buffer = ShortArray(bufferSize / 2) // Use short array for 16-bit audio

                while (isRecordingAudio) {
                    val shortsRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                    if (shortsRead > 0) {
                        val voiceDetected = detectVoiceFromShorts(buffer, shortsRead)
                        if (voiceDetected) {
                            processAudioFrame(buffer, shortsRead)
                        }
                    }

                    try {
                        Thread.sleep(1) // Reduced sleep for better real-time audio
                    } catch (_: InterruptedException) {
                        break
                    }
                }
                Log.d("MediaStreamManager", "Audio thread stopped")
            }.apply {
                name = "AudioRecorderThread"
                start()
            }

            onCameraStateChanged?.invoke("Audio stream started (VAD Active)")

        } catch (e: Exception) {
            onError?.invoke("Audio error: ${e.message}")
            Log.e("MediaStreamManager", "Audio start error", e)
        }
    }

    private fun detectVoiceFromShorts(buffer: ShortArray, length: Int): Boolean {
        var sum = 0.0

        for (i in 0 until length) {
            val sample = buffer[i].toDouble()
            sum += sample * sample
        }

        val rms = if (length > 0) sqrt(sum / length) else 0.0
        audioLevel = rms

        val wasSpeaking = isSpeaking

        if (rms > voiceThreshold) {
            isSpeaking = true
            silenceFrames = 0
        } else {
            if (isSpeaking) {
                silenceFrames++
                if (silenceFrames > silenceThreshold) {
                    isSpeaking = false
                }
            }
        }

        if (wasSpeaking != isSpeaking) {
            onSpeakingStateChanged?.invoke(isSpeaking)
            Log.d("VAD", "State changed: Sending Audio = $isSpeaking (RMS: $rms)")
        }

        return isSpeaking
    }

    private fun processAudioFrame(data: ShortArray, length: Int) {
        try {
            // Convert short array to byte array using ByteBuffer (more reliable)
            val byteBuffer = ByteBuffer.allocate(length * 2)
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN) // Android uses little-endian
            val shortBuffer = byteBuffer.asShortBuffer()
            shortBuffer.put(data, 0, length)

            val base64Audio = Base64.encodeToString(byteBuffer.array(), Base64.NO_WRAP)
            onAudioData?.invoke(base64Audio)
        } catch (e: Exception) {
            Log.e("MediaStreamManager", "Audio processing error", e)
        }
    }

    fun stopAudioStream() {
        if (!isRecordingAudio) return
        isRecordingAudio = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e("MediaStreamManager", "Error stopping audio", e)
        }
        audioRecord = null
        onSpeakingStateChanged?.invoke(false)
        onCameraStateChanged?.invoke("Audio stream stopped")
    }

    // Quality control method
    fun setStreamingQuality(width: Int, height: Int, quality: Int) {
        log("Setting streaming quality: ${width}x${height} @ $quality%")
        // Note: To apply these changes, you would need to restart the video stream
    }

    /**
     * Get optimal preview size
     */
    private fun getOptimalPreviewSize(): Size? {
        return try {
            val cameraId = currentCameraId ?: return null
            val characteristics = cameraManager?.getCameraCharacteristics(cameraId)
            val map = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val sizes = map?.getOutputSizes(SurfaceTexture::class.java)

            // Choose a reasonable preview size
            sizes?.firstOrNull { it.width <= 1280 && it.height <= 720 } ?: sizes?.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    // Permission check methods
    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    // Logging helpers
    private fun log(message: String) {
        Log.d("MediaStreamManager", message)
        onCameraStateChanged?.invoke(message)
    }

    private fun logError(message: String) {
        Log.e("MediaStreamManager", message)
        onError?.invoke(message)
    }

    // Getters
    fun isStreaming() = isStreaming
    fun isAudioStreaming() = isRecordingAudio
    fun getAudioLevel() = audioLevel
    fun isFrontCamera() = isFrontCamera

    fun setVoiceSensitivity(threshold: Double) {
        voiceThreshold = threshold
    }

    fun cleanup() {
        stopVideoStream()
        stopAudioStream()
    }
}