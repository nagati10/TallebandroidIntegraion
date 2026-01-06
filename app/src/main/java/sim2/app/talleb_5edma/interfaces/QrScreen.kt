package sim2.app.talleb_5edma.interfaces

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGetImage::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun QrScreen(
    onBack: () -> Unit,
    onQrDetected: (String) -> Unit = {}
) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // CameraX controller
    val controller = remember {
        LifecycleCameraController(ctx).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
        }
    }

    // Camera permission
    var cameraGranted by remember { mutableStateOf(false) }
    val reqCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> cameraGranted = granted }

    LaunchedEffect(Unit) {
        reqCameraPermission.launch(android.Manifest.permission.CAMERA)
    }

    // ML Kit configuration
    val options = remember {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    }
    val scanner = remember { BarcodeScanning.getClient(options) }

    // Real-time analysis
    LaunchedEffect(cameraGranted) {
        if (cameraGranted) {
            controller.setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(ctx)
            ) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(
                        mediaImage, imageProxy.imageInfo.rotationDegrees
                    )
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            barcodes.firstOrNull()?.rawValue?.let { raw ->
                                if (raw.isNotBlank()) onQrDetected(raw)
                            }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                } else {
                    imageProxy.close()
                }
            }
        }
    }

    // Pick image from gallery
    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bmp: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(ctx.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(ctx.contentResolver, uri)
            }

            bmp?.let {
                val img = InputImage.fromBitmap(it, 0)
                scanner.process(img).addOnSuccessListener { codes ->
                    codes.firstOrNull()?.rawValue?.let { value ->
                        if (value.isNotBlank()) onQrDetected(value)
                    }
                }
            }
        }
    }

    Scaffold(


    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Camera preview
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                factory = { context ->
                    PreviewView(context).apply {
                        controller.bindToLifecycle(lifecycleOwner)
                        this.controller = controller
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                }
            )

            // Action buttons
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (!cameraGranted)
                            reqCameraPermission.launch(android.Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.QrCodeScanner, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Scanner")
                }
                OutlinedButton(
                    onClick = { pickImage.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Image, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Photos")
                }
            }
        }
    }
}
