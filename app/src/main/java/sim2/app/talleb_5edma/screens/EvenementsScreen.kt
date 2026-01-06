package sim2.app.talleb_5edma.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import sim2.app.talleb_5edma.models.Evenement
import sim2.app.talleb_5edma.network.EvenementRepository
import sim2.app.talleb_5edma.util.getToken

private val Primary = Color(0xFF0D9488)
private val BackgroundStart = Color(0xFFECFEFF)
private val BackgroundEnd = Color(0xFFE0F2FE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvenementsScreen(
    navController: NavController,
    token: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val actualToken = token ?: getToken(context)
    
    var evenements by remember { mutableStateOf<List<Evenement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Evenement?>(null) }
    
    val repository = remember { EvenementRepository() }
    
    fun loadEvenements() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                evenements = repository.getAllEvenements(actualToken)
            } catch (e: Exception) {
                errorMessage = "Erreur: ${e.message}"
                snackbarHostState.showSnackbar("Erreur lors du chargement: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    
    // Load events on start
    LaunchedEffect(actualToken) {
        if (actualToken.isNotEmpty()) {
            loadEvenements()
        }
    }
    
    fun deleteEvenement(evenement: Evenement) {
        scope.launch {
            try {
                repository.deleteEvenement(actualToken, evenement._id!!)
                snackbarHostState.showSnackbar("Événement supprimé avec succès")
                loadEvenements()
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Erreur lors de la suppression: ${e.message}")
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Événements", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("evenements/create")
                },
                containerColor = Primary
            ) {
                Icon(Icons.Default.Add, "Ajouter")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BackgroundStart, BackgroundEnd)
                    )
                )
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { loadEvenements() }) {
                            Text("Réessayer")
                        }
                    }
                }
                evenements.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Aucun événement",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Appuyez sur + pour créer un événement",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(evenements) { evenement ->
                            EvenementCard(
                                evenement = evenement,
                                onEdit = {
                                    navController.navigate("evenements/edit/${evenement._id}")
                                },
                                onDelete = {
                                    showDeleteDialog = evenement
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    showDeleteDialog?.let { evenement ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Supprimer l'événement") },
            text = { Text("Êtes-vous sûr de vouloir supprimer \"${evenement.titre}\" ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteEvenement(evenement)
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
fun EvenementCard(
    evenement: Evenement,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = evenement.titre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Type: ${evenement.type}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Date: ${evenement.date}",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${evenement.heureDebut} - ${evenement.heureFin}",
                        fontSize = 14.sp
                    )
                    evenement.lieu?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Lieu: $it",
                            fontSize = 14.sp
                        )
                    }
                    evenement.tarifHoraire?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tarif: $it €/h",
                            fontSize = 14.sp
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            "Modifier",
                            tint = Primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            "Supprimer",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

