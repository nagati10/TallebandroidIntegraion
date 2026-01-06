package sim2.app.talleb_5edma.interfaces

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import sim2.app.talleb_5edma.models.Evenement
import sim2.app.talleb_5edma.network.EvenementRepository
import sim2.app.talleb_5edma.util.getToken
import sim2.app.talleb_5edma.Routes
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val Accent = Color(0xFFB71C1C)
private val Soft = Color(0xFF6B6B6B)
private val ChipBg = Color(0xFFF3F3F6)
private val GreenAccent = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    onManageAvailability: () -> Unit = {},
    navController: NavController? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val actualToken = getToken(context)
    
    var currentMonth by remember { mutableStateOf(LocalDate.now()) }
    val today = remember { LocalDate.now() }
    var selected by remember { mutableStateOf(today) }
    
    var evenements by remember { mutableStateOf<List<Evenement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showEventMenu by remember { mutableStateOf<Evenement?>(null) }
    
    val repository = remember { EvenementRepository() }
    
    fun loadEvenements() {
        scope.launch {
            isLoading = true
            try {
                evenements = repository.getAllEvenements(actualToken)
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Erreur: ${e.message}")
                }
            } finally {
                isLoading = false
            }
        }
    }
    
    fun deleteEvenement(evenement: Evenement) {
        scope.launch {
            try {
                repository.deleteEvenement(actualToken, evenement._id!!)
                scope.launch {
                    snackbarHostState.showSnackbar("Événement supprimé")
                }
                loadEvenements()
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Erreur: ${e.message}")
                }
            }
        }
    }
    
    // Get events for selected date
    val eventsForSelectedDate = remember(selected, evenements) {
        evenements.filter { event ->
            try {
                val eventDate = LocalDate.parse(event.date.split("T")[0])
                eventDate == selected
            } catch (e: Exception) {
                false
            }
        }.sortedBy { it.heureDebut }
    }
    
    // Get week dates for the selected date's week (Sunday = first day)
    val weekDates = remember(selected) {
        val dayOfWeekValue = selected.dayOfWeek.value // 1=Monday, 7=Sunday
        val daysToSubtract = if (dayOfWeekValue == 7) 0L else dayOfWeekValue.toLong()
        val startOfWeek = selected.minusDays(daysToSubtract)
        (0..6).map { startOfWeek.plusDays(it.toLong()) }
    }
    
    LaunchedEffect(Unit) {
        if (actualToken.isNotEmpty()) {
            loadEvenements()
        }
    }
    
    // Reload when month changes
    LaunchedEffect(currentMonth) {
        if (actualToken.isNotEmpty()) {
            loadEvenements()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.Filled.ArrowBack, "Précédent", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.Filled.ArrowForward, "Suivant", tint = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            navController?.navigate(Routes.ScreenEvenementCreate)
                        },
                        modifier = Modifier.size(40.dp),
                        containerColor = Accent
                    ) {
                        Icon(Icons.Filled.Add, "Ajouter", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Accent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Calendar grid header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam").forEach { day ->
                    Text(
                        day,
                        fontSize = 12.sp,
                        color = Soft,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Calendar grid dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekDates.forEach { date ->
                    val isSelected = date == selected
                    val isToday = date == today
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selected = date },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "${date.dayOfMonth}",
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else if (isToday) Accent else Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Timeline + événements
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Generate time slots from 06:00 to 23:00
                    val timeSlots = (6..23).map { String.format("%02d:00", it) }
                    
                    items(timeSlots) { timeSlot ->
                        val eventsAtThisTime = eventsForSelectedDate.filter { event ->
                            event.heureDebut.startsWith(timeSlot.substring(0, 2))
                        }
                        
                        if (eventsAtThisTime.isNotEmpty()) {
                            eventsAtThisTime.forEach { event ->
                                EventCard(
                                    event = event,
                                    onEdit = {
                                        navController?.navigate("${Routes.ScreenEvenementEdit}/${event._id}")
                                    },
                                    onDelete = {
                                        showEventMenu = event
                                    }
                                )
                            }
                        } else {
                            // Show empty time slot
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    timeSlot,
                                    fontSize = 12.sp,
                                    color = Soft,
                                    modifier = Modifier.width(56.dp)
                                )
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Event menu dialog
    showEventMenu?.let { event ->
        AlertDialog(
            onDismissRequest = { showEventMenu = null },
            title = { Text("Actions") },
            text = {
                Column {
                    TextButton(onClick = {
                        navController?.navigate("${Routes.ScreenEvenementEdit}/${event._id}")
                        showEventMenu = null
                    }) {
                        Text("Modifier")
                    }
                    TextButton(
                        onClick = {
                            deleteEvenement(event)
                            showEventMenu = null
                        }
                    ) {
                        Text("Supprimer", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEventMenu = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun EventCard(
    event: Evenement,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.width(56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                event.heureDebut,
                fontSize = 12.sp,
                color = Soft
            )
            Box(
                Modifier
                    .width(1.dp)
                    .height(8.dp)
                    .background(Soft.copy(alpha = .4f))
            )
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, Color(0xFFE7E7EC)),
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 80.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        event.titre,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color(0xFF1E1E1E)
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "${event.heureDebut} - ${event.heureFin}",
                            fontSize = 12.sp,
                            color = Soft
                        )
                        if (event.lieu != null) {
                            Text("•", color = Soft)
                            Text(
                                event.lieu,
                                fontSize = 12.sp,
                                color = Soft,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GreenAccent.copy(alpha = 0.2f),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            event.type,
                            fontSize = 11.sp,
                            color = GreenAccent,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.MoreVert,
                        "Menu",
                        tint = Soft,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
