package sim2.app.talleb_5edma.interfaces

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import sim2.app.talleb_5edma.network.GeocodingService

private val Purple = Color(0xFF7C4DFF)
private val PurpleDark = Color(0xFF5E35B1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerMapScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val geocodingService = remember { GeocodingService() }
    
    var selectedPosition by remember { mutableStateOf<GeoPoint?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var selectedMarker by remember { mutableStateOf<Marker?>(null) }
    
    // Initialize osmdroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Sélectionner une position",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (selectedPosition != null && !isLoading) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val pos = selectedPosition ?: return@ExtendedFloatingActionButton
                        isLoading = true
                        errorMessage = null
                        
                        scope.launch {
                            try {
                                val location = geocodingService.reverseGeocode(
                                    pos.latitude,
                                    pos.longitude
                                )
                                
                                if (location != null) {
                                    // Pass data back via savedStateHandle
                                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                                        set("selected_address", location.address)
                                        set("selected_city", location.city)
                                        set("selected_country", location.country)
                                        set("selected_latitude", location.latitude.toString())
                                        set("selected_longitude", location.longitude.toString())
                                    }
                                    navController.popBackStack()
                                } else {
                                    errorMessage = "Impossible de récupérer l'adresse pour cette position"
                                    isLoading = false
                                }
                            } catch (e: Exception) {
                                errorMessage = "Erreur: ${e.message}"
                                isLoading = false
                            }
                        }
                    },
                    containerColor = Purple,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Check, contentDescription = "Enregistrer")
                    Spacer(Modifier.width(8.dp))
                    Text("Enregistrer", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Map View
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(12.0)
                        // Center on Tunisia by default
                        controller.setCenter(GeoPoint(36.8065, 10.1815))
                        
                        // Add map click listener
                        val mapEventsReceiver = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                if (p != null && !isLoading) {
                                    selectedPosition = p
                                    
                                    // Remove old marker if exists
                                    selectedMarker?.let { overlays.remove(it) }
                                    
                                    // Add new marker
                                    val marker = Marker(this@apply).apply {
                                        position = p
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        title = "Position sélectionnée"
                                        snippet = "Lat: ${String.format("%.4f", p.latitude)}, Lng: ${String.format("%.4f", p.longitude)}"
                                    }
                                    overlays.add(marker)
                                    selectedMarker = marker
                                    invalidate()
                                    
                                    return true
                                }
                                return false
                            }
                            
                            override fun longPressHelper(p: GeoPoint?): Boolean {
                                return false
                            }
                        }
                        
                        overlays.add(MapEventsOverlay(mapEventsReceiver))
                        mapView = this
                    }
                }
            )
            
            // Instructions overlay
            if (selectedPosition == null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "Cliquez sur la carte pour sélectionner une position",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Error message
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF4D4D)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { errorMessage = null }
                        ) {
                            Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            // Loading overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = Purple)
                            Text(
                                "Récupération de l'adresse...",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
