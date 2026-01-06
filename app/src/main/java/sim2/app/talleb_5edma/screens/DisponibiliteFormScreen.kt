package sim2.app.talleb_5edma.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import sim2.app.talleb_5edma.models.CreateDisponibiliteRequest
import sim2.app.talleb_5edma.models.Disponibilite
import sim2.app.talleb_5edma.models.UpdateDisponibiliteRequest
import sim2.app.talleb_5edma.network.DisponibiliteRepository
import sim2.app.talleb_5edma.util.getToken
import java.text.SimpleDateFormat
import java.util.*

private val Primary = Color(0xFF0D9488)
private val BackgroundStart = Color(0xFFECFEFF)
private val BackgroundEnd = Color(0xFFE0F2FE)
private val Soft = Color(0xFF6B6B6B)

val joursSemaine = listOf(
    "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisponibiliteFormScreen(
    navController: NavController,
    disponibiliteId: String? = null,
    token: String? = null,
    jourParam: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val actualToken = token ?: getToken(context)
    val isEditMode = disponibiliteId != null
    
    var jour by remember { mutableStateOf(jourParam ?: "") }
    var heureDebut by remember { mutableStateOf("") }
    var heureFin by remember { mutableStateOf("") }
    var hasEndTime by remember { mutableStateOf(true) }
    
    var expandedJour by remember { mutableStateOf(false) }
    var showTimePickerDebut by remember { mutableStateOf(false) }
    var showTimePickerFin by remember { mutableStateOf(false) }
    
    val calendar = remember { Calendar.getInstance() }
    var selectedTimeDebut by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var selectedTimeFin by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingDisponibilite by remember { mutableStateOf(isEditMode) }
    
    val repository = remember { DisponibiliteRepository() }
    
    // Helper function to convert time from "h:mm a" to "HH:mm"
    fun convertTimeTo24Hour(timeStr: String): String {
        if (timeStr.isBlank()) {
            return timeStr
        }
        
        try {
            // Try parsing as 12-hour format with AM/PM
            val inputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val date = inputFormat.parse(timeStr)
            if (date != null) {
                val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                return outputFormat.format(date)
            }
        } catch (e: Exception) {
            println("CatLog: Failed to parse as 12h format: ${e.message}")
        }
        
        // Try parsing as 24-hour format (already in correct format)
        try {
            val format24 = SimpleDateFormat("HH:mm", Locale.getDefault())
            format24.parse(timeStr) // Just validate
            return timeStr
        } catch (e: Exception) {
            println("CatLog: Failed to parse as 24h format: ${e.message}")
        }
        
        return timeStr
    }
    
    // Load availability if editing
    LaunchedEffect(disponibiliteId) {
        if (isEditMode && actualToken.isNotEmpty() && disponibiliteId != null) {
            scope.launch {
                try {
                    val disponibilite = repository.getDisponibiliteById(actualToken, disponibiliteId)
                    jour = disponibilite.jour
                    
                    // Convert times from 24-hour to 12-hour format for display
                    try {
                        val timeFormat24 = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val timeFormat12 = SimpleDateFormat("h:mm a", Locale.getDefault())
                        val dateObj = timeFormat24.parse(disponibilite.heureDebut)
                        heureDebut = timeFormat12.format(dateObj ?: Date())
                    } catch (_: Exception) {
                        heureDebut = disponibilite.heureDebut
                    }
                    
                    if (disponibilite.heureFin.isNotEmpty()) {
                        try {
                            val timeFormat24 = SimpleDateFormat("HH:mm", Locale.getDefault())
                            val timeFormat12 = SimpleDateFormat("h:mm a", Locale.getDefault())
                            val dateObj = timeFormat24.parse(disponibilite.heureFin)
                            heureFin = timeFormat12.format(dateObj ?: Date())
                            hasEndTime = true
                        } catch (_: Exception) {
                            heureFin = disponibilite.heureFin
                            hasEndTime = true
                        }
                    } else {
                        hasEndTime = false
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Erreur: ${e.message}")
                } finally {
                    isLoadingDisponibilite = false
                }
            }
        }
    }
    
    fun saveDisponibilite() {
        // Refresh token from storage
        val refreshedToken = getToken(context)
        if (refreshedToken.isBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar("Erreur: Token d'authentification manquant. Veuillez vous reconnecter.")
            }
            return
        }
        
        if (jour.isBlank() || heureDebut.isBlank() || (hasEndTime && heureFin.isBlank())) {
            scope.launch {
                snackbarHostState.showSnackbar("Veuillez remplir tous les champs")
            }
            return
        }
        
        // Convert times to 24-hour format for API
        val heureDebut24 = convertTimeTo24Hour(heureDebut)
        val finalHeureFin = if (hasEndTime) convertTimeTo24Hour(heureFin) else ""
        
        scope.launch {
            isLoading = true
            try {
                if (isEditMode && disponibiliteId != null) {
                    val request = UpdateDisponibiliteRequest(
                        jour = jour,
                        heureDebut = heureDebut24,
                        heureFin = finalHeureFin
                    )
                    repository.updateDisponibilite(refreshedToken, disponibiliteId, request)
                    snackbarHostState.showSnackbar("Disponibilité modifiée avec succès")
                } else {
                    val request = CreateDisponibiliteRequest(
                        jour = jour,
                        heureDebut = heureDebut24,
                        heureFin = finalHeureFin
                    )
                    repository.createDisponibilite(refreshedToken, request)
                    snackbarHostState.showSnackbar("Disponibilité créée avec succès")
                }
                navController.popBackStack()
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Erreur inconnue"
                if (errorMessage.contains("401") || errorMessage.contains("Unauthorized")) {
                    snackbarHostState.showSnackbar("Erreur: Session expirée. Veuillez vous reconnecter.")
                } else {
                    snackbarHostState.showSnackbar("Erreur: $errorMessage")
                }
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isEditMode) "Modifier la disponibilité" else "Nouvelle indisponibilité",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Annuler", color = Color.Black)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { saveDisponibilite() },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.Black
                            )
                        } else {
                            Text(if (isEditMode) "Modifier" else "Créer", color = Color.Black)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8F8FB),
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { paddingValues ->
        // Time picker for heure début
        DisposableEffect(showTimePickerDebut) {
            if (showTimePickerDebut) {
                val currentHour = selectedTimeDebut?.first ?: calendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = selectedTimeDebut?.second ?: calendar.get(Calendar.MINUTE)
                
                val timePicker = TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        selectedTimeDebut = Pair(hourOfDay, minute)
                        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        heureDebut = timeFormat.format(calendar.time)
                        showTimePickerDebut = false
                    },
                    currentHour,
                    currentMinute,
                    false // 12-hour format with AM/PM
                )
                timePicker.show()
            }
            onDispose { }
        }
        
        // Time picker for heure fin
        DisposableEffect(showTimePickerFin) {
            if (showTimePickerFin) {
                val currentHour = selectedTimeFin?.first ?: calendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = selectedTimeFin?.second ?: calendar.get(Calendar.MINUTE)
                
                val timePicker = TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        selectedTimeFin = Pair(hourOfDay, minute)
                        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        heureFin = timeFormat.format(calendar.time)
                        showTimePickerFin = false
                    },
                    currentHour,
                    currentMinute,
                    false // 12-hour format with AM/PM
                )
                timePicker.show()
            }
            onDispose { }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8FB))
                .padding(paddingValues)
        ) {
            if (isLoadingDisponibilite) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Day dropdown
                    Column {
                        Text("Jour", fontSize = 14.sp, color = Soft, modifier = Modifier.padding(bottom = 8.dp))
                        ExposedDropdownMenuBox(
                            expanded = expandedJour,
                            onExpandedChange = { expandedJour = !expandedJour }
                        ) {
                            OutlinedTextField(
                                value = jour,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedJour) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedJour,
                                onDismissRequest = { expandedJour = false }
                            ) {
                                joursSemaine.forEach { jourOption ->
                                    DropdownMenuItem(
                                        text = { Text(jourOption) },
                                        onClick = {
                                            jour = jourOption
                                            expandedJour = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Horaires section
                    Column {
                        Text("Horaires", fontSize = 14.sp, color = Soft, modifier = Modifier.padding(bottom = 8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Heure début", fontSize = 14.sp)
                                    Box(
                                        modifier = Modifier
                                            .width(140.dp)
                                            .clickable { showTimePickerDebut = true }
                                    ) {
                                        OutlinedTextField(
                                            value = heureDebut,
                                            onValueChange = {},
                                            readOnly = true,
                                            enabled = false,
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("9:00 AM") },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White,
                                                disabledContainerColor = Color.White,
                                                disabledTextColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Heure fin", fontSize = 14.sp)
                                    Switch(
                                        checked = hasEndTime,
                                        onCheckedChange = { hasEndTime = it }
                                    )
                                }
                                
                                if (hasEndTime) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Heure fin", fontSize = 14.sp)
                                        Box(
                                            modifier = Modifier
                                                .width(140.dp)
                                                .clickable { showTimePickerFin = true }
                                        ) {
                                            OutlinedTextField(
                                                value = heureFin,
                                                onValueChange = {},
                                                readOnly = true,
                                                enabled = false,
                                                modifier = Modifier.fillMaxWidth(),
                                                placeholder = { Text("5:00 PM") },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = Color.White,
                                                    unfocusedContainerColor = Color.White,
                                                    disabledContainerColor = Color.White,
                                                    disabledTextColor = Color.Black
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

