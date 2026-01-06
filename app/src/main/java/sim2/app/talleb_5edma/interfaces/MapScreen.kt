package sim2.app.talleb_5edma.interfaces

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

/* ------------------ Demo pins ------------------ */
data class OfferPin(
    val pos: LatLng,
    val price: String,     // "50dt", "380dt", ...
    val highlighted: Boolean = false
)

private val demoPins = listOf(
    OfferPin(LatLng(36.8065, 10.1815), "380dt", highlighted = true), // Tunis
    OfferPin(LatLng(36.8080, 10.1770), "100dt"),
    OfferPin(LatLng(36.8050, 10.1850), "299dt"),
    OfferPin(LatLng(36.8045, 10.1750), "60dt"),
    OfferPin(LatLng(36.8090, 10.1835), "300dt"),
    OfferPin(LatLng(36.8075, 10.1885), "30dt"),
)

/* ------------------ Map Screen ------------------ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit
) {
    val ctx = LocalContext.current

    // Permission localisation
    var hasLocation by remember { mutableStateOf(false) }
    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasLocation = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }
    LaunchedEffect(Unit) {
        locationPermission.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val start = LatLng(36.8065, 10.1815) // centre Tunis
    val camState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(start, 14f)
    }

    // Préparer les icônes (bulles prix)
    val priceIcon: (String, Boolean) -> BitmapDescriptor = remember {
        { text, highlighted ->
            makePriceBubble(
                text = text,
                bg = if (highlighted) Color(0xFF222222) else Color.White,
                textColor = if (highlighted) Color.White else Color(0xFF111111),
                strokeColor = if (highlighted) Color(0x33222222) else Color(0x33000000)
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Map") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { pad ->
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad),
            cameraPositionState = camState,
            properties = MapProperties(isMyLocationEnabled = hasLocation),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = hasLocation,
                zoomControlsEnabled = false,
                compassEnabled = true,
                mapToolbarEnabled = false
            )
        ) {
            demoPins.forEach { pin ->
                Marker(
                    state = MarkerState(pin.pos),
                    title = pin.price,
                    icon = priceIcon(pin.price, pin.highlighted),
                    onClick = {
                        // centrer / zoomer sur la pin
                        // camState.move(CameraUpdateFactory.newLatLng(it.position)) // si tu veux
                        false
                    }
                )
            }
        }
    }
}

/* --------- Util: fabriquer un BitmapDescriptor "bulle prix" --------- */
private fun makePriceBubble(
    text: String,
    bg: Color,
    textColor: Color,
    strokeColor: Color
): BitmapDescriptor {
    val scale = 3 // densité pour que ce soit net
    val padding = 10f * scale
    val radius = 14f * scale
    val textSize = 14f * scale

    val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor.toArgb()
        this.textSize = textSize
    }
    val textWidth = paintText.measureText(text)
    val textHeight = paintText.fontMetrics.run { bottom - top }

    val w = (textWidth + 2 * padding).toInt()
    val h = (textHeight + 2 * padding).toInt()

    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)

    val rect = RectF(0f, 0f, w.toFloat(), h.toFloat())
    val paintBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bg.toArgb() }
    val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = strokeColor.toArgb()
    }

    c.drawRoundRect(rect, radius, radius, paintBg)
    c.drawRoundRect(rect, radius, radius, paintStroke)

    val x = padding
    val y = padding - paintText.fontMetrics.top
    c.drawText(text, x, y, paintText)

    return BitmapDescriptorFactory.fromBitmap(bmp)
}
