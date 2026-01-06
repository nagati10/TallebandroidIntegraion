package sim2.app.talleb_5edma.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import sim2.app.talleb_5edma.models.Course
import sim2.app.talleb_5edma.network.ScheduleRepository
import sim2.app.talleb_5edma.util.FileUtils
import sim2.app.talleb_5edma.util.getToken
import java.text.SimpleDateFormat
import java.util.*

private val Primary = Color(0xFF0D9488)
private val Soft = Color(0xFF6B6B6B)
private val Background = Color(0xFFF8F8FB)
private val CardBackground = Color.White
private val SuccessColor = Color(0xFF10B981)
private val ErrorColor = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleUploadScreen(
    navController: NavController,
    token: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val actualToken = token ?: getToken(context)
    
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var extractedCourses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var weekStartDate by remember { mutableStateOf("") }
    
    var isProcessing by remember { mutableStateOf(false) }
    var isCreatingEvents by remember { mutableStateOf(false) }
    var processingStep by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var eventsCreatedCount by remember { mutableStateOf(0) }
    
    val repository = remember { ScheduleRepository() }
    
    // Calculer la date de début de semaine (lundi)
    fun getCurrentWeekStart(): String {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
        calendar.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    
    // Initialiser avec la date de début de semaine courante
    LaunchedEffect(Unit) {
        weekStartDate = getCurrentWeekStart()
    }
    
    // Launcher pour sélectionner une image (JPG, PNG uniquement)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            // Obtenir le nom du fichier
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val nameIndex = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        selectedFileName = c.getString(nameIndex)
                    }
                }
            }
            // Vérifier que c'est une image valide
            val mimeType = context.contentResolver.getType(it)
            if (mimeType != null && !mimeType.startsWith("image/")) {
                errorMessage = "Veuillez sélectionner une image (JPG ou PNG)"
                selectedFileUri = null
                selectedFileName = null
                selectedImageBitmap = null
                return@let
            }
            // Générer un nom par défaut si nécessaire
            if (selectedFileName == null) {
                val extension = when (mimeType) {
                    "image/jpeg", "image/jpg" -> "jpg"
                    "image/png" -> "png"
                    else -> "jpg"
                }
                selectedFileName = "schedule_${System.currentTimeMillis()}.$extension"
            }
            // Charger l'image pour prévisualisation
            try {
                selectedImageBitmap = FileUtils.uriToBitmap(context, it)
            } catch (e: Exception) {
                println("CatLog: Error loading image preview: ${e.message}")
            }
            // Réinitialiser les états
            extractedCourses = emptyList()
            errorMessage = null
            successMessage = null
            eventsCreatedCount = 0
        }
    }
    
    // Fonction pour traiter l'image
    fun processImage() {
        val uri = selectedFileUri ?: return
        val fileName = selectedFileName ?: return
        
        scope.launch {
            isProcessing = true
            processingStep = "Lecture de l'image..."
            errorMessage = null
            successMessage = null
            
            try {
                // Vérifier le type MIME
                val mimeType = context.contentResolver.getType(uri)
                if (mimeType == null || !mimeType.startsWith("image/")) {
                    throw Exception("Le fichier sélectionné n'est pas une image valide")
                }
                
                // Vérifier que c'est JPG ou PNG
                if (mimeType != "image/jpeg" && mimeType != "image/jpg" && mimeType != "image/png") {
                    throw Exception("Veuillez sélectionner une image au format JPG ou PNG")
                }
                
                // Lire le contenu du fichier
                val imageBytes = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.readBytes()
                }
                
                if (imageBytes == null || imageBytes.isEmpty()) {
                    throw Exception("Impossible de lire l'image")
                }
                
                println("CatLog: Image size: ${imageBytes.size} bytes, MIME: $mimeType")
                
                processingStep = "Extraction des cours avec l'IA..."
                
                // Traiter l'image
                val response = repository.processImageSchedule(imageBytes, fileName)
                
                extractedCourses = response.courses
                successMessage = "${response.courses.size} cours extraits avec succès"
                
                snackbarHostState.showSnackbar(
                    message = successMessage!!,
                    duration = SnackbarDuration.Short
                )
                
            } catch (e: Exception) {
                println("CatLog: Error processing image: ${e.message}")
                errorMessage = "Erreur: ${e.message}"
                snackbarHostState.showSnackbar(
                    message = errorMessage!!,
                    duration = SnackbarDuration.Long
                )
            } finally {
                isProcessing = false
                processingStep = ""
            }
        }
    }
    
    // Fonction pour créer les événements
    fun createEvents() {
        if (extractedCourses.isEmpty()) return
        
        scope.launch {
            isCreatingEvents = true
            errorMessage = null
            successMessage = null
            
            try {
                val startDate = if (weekStartDate.isNotBlank()) weekStartDate else null
                
                val response = repository.createEventsFromSchedule(
                    token = actualToken,
                    courses = extractedCourses,
                    weekStartDate = startDate
                )
                
                eventsCreatedCount = response.eventsCreated
                successMessage = response.message
                
                snackbarHostState.showSnackbar(
                    message = "${response.eventsCreated} événements créés avec succès",
                    duration = SnackbarDuration.Long
                )
                
                // Réinitialiser après succès
                selectedFileUri = null
                selectedFileName = null
                extractedCourses = emptyList()
                
            } catch (e: Exception) {
                println("CatLog: Error creating events: ${e.message}")
                errorMessage = "Erreur: ${e.message}"
                snackbarHostState.showSnackbar(
                    message = errorMessage!!,
                    duration = SnackbarDuration.Long
                )
            } finally {
                isCreatingEvents = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Import emploi du temps",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // En-tête avec instructions
            InstructionCard()
            
            // Sélection d'image
            FileSelectionCard(
                selectedFileName = selectedFileName,
                selectedImageBitmap = selectedImageBitmap,
                onSelectFile = { 
                    // Sélectionner une image (JPG ou PNG) - la validation est faite après sélection
                    imagePickerLauncher.launch("image/*")
                }
            )
            
            // Date de début de semaine
            if (selectedFileName != null) {
                WeekStartDateCard(
                    weekStartDate = weekStartDate,
                    onDateChange = { weekStartDate = it }
                )
            }
            
            // Bouton pour traiter l'image
            if (selectedFileName != null && extractedCourses.isEmpty()) {
                Button(
                    onClick = { processImage() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    if (isProcessing) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Text(processingStep)
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, "Analyser")
                            Text("Analyser l'image avec l'IA")
                        }
                    }
                }
            }
            
            // Messages de statut
            successMessage?.let {
                StatusCard(message = it, isError = false)
            }
            
            errorMessage?.let {
                StatusCard(message = it, isError = true)
            }
            
            // Affichage des cours extraits
            if (extractedCourses.isNotEmpty()) {
                CoursesListCard(courses = extractedCourses)
                
                // Bouton pour créer les événements
                Button(
                    onClick = { createEvents() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isCreatingEvents,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)
                ) {
                    if (isCreatingEvents) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Text("Création en cours...")
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, "Créer")
                            Text("Créer ${extractedCourses.size} événements")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InstructionCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Comment ça marche ?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            InstructionStep(
                number = "1",
                text = "Sélectionnez une photo de votre emploi du temps (JPG ou PNG)"
            )
            InstructionStep(
                number = "2",
                text = "Notre IA analyse et extrait automatiquement les cours"
            )
            InstructionStep(
                number = "3",
                text = "Vérifiez les cours extraits et ajustez la date si nécessaire"
            )
            InstructionStep(
                number = "4",
                text = "Créez tous les événements d'un seul clic !"
            )
        }
    }
}

@Composable
fun InstructionStep(number: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Primary,
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    number,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
        Text(
            text,
            fontSize = 14.sp,
            color = Soft,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun FileSelectionCard(
    selectedFileName: String?,
    selectedImageBitmap: Bitmap?,
    onSelectFile: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectFile),
        shape = RoundedCornerShape(12.dp),
        color = if (selectedFileName != null) Primary.copy(alpha = 0.1f) else CardBackground,
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            if (selectedFileName != null) Primary else Color.LightGray
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Afficher la prévisualisation de l'image si disponible
            if (selectedImageBitmap != null) {
                Image(
                    bitmap = selectedImageBitmap.asImageBitmap(),
                    contentDescription = "Image sélectionnée",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    if (selectedFileName != null) Icons.Default.CheckCircle else Icons.Default.Image,
                    contentDescription = null,
                    tint = if (selectedFileName != null) Primary else Soft,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Text(
                selectedFileName ?: "Sélectionner une image (JPG ou PNG)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (selectedFileName != null) Primary else Color.Black
            )
            
            if (selectedFileName == null) {
                Text(
                    "Appuyez pour choisir une photo de votre emploi du temps",
                    fontSize = 12.sp,
                    color = Soft
                )
            }
        }
    }
}

@Composable
fun WeekStartDateCard(
    weekStartDate: String,
    onDateChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Date de début de semaine",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Soft
            )
            
            OutlinedTextField(
                value = weekStartDate,
                onValueChange = onDateChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("2024-12-01") },
                leadingIcon = {
                    Icon(Icons.Default.DateRange, "Date")
                },
                singleLine = true
            )
            
            Text(
                "Format: AAAA-MM-JJ (lundi de la semaine)",
                fontSize = 12.sp,
                color = Soft
            )
        }
    }
}

@Composable
fun StatusCard(message: String, isError: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isError) ErrorColor.copy(alpha = 0.1f) else SuccessColor.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isError) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isError) ErrorColor else SuccessColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                message,
                fontSize = 14.sp,
                color = if (isError) ErrorColor else SuccessColor
            )
        }
    }
}

@Composable
fun CoursesListCard(courses: List<Course>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Cours extraits (${courses.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            courses.forEach { course ->
                CourseItem(course = course)
                if (course != courses.last()) {
                    Divider(color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
fun CourseItem(course: Course) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                course.subject,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Primary.copy(alpha = 0.1f)
            ) {
                Text(
                    course.day,
                    fontSize = 12.sp,
                    color = Primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                tint = Soft,
                modifier = Modifier.size(16.dp)
            )
            Text(
                "${course.start} - ${course.end}",
                fontSize = 14.sp,
                color = Soft
            )
        }
        
        course.classroom?.let {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    tint = Soft,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    it,
                    fontSize = 14.sp,
                    color = Soft
                )
            }
        }
        
        course.professor?.let {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Soft,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    it,
                    fontSize = 14.sp,
                    color = Soft
                )
            }
        }
    }
}

