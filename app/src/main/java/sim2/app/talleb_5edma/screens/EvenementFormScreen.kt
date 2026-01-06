package sim2.app.talleb_5edma.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
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
import sim2.app.talleb_5edma.models.CreateEvenementRequest
import sim2.app.talleb_5edma.models.Evenement
import sim2.app.talleb_5edma.models.UpdateEvenementRequest
import sim2.app.talleb_5edma.network.EvenementRepository
import sim2.app.talleb_5edma.util.getToken
import java.text.SimpleDateFormat
import java.util.*

private val Primary = Color(0xFF0D9488)
private val Soft = Color(0xFF6B6B6B)

val typesEvenement = listOf("Cours", "Job", "Deadline", "Examen", "Réunion", "Autre")
val couleursDisponibles = listOf(
    "Rouge" to "#FF0000",
    "Bleu" to "#0000FF",
    "Vert" to "#00FF00",
    "Orange" to "#FFA500",
    "Violet" to "#800080",
    "Rose" to "#FFC0CB",
    "Turquoise" to "#40E0D0",
    "Jaune" to "#FFFF00",
    "Rouge bordeaux" to "#800020",
    "Gris" to "#808080"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvenementFormScreen(
    navController: NavController,
    eventId: String? = null,
    token: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val actualToken = token ?: getToken(context)
    val isEditMode = eventId != null
    
    var titre by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var heureDebut by remember { mutableStateOf("") }
    var heureFin by remember { mutableStateOf("") }
    var lieu by remember { mutableStateOf("") }
    var tarifHoraire by remember { mutableStateOf("") }
    var couleur by remember { mutableStateOf("#808080") } // Default to Gris
    var couleurNom by remember { mutableStateOf("Gris") }
    
    var expandedType by remember { mutableStateOf(false) }
    var expandedCouleur by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePickerDebut by remember { mutableStateOf(false) }
    var showTimePickerFin by remember { mutableStateOf(false) }
    
    val calendar = remember { Calendar.getInstance() }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var selectedTimeDebut by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var selectedTimeFin by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingEvent by remember { mutableStateOf(isEditMode) }
    
    val repository = remember { EvenementRepository() }
    
    // Load event if editing
    LaunchedEffect(eventId) {
        if (isEditMode && actualToken.isNotEmpty() && eventId != null) {
            scope.launch {
                try {
                    val event = repository.getEvenementById(actualToken, eventId)
                    titre = event.titre
                    type = event.type
                    date = event.date
                    
                    // Convert times from 24-hour to 12-hour format for display
                    try {
                        val timeFormat24 = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val timeFormat12 = SimpleDateFormat("h:mm a", Locale.getDefault())
                        val dateObj = timeFormat24.parse(event.heureDebut)
                        heureDebut = timeFormat12.format(dateObj ?: Date())
                    } catch (_: Exception) {
                        heureDebut = event.heureDebut
                    }
                    
                    try {
                        val timeFormat24 = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val timeFormat12 = SimpleDateFormat("h:mm a", Locale.getDefault())
                        val dateObj = timeFormat24.parse(event.heureFin)
                        heureFin = timeFormat12.format(dateObj ?: Date())
                    } catch (_: Exception) {
                        heureFin = event.heureFin
                    }
                    
                    lieu = event.lieu ?: ""
                    tarifHoraire = event.tarifHoraire?.toString() ?: ""
                    couleur = event.couleur ?: "#808080"
                    // Find color name from hex
                    couleurNom = couleursDisponibles.find { it.second.equals(couleur, ignoreCase = true) }?.first ?: "Gris"
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Erreur: ${e.message}")
                } finally {
                    isLoadingEvent = false
                }
            }
        }
    }
    
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
        
        // If all parsing fails, try to extract manually
        println("CatLog: Warning - Could not parse time format: $timeStr, using as is")
        return timeStr
    }
    
    // Helper function to format date for display
    val formatDateForDisplay: (String) -> String = { dateStr ->
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            if (date != null) {
                val calendar = Calendar.getInstance()
                calendar.time = date
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                val month = monthNames[calendar.get(Calendar.MONTH)]
                val year = calendar.get(Calendar.YEAR)
                "$day $month $year"
            } else {
                dateStr
            }
        } catch (e: Exception) {
            dateStr
        }
    }
    
    fun saveEvenement() {
        if (titre.isBlank() || type.isBlank() || date.isBlank() || 
            heureDebut.isBlank() || heureFin.isBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar("Veuillez remplir tous les champs obligatoires")
            }
            return
        }
        
        scope.launch {
            isLoading = true
            try {
                // Refresh token from storage to ensure we have the latest one
                val refreshedToken = getToken(context)
                if (refreshedToken.isBlank()) {
                    snackbarHostState.showSnackbar("Erreur: Token d'authentification manquant. Veuillez vous reconnecter.")
                    isLoading = false
                    return@launch
                }
                
                // Use refreshed token
                val tokenToUse = refreshedToken
                
                println("CatLog: EvenementFormScreen - Using token: ${tokenToUse.take(20)}... (length: ${tokenToUse.length})")
                
                // Convert times to 24-hour format for API
                val heureDebut24 = convertTimeTo24Hour(heureDebut)
                val heureFin24 = convertTimeTo24Hour(heureFin)
                
                // Validate converted times
                if (heureDebut24.isBlank() || heureFin24.isBlank()) {
                    snackbarHostState.showSnackbar("Erreur: Format d'heure invalide")
                    isLoading = false
                    return@launch
                }
                
                println("CatLog: EvenementFormScreen - Original times - heureDebut: $heureDebut, heureFin: $heureFin")
                println("CatLog: EvenementFormScreen - Converted times - heureDebut24: $heureDebut24, heureFin24: $heureFin24")
                println("CatLog: EvenementFormScreen - Date: $date, Type: $type, Titre: $titre")
                
                if (isEditMode) {
                    val request = UpdateEvenementRequest(
                        titre = titre,
                        type = type,
                        date = date,
                        heureDebut = heureDebut24,
                        heureFin = heureFin24,
                        lieu = lieu.takeIf { it.isNotBlank() },
                        tarifHoraire = tarifHoraire.toDoubleOrNull(),
                        couleur = couleur.takeIf { it.isNotBlank() }
                    )
                    val result = repository.updateEvenement(tokenToUse, eventId ?: return@launch, request)
                    println("CatLog: EvenementFormScreen - Event updated successfully: ${result._id}")
                    snackbarHostState.showSnackbar("Événement modifié avec succès")
                } else {
                    // Convert type to lowercase to match API expectations
                    val typeLowercase = type.lowercase()
                    
                    val request = CreateEvenementRequest(
                        titre = titre,
                        type = typeLowercase,
                        date = date,
                        heureDebut = heureDebut24,
                        heureFin = heureFin24,
                        lieu = lieu.takeIf { it.isNotBlank() },
                        tarifHoraire = tarifHoraire.toDoubleOrNull(),
                        couleur = couleur.takeIf { it.isNotBlank() }
                    )
                    println("CatLog: EvenementFormScreen - Sending request with type: $typeLowercase")
                    val result = repository.createEvenement(tokenToUse, request)
                    println("CatLog: EvenementFormScreen - Event created successfully: ${result._id}")
                    snackbarHostState.showSnackbar("Événement créé avec succès")
                }
                navController.popBackStack()
            } catch (e: Exception) {
                println("CatLog: EvenementFormScreen - Error: ${e.message}")
                println("CatLog: EvenementFormScreen - Error stack trace: ${e.stackTraceToString()}")
                
                // Check if it's an authentication error
                val errorMessage = e.message ?: "Erreur inconnue"
                if (errorMessage.contains("401") || errorMessage.contains("Unauthorized")) {
                    snackbarHostState.showSnackbar("Erreur: Session expirée. Veuillez vous reconnecter.")
                    // Optionally navigate to login screen
                    // navController.navigate(Routes.Screen1) { popUpTo(0) }
                } else {
                    snackbarHostState.showSnackbar("Erreur: $errorMessage")
                }
            } finally {
                isLoading = false
            }
        }
    }
    
    // Date picker dialog
    DisposableEffect(showDatePicker) {
        if (showDatePicker) {
            val datePicker = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    date = dateFormat.format(calendar.time)
                    showDatePicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
        onDispose { }
    }
    
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isEditMode) "Modifier l'événement" else "Nouvel événement",
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
                        onClick = { saveEvenement() },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8FB))
                .padding(paddingValues)
        ) {
            if (isLoadingEvent) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Section: Informations de base
                    Column {
                        Text(
                            "Informations de base",
                            fontSize = 14.sp,
                            color = Soft,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        OutlinedTextField(
                            value = titre,
                            onValueChange = { titre = it },
                            label = { Text("Titre") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Type dropdown
                        ExposedDropdownMenuBox(
                            expanded = expandedType,
                            onExpandedChange = { expandedType = !expandedType }
                        ) {
                            OutlinedTextField(
                                value = type,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Type") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedType,
                                onDismissRequest = { expandedType = false }
                            ) {
                                typesEvenement.forEach { typeOption ->
                                    DropdownMenuItem(
                                        text = { Text(typeOption) },
                                        onClick = {
                                            type = typeOption
                                            expandedType = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Date picker
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true }
                        ) {
                            OutlinedTextField(
                                value = if (date.isNotEmpty()) formatDateForDisplay(date) else "",
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                label = { Text("Date") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = "Sélectionner la date"
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    disabledContainerColor = Color.White,
                                    disabledTextColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("17 Nov 2025") }
                            )
                        }
                    }
                    
                    // Section: Horaires
                    Column {
                        Text(
                            "Horaires",
                            fontSize = 14.sp,
                            color = Soft,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
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
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White,
                                                disabledContainerColor = Color.White,
                                                disabledTextColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            placeholder = { Text("8:56 AM") }
                                        )
                                    }
                                }
                                
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
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White,
                                                disabledContainerColor = Color.White,
                                                disabledTextColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            placeholder = { Text("8:56 AM") }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Section: Détails (optionnel)
                    Column {
                        Text(
                            "Détails (optionnel)",
                            fontSize = 14.sp,
                            color = Soft,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        OutlinedTextField(
                            value = lieu,
                            onValueChange = { lieu = it },
                            label = { Text("Lieu") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Couleur dropdown
                        ExposedDropdownMenuBox(
                            expanded = expandedCouleur,
                            onExpandedChange = { expandedCouleur = !expandedCouleur }
                        ) {
                            OutlinedTextField(
                                value = couleurNom,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Couleur") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCouleur) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedCouleur,
                                onDismissRequest = { expandedCouleur = false }
                            ) {
                                couleursDisponibles.forEach { (nom, hex) ->
                                    DropdownMenuItem(
                                        text = { Text(nom) },
                                        onClick = {
                                            couleurNom = nom
                                            couleur = hex
                                            expandedCouleur = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = tarifHoraire,
                            onValueChange = { tarifHoraire = it },
                            label = { Text("Tarif horaire (€)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { saveEvenement() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text(if (isEditMode) "Modifier" else "Créer")
                        }
                    }
                }
            }
        }
    }
}

