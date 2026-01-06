package sim2.app.talleb_5edma.interfaces

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import sim2.app.talleb_5edma.models.Disponibilite
import sim2.app.talleb_5edma.network.DisponibiliteRepository
import sim2.app.talleb_5edma.util.getToken
import sim2.app.talleb_5edma.Routes

private val Accent = Color(0xFFB71C1C)
private val Soft = Color(0xFF6B6B6B)
private val ChipBg = Color(0xFFF3F3F6)

val joursSemaine = listOf(
    "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"
)

@Composable
fun AvailabilityScreen(
    onBack: () -> Unit,
    onOpenExamMode: () -> Unit,
    navController: NavController? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val actualToken = getToken(context)
    
    var disponibilites by remember { mutableStateOf<List<Disponibilite>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Disponibilite?>(null) }
    
    val repository = remember { DisponibiliteRepository() }
    
    fun loadDisponibilites() {
        scope.launch {
            isLoading = true
            try {
                disponibilites = repository.getAllDisponibilites(actualToken)
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Erreur lors du chargement: ${e.message}")
                }
            } finally {
                isLoading = false
            }
        }
    }
    
    fun deleteDisponibilite(disponibilite: Disponibilite) {
        scope.launch {
            try {
                repository.deleteDisponibilite(actualToken, disponibilite._id!!)
                scope.launch {
                    snackbarHostState.showSnackbar("Disponibilité supprimée")
                }
                loadDisponibilites()
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Erreur: ${e.message}")
                }
            }
        }
    }
    
    // Group disponibilités by day
    val disponibilitesByDay = remember(disponibilites) {
        joursSemaine.associateWith { jour ->
            disponibilites.filter { it.jour == jour }
        }
    }
    
    LaunchedEffect(Unit) {
        if (actualToken.isNotEmpty()) {
            loadDisponibilites()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8FB))
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))

            // Bandeau rouge "Indique quand tu N'ES PAS disponible"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Indique quand tu N'ES PAS disponible",
                    color = Color.Red,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Carte "Gain de temps / Sync Google Calendar"
            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Gain de temps", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Accent)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { /* TODO: sync Google Calendar */ },
                        border = BorderStroke(1.dp, Color(0xFF202124)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Sync Google Calendar")
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Import auto de tes cours",
                        fontSize = 12.sp,
                        color = Soft
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Section "Cette semaine"
            Text("Cette semaine", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Accent)

            Spacer(Modifier.height(10.dp))

            // Afficher tous les jours de la semaine
            joursSemaine.forEach { jour ->
                val slotsForDay = disponibilitesByDay[jour] ?: emptyList()
                
                DayRowHeader(
                    dayName = jour,
                    onAdd = {
                        navController?.navigate("${Routes.ScreenDisponibiliteCreate}/$jour")
                    }
                )

                if (slotsForDay.isEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Aucune indisponibilité",
                        fontSize = 13.sp,
                        color = Soft,
                        modifier = Modifier.padding(start = 4.dp, bottom = 14.dp)
                    )
                } else {
                    Spacer(Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        slotsForDay.forEach { disponibilite ->
                            BusySlotCard(
                                disponibilite = disponibilite,
                                onEdit = {
                                    navController?.navigate("${Routes.ScreenDisponibiliteEdit}/${disponibilite._id}")
                                },
                                onDelete = {
                                    showDeleteDialog = disponibilite
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }
            }

            Spacer(Modifier.height(22.dp))

            // Bouton Mode examens
            Surface(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 1.dp
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { onOpenExamMode() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.AccessTime,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Mode Examens",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Accent
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Configurez vos préférences pour la période d'examens",
                            fontSize = 13.sp,
                            color = Soft
                        )
                    }
                    OutlinedButton(
                        onClick = onOpenExamMode,
                        border = BorderStroke(1.dp, Accent),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Accent)
                    ) {
                        Text("Configurer le mode examens")
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
    
    // Delete confirmation dialog
    showDeleteDialog?.let { disponibilite ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Supprimer la disponibilité") },
            text = { Text("Êtes-vous sûr de vouloir supprimer cette indisponibilité ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteDisponibilite(disponibilite)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun DayRowHeader(dayName: String, onAdd: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(dayName, fontWeight = FontWeight.Medium, fontSize = 15.sp)
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onAdd) {
            Text("+ Ajouter", fontSize = 13.sp, color = Accent)
        }
    }
}

@Composable
private fun BusySlotCard(
    disponibilite: Disponibilite,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ChipBg,
        border = BorderStroke(1.dp, Color(0xFFE1E1E6)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                val timeText = if (disponibilite.heureFin.isNotEmpty()) {
                    "${disponibilite.heureDebut}-${disponibilite.heureFin}"
                } else {
                    disponibilite.heureDebut
                }
                Text(
                    timeText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    disponibilite.jour,
                    fontSize = 13.sp,
                    color = Soft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.Edit,
                    "Modifier",
                    tint = Accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    "Supprimer",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
