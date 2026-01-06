package sim2.app.talleb_5edma.interfaces

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

// Fallback coordinates for major Tunisian cities
private val cityCoordinates = mapOf(
    "tunis" to GeoPoint(36.8065, 10.1815),
    "sfax" to GeoPoint(34.7406, 10.7603),
    "sousse" to GeoPoint(35.8256, 10.6369),
    "kairouan" to GeoPoint(35.6781, 10.0963),
    "bizerte" to GeoPoint(37.2746, 9.8739),
    "gabes" to GeoPoint(33.8815, 10.0982),
    "ariana" to GeoPoint(36.8625, 10.1956),
    "gafsa" to GeoPoint(34.4250, 8.7842),
    "monastir" to GeoPoint(35.7643, 10.8113),
    "ben arous" to GeoPoint(36.7539, 10.2286),
    "kasserine" to GeoPoint(35.1676, 8.8365),
    "medenine" to GeoPoint(33.3549, 10.5055),
    "nabeul" to GeoPoint(36.4561, 10.7376),
    "tataouine" to GeoPoint(32.9297, 10.4517),
    "beja" to GeoPoint(36.7256, 9.1817),
    "jendouba" to GeoPoint(36.5011, 8.7803),
    "mahdia" to GeoPoint(35.5047, 11.0622),
    "siliana" to GeoPoint(36.0847, 9.3700),
    "kef" to GeoPoint(36.1742, 8.7050),
    "tozeur" to GeoPoint(33.9197, 8.1335),
    "kebili" to GeoPoint(33.7047, 8.9697),
    "zaghouan" to GeoPoint(36.4028, 10.1425),
    "manouba" to GeoPoint(36.8080, 10.0965),
    "sidi bouzid" to GeoPoint(35.0381, 9.4858)
)

private fun getCityCoordinates(city: String?): GeoPoint? {
    if (city.isNullOrBlank()) return null
    val normalizedCity = city.trim().lowercase()
    return cityCoordinates[normalizedCity] ?: cityCoordinates.entries
        .firstOrNull { normalizedCity.contains(it.key) || it.key.contains(normalizedCity) }
        ?.value
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val repository = remember { sim2.app.talleb_5edma.network.OffreRepository() }
    var offers by remember { mutableStateOf<List<sim2.app.talleb_5edma.models.Offre>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Initialize osmdroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        try {
            val fetchedOffers = repository.getAllActiveOffers()
            println("MapScreen: Fetched ${fetchedOffers.size} total offers")
            
            // Display ALL offers - use coordinates if available, otherwise fallback to city
            offers = fetchedOffers
            
            println("MapScreen: Will display ${offers.size} offers on map")
        } catch (e: Exception) {
            println("MapScreen: Error fetching offers - ${e.message}")
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carte des Offres") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { pad ->
        Box(modifier = Modifier.padding(pad).fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    MapView(context).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(7.0) // Zoom out to see more of Tunisia
                        controller.setCenter(GeoPoint(36.8065, 10.1815)) // Center on Tunis default
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()
                    
                    // Group offers by location to handle overlapping markers
                    val offersByLocation = offers.groupBy { offer ->
                        // DEBUG: Log coordinates for each offer
                        println("MapScreen: Processing offer '${offer.title}' - lat=${offer.location?.coordinates?.lat}, lng=${offer.location?.coordinates?.lng}, city=${offer.location?.city}")
                        
                        val geoPoint = if (offer.location?.coordinates?.lat != null && 
                                          offer.location.coordinates.lng != null) {
                            println("MapScreen: Offer '${offer.title}' has GPS coordinates: (${offer.location.coordinates.lat}, ${offer.location.coordinates.lng})")
                            GeoPoint(offer.location.coordinates.lat, offer.location.coordinates.lng)
                        } else {
                            println("MapScreen: Offer '${offer.title}' using city fallback: ${offer.location?.city}")
                            getCityCoordinates(offer.location?.city)
                        }
                        // Round to avoid floating point issues
                        geoPoint?.let { "${(it.latitude * 1000).toInt()}_${(it.longitude * 1000).toInt()}" }
                    }
                    
                    var displayedCount = 0
                    var skippedCount = 0
                    
                    // Process each location group
                    offersByLocation.forEach { (locationKey, offersAtLocation) ->
                        if (locationKey == null) {
                            skippedCount += offersAtLocation.size
                            offersAtLocation.forEach { offer ->
                                println("MapScreen: Skipped offer '${offer.title}' - no coordinates or unknown city: ${offer.location?.city}")
                            }
                            return@forEach
                        }
                        
                        // Get the base coordinates for this group
                        val firstOffer = offersAtLocation.first()
                        val baseGeoPoint = if (firstOffer.location?.coordinates?.lat != null && 
                                              firstOffer.location.coordinates.lng != null) {
                            GeoPoint(firstOffer.location.coordinates.lat, firstOffer.location.coordinates.lng)
                        } else {
                            getCityCoordinates(firstOffer.location?.city)
                        }
                        
                        if (baseGeoPoint == null) {
                            skippedCount += offersAtLocation.size
                            return@forEach
                        }
                        
                        // If multiple offers at same location, offset them in a circle
                        val offsetRadius = 0.005 // ~500m offset
                        offersAtLocation.forEachIndexed { index, offer ->
                            val geoPoint = if (offersAtLocation.size > 1) {
                                // Calculate circular offset
                                val angle = (2 * Math.PI * index) / offersAtLocation.size
                                val latOffset = offsetRadius * Math.cos(angle)
                                val lngOffset = offsetRadius * Math.sin(angle)
                                GeoPoint(
                                    baseGeoPoint.latitude + latOffset,
                                    baseGeoPoint.longitude + lngOffset
                                )
                            } else {
                                baseGeoPoint
                            }
                            
                            val marker = Marker(mapView)
                            marker.position = geoPoint
                            
                            // Display price on the marker bubble (or "N/A" if no price)
                            val displayText = offer.salary ?: "N/A"
                            
                            // Build title with reference and price for the popup
                            val markerInfo = buildString {
                                if (!offer.reference.isNullOrBlank()) {
                                    append("[${offer.reference}] ")
                                }
                                append(offer.title ?: "")
                                append("\n💰 $displayText")
                                append("\n📍 ${offer.location?.city ?: ""}")
                            }
                            marker.title = markerInfo
                            
                            // Create custom icon with price
                            try {
                                val iconBitmap = makePriceBubbleBitmap(
                                    text = displayText,
                                    bg = Color.White,
                                    textColor = Color.Black,
                                    strokeColor = Color.Gray
                                )
                                val iconDrawable = android.graphics.drawable.BitmapDrawable(ctx.resources, iconBitmap)
                                marker.icon = iconDrawable
                                
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            
                            // Add click listener to navigate to offer details
                            marker.setOnMarkerClickListener { clickedMarker, _ ->
                                offer.id?.let { offerId ->
                                    navController.navigate("${sim2.app.talleb_5edma.Routes.ScreenOffre}/$offerId")
                                }
                                true // Consume the event
                            }
                            
                            mapView.overlays.add(marker)
                            displayedCount++
                        }
                    }
                    
                    println("MapScreen: Displayed $displayedCount markers (from ${offers.size} offers), skipped $skippedCount")
                    mapView.invalidate() // Refresh map
                }
            )
            
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }
        }
    }
}

/* --------- Util: fabriquer un Bitmap "bulle prix" for OSM --------- */
private fun makePriceBubbleBitmap(
    text: String,
    bg: Color,
    textColor: Color,
    strokeColor: Color
): Bitmap {
    val scale = 3 // densité
    val padding = 10f * scale
    val radius = 14f * scale
    val textSize = 14f * scale

    val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor.toArgb()
        this.textSize = textSize
    }
    val textWidth = paintText.measureText(text)
    val textHeight = paintText.fontMetrics.run { bottom - top }
    // Ensure minimum size
    val w = Math.max((textWidth + 2 * padding).toInt(), (40 * scale))
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

    val x = (w - textWidth) / 2
    val y = (h - textHeight) / 2 - paintText.fontMetrics.top
    c.drawText(text, x, y, paintText)

    return bmp
}

