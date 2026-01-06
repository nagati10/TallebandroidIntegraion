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
import sim2.app.talleb_5edma.models.Disponibilite
import sim2.app.talleb_5edma.network.DisponibiliteRepository
import sim2.app.talleb_5edma.util.getToken

private val Primary = Color(0xFF0D9488)
private val BackgroundStart = Color(0xFFECFEFF)
private val BackgroundEnd = Color(0xFFE0F2FE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisponibilitesScreen(
    navController: NavController,
    token: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val actualToken = token ?: getToken(context)
    
    var disponibilites by remember { mutableStateOf<List<Disponibilite>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Disponibilite?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    
    val repository = remember { DisponibiliteRepository() }
    
    fun loadDisponibilites() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                disponibilites = repository.getAllDisponibilites(actualToken)
            } catch (e: Exception) {
                errorMessage = "Erreur: ${e.message}"
                snackbarHostState.showSnackbar("Erreur lors du chargement: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    
    // Load availabilities on start
    LaunchedEffect(actualToken) {
        if (actualToken.isNotEmpty()) {
            loadDisponibilites()
        }
    }
    
    fun deleteDisponibilite(disponibilite: Disponibilite) {
        scope.launch {
            try {
                repository.deleteDisponibilite(actualToken, disponibilite._id!!)
                snackbarHostState.showSnackbar("Disponibilité supprimée avec succès")
                loadDisponibilites()
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Erreur lors de la suppression: ${e.message}")
            }
        }
    }
    
    fun deleteAllDisponibilites() {
        scope.launch {
            try {
                repository.deleteAllDisponibilites(actualToken)
                snackbarHostState.showSnackbar("Toutes les disponibilités ont été supprimées")
                loadDisponibilites()
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Erreur lors de la suppression: ${e.message}")
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Disponibilités", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    if (disponibilites.isNotEmpty()) {
                        TextButton(
                            onClick = { showDeleteAllDialog = true }
                        ) {
                            Text("Tout supprimer", color = MaterialTheme.colorScheme.error)
                        }
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
                    navController.navigate("disponibilites/create")
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
                        Button(onClick = { loadDisponibilites() }) {
                            Text("Réessayer")
                        }
                    }
                }
                disponibilites.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Aucune disponibilité",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Appuyez sur + pour créer une disponibilité",
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
                        items(disponibilites) { disponibilite ->
                            DisponibiliteCard(
                                disponibilite = disponibilite,
                                onEdit = {
                                    navController.navigate("disponibilites/edit/${disponibilite._id}")
                                },
                                onDelete = {
                                    showDeleteDialog = disponibilite
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    showDeleteDialog?.let { disponibilite ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Supprimer la disponibilité") },
            text = { Text("Êtes-vous sûr de vouloir supprimer la disponibilité du ${disponibilite.jour} ?") },
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
    
    // Delete all confirmation dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Supprimer toutes les disponibilités") },
            text = { Text("Êtes-vous sûr de vouloir supprimer toutes les disponibilités ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteAllDisponibilites()
                        showDeleteAllDialog = false
                    }
                ) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun DisponibiliteCard(
    disponibilite: Disponibilite,
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
                        text = disponibilite.jour,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${disponibilite.heureDebut} - ${disponibilite.heureFin}",
                        fontSize = 16.sp
                    )
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

