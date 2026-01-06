package sim2.app.talleb_5edma.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import com.google.gson.Gson
import kotlinx.coroutines.launch
import sim2.app.talleb_5edma.models.*
import sim2.app.talleb_5edma.network.DisponibiliteRepository
import sim2.app.talleb_5edma.network.EvenementRepository
import sim2.app.talleb_5edma.network.RoutineRepository
import sim2.app.talleb_5edma.util.*
import java.text.SimpleDateFormat
import android.util.Log
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

private val Background = Color(0xFFF8F8FB)
private val Primary = Color(0xFF0D9488)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineAnalysisScreen(
    navController: NavController,
    token: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val actualToken = token ?: getToken(context)
    
    var analysisData by remember { mutableStateOf<RoutineAnalysisDataEnhanced?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastAnalysisDate by remember { mutableStateOf<String?>(null) }
    
    val routineRepository = remember { RoutineRepository() }
    val evenementRepository = remember { EvenementRepository() }
    val disponibiliteRepository = remember { DisponibiliteRepository() }
    val gson = remember { Gson() }
    val LOG_TAG = "EnhancedRoutine"
    
    // Calculer les dates de début et fin de la semaine (lundi à dimanche) au format ISO avec LocalDate
    fun getWeekDates(referenceDate: LocalDate): Pair<String, String> {
        // Méthode alternative plus explicite pour calculer le lundi de la semaine
        // DayOfWeek.MONDAY = 1, TUESDAY = 2, ..., SUNDAY = 7
        val dayOfWeek = referenceDate.dayOfWeek.value
        val daysToSubtract = if (dayOfWeek == 1) 0 else dayOfWeek - 1 // 1 = Monday

        val monday = referenceDate.minusDays(daysToSubtract.toLong())
        val sunday = monday.plusDays(6) // 6 jours après lundi = dimanche

        // Créer les LocalDateTime pour début et fin
        val startDateTime = monday.atStartOfDay() // 00:00:00
        val endDateTime = sunday.atTime(23, 59, 59) // 23:59:59

        // Formater en UTC avec le format demandé
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)

        val dateDebut = startDateTime.atZone(ZoneOffset.UTC).format(formatter)
        val dateFin = endDateTime.atZone(ZoneOffset.UTC).format(formatter)

        println("CatLog: === WEEK CALCULATION DEBUG ===")
        println("CatLog: Reference date: ${referenceDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
        println("CatLog: Day of week value: $dayOfWeek (1=Monday, 7=Sunday)")
        println("CatLog: Days to subtract: $daysToSubtract")
        println("CatLog: Monday: ${monday.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
        println("CatLog: Sunday: ${sunday.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
        println("CatLog: API dateDebut: $dateDebut")
        println("CatLog: API dateFin: $dateFin")

        return Pair(dateDebut, dateFin)
    }

    // Fonction pour convertir un nom de jour (Lundi, Mardi, etc.) en date YYYY-MM-DD de la semaine actuelle
    fun convertDayNameToDate(dayName: String, referenceDate: LocalDate): String {
        // Trouver le lundi de la semaine actuelle
        val monday = referenceDate.minusDays((referenceDate.dayOfWeek.value - 1).toLong())

        // Mapping des noms de jours français aux offsets depuis lundi
        val dayOffsetMapping = mapOf(
            "Lundi" to 0,
            "Mardi" to 1,
            "Mercredi" to 2,
            "Jeudi" to 3,
            "Vendredi" to 4,
            "Samedi" to 5,
            "Dimanche" to 6
        )

        val daysToAdd = dayOffsetMapping[dayName.trim()]
        if (daysToAdd == null) {
            println("CatLog: Unknown day name: '$dayName' - valid names: ${dayOffsetMapping.keys.joinToString(", ")}")
            return ""
        }

        // Calculer la date en ajoutant les jours au lundi
        val targetDate = monday.plusDays(daysToAdd.toLong())
        val dateStr = targetDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        println("CatLog: Converted '$dayName' to date: $dateStr (Monday was ${monday.format(DateTimeFormatter.ISO_LOCAL_DATE)})")

        return dateStr
    }

    // Fonction pour obtenir les dates de début et fin au format YYYY-MM-DD pour le filtrage
    fun getWeekDateRangeForFiltering(referenceDate: LocalDate): Pair<String, String> {
        // Trouver le lundi de la semaine
        val monday = referenceDate.minusDays((referenceDate.dayOfWeek.value - 1).toLong())
        // Trouver le dimanche de la semaine
        val sunday = monday.plusDays(6)

        val dateDebut = monday.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val dateFin = sunday.format(DateTimeFormatter.ISO_LOCAL_DATE)

        println("CatLog: Filter range - Monday: $dateDebut, Sunday: $dateFin")

        return Pair(dateDebut, dateFin)
    }
    
    fun loadAnalysis(useCache: Boolean = true) {
        scope.launch {
            // Vérifier le cache d'abord
            if (useCache && isRoutineCacheValid(context)) {
                val cachedData = getRoutineAnalysisCache(context)
                if (cachedData != null) {
                    try {
                        val cached = gson.fromJson(cachedData, RoutineAnalysisDataEnhanced::class.java)
                        analysisData = cached
                        println("CatLog: Using cached routine analysis data")
                        return@launch
                    } catch (e: Exception) {
                        println("CatLog: Error parsing cached data: ${e.message}")
                        clearRoutineAnalysisCache(context)
                    }
                }
            }
            
            isLoading = true
            errorMessage = null
            
            try {
                // CRITIQUEMENT IMPORTANT : Utiliser UTC pour la cohérence avec iOS
                // LocalDate.now() peut donner des résultats différents selon le timezone du device
                // 1) Charger tous les événements pour déterminer une date de référence fiable (éviter LocalDate.now)
                val allEventsForReference = try {
                    evenementRepository.getAllEvenements(actualToken)
                } catch (e: Exception) {
                    Log.d(LOG_TAG, "Failed to load all events for reference date: ${e.message}")
                    emptyList()
                }

                val referenceDate = allEventsForReference
                    .mapNotNull { event ->
                        try {
                            val dateStr = if (event.date.contains("T")) event.date.split("T")[0] else event.date
                            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                        } catch (_: Exception) { null }
                    }
                    .minOrNull()
                    ?: LocalDate.now(ZoneOffset.UTC).also {
                        Log.d(LOG_TAG, "WARNING - No events to derive reference date, falling back to today UTC: $it")
                    }

                Log.d(LOG_TAG, "REFERENCE DATE - derived from events (or fallback): $referenceDate (TZ=${java.util.TimeZone.getDefault().id})")

                val (dateDebut, dateFin) = getWeekDates(referenceDate)
                val (filterDateDebut, filterDateFin) = getWeekDateRangeForFiltering(referenceDate)

                // Validation des plages de dates
                if (dateDebut.isBlank() || dateFin.isBlank() || filterDateDebut.isBlank() || filterDateFin.isBlank()) {
                    throw IllegalStateException("Invalid date ranges calculated: dateDebut='$dateDebut', dateFin='$dateFin', filterDateDebut='$filterDateDebut', filterDateFin='$filterDateFin'")
                }

                Log.d(LOG_TAG, "Final date ranges -> api: $dateDebut to $dateFin | filter: $filterDateDebut to $filterDateFin")

                // CHARGER LES ÉVÉNEMENTS FILTRÉS CÔTÉ SERVEUR (CRITIQUEMENT IMPORTANT pour cohérence avec iOS)
                Log.d(LOG_TAG, "LOADING EVENTS BY DATE RANGE: $filterDateDebut -> $filterDateFin (server filter, aligned iOS)")

                val evenements = try {
                    // Utiliser la méthode de filtrage côté serveur pour cohérence avec iOS
                    val serverFilteredEvents = evenementRepository.getEvenementsByDateRange(actualToken, filterDateDebut, filterDateFin)
                    println("CatLog: SUCCESS - Server returned ${serverFilteredEvents.size} events for date range")

                    // Vérifier que le serveur a bien filtré (quick check)
                    val obviouslyOutOfRange = serverFilteredEvents.filter { event ->
                        try {
                            val eventDate = if (event.date.contains("T")) event.date.split("T")[0] else event.date
                            eventDate < filterDateDebut || eventDate > filterDateFin
                        } catch (e: Exception) {
                            false
                        }
                    }

                    if (obviouslyOutOfRange.isNotEmpty()) {
                        println("CatLog: WARNING - Server returned ${obviouslyOutOfRange.size} events outside requested range!")
                        obviouslyOutOfRange.take(3).forEach { event ->
                            println("CatLog:   - ${event.titre} on ${event.date}")
                        }
                    }

                    serverFilteredEvents

                } catch (e: Exception) {
                    println("CatLog: CRITICAL ERROR - Failed to load events by date range: ${e.message}")
                    println("CatLog: This will cause INCONSISTENCY with iOS which may use server filtering!")
                    println("CatLog: Falling back to getAllEvenements - results will likely differ from iOS")

                    // Fallback vers la méthode générale si la filtrée échoue
                    val allEvents = evenementRepository.getAllEvenements(actualToken)
                    println("CatLog: Fallback loaded ${allEvents.size} total events")

                    allEvents
                }

                // Filtrage côté app (optionnel) pour réduire la payload et être identique au serveur
                println("CatLog: === CLIENT-SIDE FILTER (optimization) ===")
                val clientFilteredEvents = evenements.filter { event ->
                    try {
                        val eventDate = if (event.date.contains("T")) {
                            event.date.split("T")[0]
                        } else {
                            event.date
                        }
                        eventDate >= filterDateDebut && eventDate <= filterDateFin
                    } catch (_: Exception) {
                        false
                    }
                }
                Log.d(LOG_TAG, "Events after client-side filter: ${clientFilteredEvents.size}/${evenements.size}")

                val disponibilites = disponibiliteRepository.getAllDisponibilites(actualToken)
                
                Log.d(LOG_TAG, "DATA LOADING RESULTS - Events(server): ${evenements.size}, Disponibilites: ${disponibilites.size}")

                // LOG DÉTAILLÉ DES ÉVÉNEMENTS BRUTS POUR DIAGNOSTIC
                println("CatLog: === RAW EVENTS FROM SERVER ===")
                if (evenements.isNotEmpty()) {
                    evenements.take(10).forEachIndexed { index, event ->
                        println("CatLog: Raw Event ${index + 1}:")
                        println("CatLog:   - ID: ${event._id}")
                        println("CatLog:   - Title: '${event.titre}'")
                        println("CatLog:   - Type: '${event.type}'")
                        println("CatLog:   - Date: '${event.date}'")
                        println("CatLog:   - HeureDebut: '${event.heureDebut}'")
                        println("CatLog:   - HeureFin: '${event.heureFin}'")
                    }
                    if (evenements.size > 10) {
                        println("CatLog: ... and ${evenements.size - 10} more events")
                    }
                } else {
                    println("CatLog: No events found")
                }

                // LOG DÉTAILLÉ DES DISPONIBILITÉS BRUTES
                println("CatLog: === RAW DISPONIBILITES FROM SERVER ===")
                if (disponibilites.isNotEmpty()) {
                    disponibilites.forEachIndexed { index, disp ->
                        println("CatLog: Raw Disponibilite ${index + 1}:")
                        println("CatLog:   - ID: ${disp._id}")
                        println("CatLog:   - Jour: '${disp.jour}'")
                        println("CatLog:   - HeureDebut: '${disp.heureDebut}'")
                        println("CatLog:   - HeureFin: '${disp.heureFin}'")
                    }
                } else {
                    println("CatLog: No disponibilites found")
                }

                // Validation des données chargées
                if (evenements.isEmpty() && disponibilites.isEmpty()) {
                    println("CatLog: Warning - No events or disponibilites found for user")
                }

                // VALIDER LES ÉVÉNEMENTS (déjà filtrés côté serveur)
                println("CatLog: === EVENT VALIDATION (SERVER FILTERED) ===")
                println("CatLog: Validating events that should already be filtered between $filterDateDebut and $filterDateFin")

                var invalidDateCount = 0
                var nullDateCount = 0
                var outOfRangeCount = 0

                val evenementsSemaine = clientFilteredEvents.filter { event ->
                    try {
                        // Validation de base
                        if (event.date.isNullOrBlank()) {
                            println("CatLog: Event validation failed - NULL/BLANK date: ${event.titre}")
                            nullDateCount++
                            return@filter false
                        }

                        // Normaliser la date (extraire YYYY-MM-DD sans timezone)
                        val eventDate = if (event.date.contains("T")) {
                            event.date.split("T")[0]
                        } else {
                            event.date
                        }

                        // Valider le format de la date
                        if (!eventDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                            println("CatLog: Event validation failed - Invalid date format '${event.date}' -> '$eventDate' for '${event.titre}'")
                            invalidDateCount++
                            return@filter false
                        }

                        // Vérifier si la date est dans la plage (le serveur devrait avoir filtré)
                        val isInRange = eventDate >= filterDateDebut && eventDate <= filterDateFin

                        if (!isInRange) {
                            println("CatLog: WARNING - Event outside expected range (server may not be filtering correctly): ${event.titre} on $eventDate (expected range: $filterDateDebut to $filterDateFin)")
                            outOfRangeCount++
                            // Garder quand même l'événement pour ne pas perdre de données
                        }

                        true // Garder tous les événements valides
                    } catch (e: Exception) {
                        println("CatLog: Error validating event ${event.titre} (date: '${event.date}'): ${e.message}")
                        false
                    }
                }

                println("CatLog: === VALIDATION SUMMARY ===")
                println("CatLog: Events received from server: ${evenements.size}")
                println("CatLog: Events after validation: ${evenementsSemaine.size}")
                println("CatLog: Events with invalid dates: $invalidDateCount")
                println("CatLog: Events with null/blank dates: $nullDateCount")
                println("CatLog: Events outside expected range: $outOfRangeCount")

                if (outOfRangeCount > 0) {
                    println("CatLog: CRITICAL - Server-side filtering appears to be broken! $outOfRangeCount events outside the requested date range.")
                } else {
                    println("CatLog: ✓ Server-side filtering appears to be working correctly")
                }
                
                // Convertir en DTOs stricts - Format strict pour le backend enhanced
                println("CatLog: === EVENT CONVERSION ===")

                var eventsValidationFailed = 0
                var eventsConversionError = 0

                val evenementsDto = evenementsSemaine.mapNotNull { event ->
                    try {
                        // Validation des champs obligatoires
                        if (event.titre.isNullOrBlank()) {
                            println("CatLog: Event validation failed - Empty/null title for event ID: ${event._id}")
                            eventsValidationFailed++
                            return@mapNotNull null
                        }

                        if (event.type.isNullOrBlank()) {
                            println("CatLog: Event validation failed - Empty/null type for '${event.titre}'")
                            eventsValidationFailed++
                            return@mapNotNull null
                        }

                        if (event.date.isNullOrBlank()) {
                            println("CatLog: Event validation failed - Empty/null date for '${event.titre}'")
                            eventsValidationFailed++
                            return@mapNotNull null
                        }

                        if (event.heureDebut.isNullOrBlank()) {
                            println("CatLog: Event validation failed - Empty/null heureDebut for '${event.titre}'")
                            eventsValidationFailed++
                            return@mapNotNull null
                        }

                        if (event.heureFin.isNullOrBlank()) {
                            println("CatLog: Event validation failed - Empty/null heureFin for '${event.titre}'")
                            eventsValidationFailed++
                            return@mapNotNull null
                        }

                        // Normaliser la date (sans timezone, format YYYY-MM-DD)
                        val eventDate = if (event.date.contains("T")) {
                            event.date.split("T")[0]
                        } else {
                            event.date
                        }
                        
                        // Validation du format de date
                        if (!eventDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                            println("CatLog: Event validation failed - Invalid date format '${event.date}' -> '$eventDate' for '${event.titre}'")
                            eventsValidationFailed++
                            return@mapNotNull null
                        }

                        // Validation du format des heures (HH:MM 24h)
                        if (!event.heureDebut.matches(Regex("\\d{2}:\\d{2}"))) {
                            println("CatLog: Event validation failed - Invalid heureDebut format '${event.heureDebut}' for '${event.titre}'")
                            eventsValidationFailed++
                            return@mapNotNull null
                        }

                        if (!event.heureFin.matches(Regex("\\d{2}:\\d{2}"))) {
                            println("CatLog: Event validation failed - Invalid heureFin format '${event.heureFin}' for '${event.titre}'")
                            eventsValidationFailed++
                            return@mapNotNull null
                        }

                        // Validation logique : heure de fin après heure de début
                        try {
                            val debutParts = event.heureDebut.split(":").map { it.toInt() }
                            val finParts = event.heureFin.split(":").map { it.toInt() }
                            val debutMinutes = debutParts[0] * 60 + debutParts[1]
                            val finMinutes = finParts[0] * 60 + finParts[1]

                            if (finMinutes <= debutMinutes) {
                                println("CatLog: Event validation failed - End time (${event.heureFin}) before or equal to start time (${event.heureDebut}) for '${event.titre}'")
                                eventsValidationFailed++
                                return@mapNotNull null
                            }
                        } catch (e: Exception) {
                            println("CatLog: Event validation failed - Error parsing times for '${event.titre}': ${e.message}")
                            eventsValidationFailed++
                            return@mapNotNull null
                        }

                        val dto = EvenementDtoStrict(
                            id = event._id,
                                titre = event.titre.trim(),
                                type = event.type.trim(),
                                date = eventDate,
                                heureDebut = event.heureDebut.trim(),
                                heureFin = event.heureFin.trim()
                            )

                        println("CatLog: Event converted successfully: ${dto.titre} - ${dto.date} ${dto.heureDebut}-${dto.heureFin}")
                        dto

                    } catch (e: Exception) {
                        println("CatLog: Error converting event '${event.titre}': ${e.message}")
                        eventsConversionError++
                        null
                    }
                }

                println("CatLog: Events conversion summary:")
                println("CatLog: - Successfully converted: ${evenementsDto.size}")
                println("CatLog: - Validation failed: $eventsValidationFailed")
                println("CatLog: - Conversion errors: $eventsConversionError")
                
                // Convertir les disponibilités en DTOs stricts
                println("CatLog: === DISPONIBILITE CONVERSION ===")

                var disponibilitesValidationFailed = 0
                var disponibilitesConversionError = 0
                
                val disponibilitesDto = disponibilites.mapNotNull { disp ->
                    try {
                        // Validation des champs obligatoires
                        if (disp.jour.isNullOrBlank()) {
                            Log.d(LOG_TAG, "Disponibilite validation failed - Empty/null jour for ID: ${disp._id}")
                            disponibilitesValidationFailed++
                            return@mapNotNull null
                        }

                        if (disp.heureDebut.isNullOrBlank()) {
                            Log.d(LOG_TAG, "Disponibilite validation failed - Empty/null heureDebut for jour: '${disp.jour}'")
                            disponibilitesValidationFailed++
                            return@mapNotNull null
                        }

                        if (disp.heureFin.isNullOrBlank()) {
                            Log.d(LOG_TAG, "Disponibilite validation failed - Empty/null heureFin for jour: '${disp.jour}'")
                            disponibilitesValidationFailed++
                            return@mapNotNull null
                        }

                        // Déjà au format date ?
                        val dateRegex = Regex("\\d{4}-\\d{2}-\\d{2}")
                        val jourDate = when {
                            dateRegex.matches(disp.jour.trim()) -> disp.jour.trim()
                            else -> convertDayNameToDate(disp.jour, referenceDate)
                        }

                        if (jourDate.isBlank()) {
                            Log.d(LOG_TAG, "Disponibilite validation failed - Could not normalize jour '${disp.jour}' to date")
                            disponibilitesValidationFailed++
                            return@mapNotNull null
                        }

                        // Filtrer sur la semaine courante (lundi-dimanche)
                        val monday = referenceDate.minusDays((referenceDate.dayOfWeek.value - 1).toLong())
                        val sunday = monday.plusDays(6)
                        val dispoDate = LocalDate.parse(jourDate, DateTimeFormatter.ISO_LOCAL_DATE)
                        if (dispoDate.isBefore(monday) || dispoDate.isAfter(sunday)) {
                            println("CatLog: Disponibilite filtered out - outside week range: $jourDate")
                            disponibilitesValidationFailed++
                            return@mapNotNull null
                        }

                        // Validation du format des heures (HH:MM 24h)
                        if (!disp.heureDebut.matches(Regex("\\d{2}:\\d{2}"))) {
                            println("CatLog: Disponibilite validation failed - Invalid heureDebut format '${disp.heureDebut}' for '${disp.jour}'")
                            disponibilitesValidationFailed++
                            return@mapNotNull null
                        }

                        if (!disp.heureFin.matches(Regex("\\d{2}:\\d{2}"))) {
                            println("CatLog: Disponibilite validation failed - Invalid heureFin format '${disp.heureFin}' for '${disp.jour}'")
                            disponibilitesValidationFailed++
                            return@mapNotNull null
                        }

                        // Validation logique : heure de fin après heure de début
                        try {
                            val debutParts = disp.heureDebut.split(":").map { it.toInt() }
                            val finParts = disp.heureFin.split(":").map { it.toInt() }
                            val debutMinutes = debutParts[0] * 60 + debutParts[1]
                            val finMinutes = finParts[0] * 60 + finParts[1]

                            if (finMinutes <= debutMinutes) {
                                println("CatLog: Disponibilite validation failed - End time (${disp.heureFin}) before or equal to start time (${disp.heureDebut}) for '${disp.jour}'")
                                disponibilitesValidationFailed++
                                return@mapNotNull null
                            }
                        } catch (e: Exception) {
                            println("CatLog: Disponibilite validation failed - Error parsing times for '${disp.jour}': ${e.message}")
                            disponibilitesValidationFailed++
                            return@mapNotNull null
                        }

                        val dto = DisponibiliteDtoStrict(
                            id = disp._id,
                            jour = jourDate,
                                heureDebut = disp.heureDebut.trim(),
                                heureFin = disp.heureFin.trim()
                            )

                        println("CatLog: Disponibilite converted successfully: ${disp.jour} -> $jourDate ${dto.heureDebut}-${dto.heureFin}")
                        dto

                    } catch (e: Exception) {
                        println("CatLog: Error converting disponibilite for jour '${disp.jour}': ${e.message}")
                        disponibilitesConversionError++
                        null
                    }
                }

                println("CatLog: Disponibilites conversion summary:")
                println("CatLog: - Successfully converted: ${disponibilitesDto.size}")
                println("CatLog: - Validation failed: $disponibilitesValidationFailed")
                println("CatLog: - Conversion errors: $disponibilitesConversionError")
                
                println("CatLog: Validated - Events: ${evenementsDto.size}, Disponibilities: ${disponibilitesDto.size}")
                
                // Log des premiers éléments pour debug
                if (evenementsDto.isNotEmpty()) {
                    val firstEvent = evenementsDto.first()
                    println("CatLog: First event sample - id: '${firstEvent.id}', titre: '${firstEvent.titre}', type: '${firstEvent.type}', date: '${firstEvent.date}', heureDebut: '${firstEvent.heureDebut}', heureFin: '${firstEvent.heureFin}'")
                }
                
                if (disponibilitesDto.isNotEmpty()) {
                    val firstDisp = disponibilitesDto.first()
                    println("CatLog: First disponibilite sample - id: '${firstDisp.id}', jour: '${firstDisp.jour}', heureDebut: '${firstDisp.heureDebut}', heureFin: '${firstDisp.heureFin}'")
                }
                
                // Validation finale des données avant envoi
                println("CatLog: === FINAL VALIDATION ===")

                // Vérifier que les dates sont cohérentes
                val parsedDateDebut = try {
                    LocalDateTime.parse(dateDebut.replace("Z", "+00:00"), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                } catch (e: Exception) {
                    throw IllegalStateException("Invalid dateDebut format: $dateDebut")
                }

                val parsedDateFin = try {
                    LocalDateTime.parse(dateFin.replace("Z", "+00:00"), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                } catch (e: Exception) {
                    throw IllegalStateException("Invalid dateFin format: $dateFin")
                }

                if (parsedDateFin.isBefore(parsedDateDebut)) {
                    throw IllegalStateException("dateFin ($dateFin) is before dateDebut ($dateDebut)")
                }

                // Vérifier que tous les événements sont dans la plage
                val invalidEvents = evenementsDto.filter { event ->
                    try {
                        val eventDate = LocalDate.parse(event.date, DateTimeFormatter.ISO_LOCAL_DATE)
                        val monday = referenceDate.minusDays((referenceDate.dayOfWeek.value - 1).toLong())
                        val sunday = monday.plusDays(6)
                        eventDate.isBefore(monday) || eventDate.isAfter(sunday)
                    } catch (e: Exception) {
                        true // Considérer comme invalide si on ne peut pas parser
                    }
                }

                if (invalidEvents.isNotEmpty()) {
                    println("CatLog: ERROR - Found ${invalidEvents.size} events outside week range:")
                    invalidEvents.forEach { event ->
                        println("CatLog:   - ${event.titre} on ${event.date}")
                    }
                    throw IllegalStateException("Found ${invalidEvents.size} events outside the week range")
                }

                // CRÉER LE PAYLOAD STRICT (aligné iOS)
                val requestStrict = RoutineInputDataDtoStrict(
                    evenements = evenementsDto,
                    disponibilites = disponibilitesDto,
                    dateDebut = dateDebut,
                    dateFin = dateFin
                )
                
                Log.d(LOG_TAG, "ANDROID ROUTINE ANALYSIS PAYLOAD - Week: $filterDateDebut -> $filterDateFin, API: $dateDebut -> $dateFin, Events sent: ${evenementsDto.size}, Dispos sent: ${disponibilitesDto.size}, Ref: ${referenceDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}")

                // Calcul de la semaine détaillé pour vérification
                val monday = referenceDate.minusDays((referenceDate.dayOfWeek.value - 1).toLong())
                val sunday = monday.plusDays(6)
                println("CatLog: Week calculation details:")
                println("CatLog: - Monday: ${monday.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
                println("CatLog: - Sunday: ${sunday.format(DateTimeFormatter.ISO_LOCAL_DATE)}")

                // Vérification que tous les événements sont dans la semaine calculée
                val eventsOutsideWeek = evenementsDto.filter { event ->
                    val eventDate = LocalDate.parse(event.date, DateTimeFormatter.ISO_LOCAL_DATE)
                    eventDate.isBefore(monday) || eventDate.isAfter(sunday)
                }
                if (eventsOutsideWeek.isNotEmpty()) {
                    println("CatLog: ERROR - Found ${eventsOutsideWeek.size} events outside calculated week!")
                    eventsOutsideWeek.forEach { event ->
                        println("CatLog:   - Event '${event.titre}' on ${event.date} (week: $monday to $sunday)")
                    }
                    // Ne pas stopper, mais signaler clairement
                } else {
                    println("CatLog: ✓ All events are within the calculated week range")
                }

                // Log détaillé des événements
                println("CatLog: === EVENTS LIST ===")
                if (evenementsDto.isEmpty()) {
                    println("CatLog: No events to send")
                } else {
                    evenementsDto.forEachIndexed { index, event ->
                        println("CatLog: Event ${index + 1}: ${event.titre} / ${event.type} / ${event.date} ${event.heureDebut}-${event.heureFin} (id=${event.id})")
                    }
                }

                // Log détaillé des disponibilités
                println("CatLog: === DISPONIBILITES LIST ===")
                if (disponibilitesDto.isEmpty()) {
                    println("CatLog: No disponibilites to send")
                } else {
                    disponibilitesDto.forEachIndexed { index, disp ->
                        println("CatLog: Disponibilite ${index + 1}: ${disp.jour} ${disp.heureDebut}-${disp.heureFin} (id=${disp.id})")
                    }
                }

                // Log du JSON complet (comme iOS stringify)
                val payloadJson = gson.toJson(requestStrict)
                Log.d(LOG_TAG, "FULL JSON PAYLOAD size=${payloadJson.length}")
                Log.d(LOG_TAG, payloadJson)

                // Validation finale avant envoi
                if (evenementsDto.isEmpty() && disponibilitesDto.isEmpty()) {
                    println("CatLog: WARNING - Sending empty payload (no events and no disponibilites)")
                }

                if (evenementsDto.size > 100) {
                    println("CatLog: WARNING - Large number of events: ${evenementsDto.size}")
                }

                if (disponibilitesDto.size > 7) {
                    println("CatLog: WARNING - Large number of disponibilites: ${disponibilitesDto.size} (expected max 7 for one week)")
                }

                // Appeler uniquement l'API Enhanced (aligné iOS corrigé)
                val response = routineRepository.analyzeRoutineEnhanced(actualToken, requestStrict)

                // Traiter la réponse
                val success = response.success
                val data = response.data
                val message = response.message

                if (success && data != null) {
                    // Convertir les données selon le type
                    analysisData = when (data) {
                        is RoutineAnalysisDataEnhanced -> data
                        is RoutineAnalysisData -> {
                            // Convertir legacy vers enhanced format
                            RoutineAnalysisDataEnhanced(
                                scoreEquilibre = data.scoreEquilibre,
                                scoreBreakdown = null,
                                conflicts = emptyList(),
                                overloadedDays = emptyList(),
                                availableTimeSlots = emptyList(),
                                recommandations = data.recommandations?.map { rec ->
                                    Recommandation(
                                        type = rec.categorie,
                                        titre = rec.titre,
                                        description = rec.description,
                                        priorite = rec.priorite,
                                        actionSuggeree = rec.action
                                    )
                                } ?: emptyList(),
                                analyseHebdomadaire = null,
                                healthSummary = null
                            )
                        }
                        else -> null
                    }

                    lastAnalysisDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                    
                    // Sauvegarder dans le cache
                    try {
                        val jsonData = gson.toJson(analysisData)
                        saveRoutineAnalysisCache(context, jsonData)
                    } catch (e: Exception) {
                        println("CatLog: Error saving cache: ${e.message}")
                    }

                    // Log du score final pour comparaison avec iOS
                    val finalScore = analysisData?.scoreEquilibre ?: -1
                    Log.d(LOG_TAG, "FINAL SCORE (enhanced): $finalScore")
                    
                    snackbarHostState.showSnackbar(
                        message = "Analyse terminée avec succès",
                        duration = SnackbarDuration.Short
                    )
                } else {
                    errorMessage = message ?: "Erreur lors de l'analyse"
                }
            } catch (e: Exception) {
                println("CatLog: Error analyzing routine: ${e.message}")
                errorMessage = "Erreur: ${e.message}"
                
                // Essayer de charger depuis le cache en cas d'erreur
                if (useCache) {
                    val cachedData = getRoutineAnalysisCache(context)
                    if (cachedData != null) {
                        try {
                            val cached = gson.fromJson(cachedData, RoutineAnalysisDataEnhanced::class.java)
                            analysisData = cached
                            snackbarHostState.showSnackbar(
                                message = "Données en cache (erreur réseau)",
                                duration = SnackbarDuration.Long
                            )
                            return@launch
                        } catch (e2: Exception) {
                            println("CatLog: Error loading from cache: ${e2.message}")
                        }
                    }
                }
                
                snackbarHostState.showSnackbar(
                    message = "Erreur: ${e.message}",
                    duration = SnackbarDuration.Long
                )
            } finally {
                isLoading = false
            }
        }
    }
    
    // Charger l'analyse au démarrage
    LaunchedEffect(actualToken) {
        if (actualToken.isNotEmpty()) {
            loadAnalysis(useCache = true)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Mon Planning",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Retour",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { loadAnalysis(useCache = false) },
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            "Actualiser",
                            tint = if (isLoading) Color.Gray else Primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Analyse en cours...",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
                errorMessage != null && analysisData == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Erreur inconnue",
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { loadAnalysis(useCache = false) },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Réessayer")
                        }
                    }
                }
                analysisData != null -> {
                    AnalysisContentEnhanced(
                        analysisData = analysisData!!,
                        lastAnalysisDate = lastAnalysisDate,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Aucune donnée disponible",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { loadAnalysis(useCache = false) },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Analyser Mon Planning")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalysisContentEnhanced(
    analysisData: RoutineAnalysisDataEnhanced,
    lastAnalysisDate: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Date de dernière analyse
        lastAnalysisDate?.let {
            Text(
                text = "Dernière analyse: $it",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        // Score Card
        ScoreCard(
            score = analysisData.scoreEquilibre,
            scoreBreakdown = analysisData.scoreBreakdown
        )
        
        // Health Summary
        analysisData.healthSummary?.let { healthSummary ->
            HealthSummaryCard(healthSummary = healthSummary)
        }
        
        // Statistics Cards
        analysisData.analyseHebdomadaire?.let { analyse ->
            StatisticsCards(analyseHebdomadaire = analyse)
        }
        
        // Conflicts List
        ConflictsList(conflicts = analysisData.conflicts)
        
        // Overloaded Days List
        OverloadedDaysList(overloadedDays = analysisData.overloadedDays)
        
        // Recommendations List
        RecommendationsList(recommendations = analysisData.recommandations)
        
        // Padding en bas
        Spacer(modifier = Modifier.height(20.dp))
    }
}
