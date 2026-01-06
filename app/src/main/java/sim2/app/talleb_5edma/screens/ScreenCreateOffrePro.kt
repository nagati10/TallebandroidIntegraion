package sim2.app.talleb_5edma.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sim2.app.talleb_5edma.models.CreateOffreRequest
import sim2.app.talleb_5edma.models.JobType
import sim2.app.talleb_5edma.models.Location
import sim2.app.talleb_5edma.models.Shift
import sim2.app.talleb_5edma.models.User
import sim2.app.talleb_5edma.network.OffreRepository
import sim2.app.talleb_5edma.network.UserRepository
import sim2.app.talleb_5edma.util.FileUtils
import sim2.app.talleb_5edma.util.getToken
import java.text.SimpleDateFormat
import java.util.Locale

// Color palette matching Accueil screen
private val LavenderBgTop = Color(0xFFF7F4FF)
private val LavenderBgMid = Color(0xFFF1ECFF)
private val CardBorder = Color(0xFFE6DEFF)
private val SoftText = Color(0xFF6F6B80)
private val TitleText = Color(0xFF1F1B2E)
private val Purple = Color(0xFF7C4DFF)
private val PurpleDark = Color(0xFF5E35B1)
private val RedAccent = Color(0xFFFF4D4D)
private val GreenSuccess = Color(0xFF4CAF50)
private val TagBackground = Color(0xFFEDE7F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenCreateOffrePro(
    navController: NavController,
    snackBarHostState: SnackbarHostState? = null,
    token: String? = null
) {
    val offreRepository = remember { OffreRepository() }
    val userRepository = remember { UserRepository() }
    val context = LocalContext.current

    // Form state - aligned with backend DTO
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var expiresAt by remember { mutableStateOf("") }

    var exigences by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentExigenceInput by remember { mutableStateOf("") }

    // Tags state
    var tags by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentTagInput by remember { mutableStateOf("") }

    // Dropdown states
    var jobTypeExpanded by remember { mutableStateOf(false) }
    var selectedJobType by remember { mutableStateOf(JobType.JOB) } // Default value as per DTO

    var shiftExpanded by remember { mutableStateOf(false) }
    var selectedShift by remember { mutableStateOf(Shift.JOUR) } // Default value as per DTO

    // Multiple images state
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var imageBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    // Current user state
    var currentUser by remember { mutableStateOf<User?>(null) }

    // UI states
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Validation states
    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var cityError by remember { mutableStateOf<String?>(null) }
    var countryError by remember { mutableStateOf<String?>(null) }
    var expiresAtError by remember { mutableStateOf<String?>(null) }
    
    // Touched states - to track if user has interacted with field
    var titleTouched by remember { mutableStateOf(false) }
    var descriptionTouched by remember { mutableStateOf(false) }
    var addressTouched by remember { mutableStateOf(false) }
    var cityTouched by remember { mutableStateOf(false) }
    var countryTouched by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Date formatter for expiresAt
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Multiple Image Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            uris.take(5 - selectedImageUris.size).forEach { uri ->
                try {
                    val previewBitmap = FileUtils.uriToBitmap(context, uri)
                    selectedImageUris = selectedImageUris + uri
                    imageBitmaps = imageBitmaps + previewBitmap!!
                } catch (e: Exception) {
                    scope.launch {
                        snackBarHostState?.showSnackbar(
                            message = "Erreur de chargement d'image: ${e.message}",
                            duration = SnackbarDuration.Long
                        )
                    }
                }
            }
        }
    )

    // Remove image function
    fun removeImage(index: Int) {
        selectedImageUris = selectedImageUris.toMutableList().apply { removeAt(index) }
        imageBitmaps = imageBitmaps.toMutableList().apply { removeAt(index) }
    }

    // Add tag function
    fun addTag() {
        val trimmedTag = currentTagInput.trim()
        if (trimmedTag.isNotEmpty() && !tags.contains(trimmedTag)) {
            tags = tags + trimmedTag
            currentTagInput = ""
        }
    }

    // Remove tag function
    fun removeTag(tag: String) {
        tags = tags.filter { it != tag }
    }

    fun addExigence() {
        val trimmedExigence = currentExigenceInput.trim()
        if (trimmedExigence.isNotEmpty() && !exigences.contains(trimmedExigence)) {
            exigences = exigences + trimmedExigence
            currentExigenceInput = ""
        }
    }

    fun removeExigence(exigence: String) {
        exigences = exigences.filter { it != exigence }
    }

    // Real-time validation functions
    fun validateTitle(value: String): String? {
        return if (value.isBlank()) "Le titre est requis" else null
    }

    fun validateDescription(value: String): String? {
        return if (value.isBlank()) "La description est requise" else null
    }

    fun validateAddress(value: String): String? {
        return if (value.isBlank()) "L'adresse est requise" else null
    }

    fun validateCity(value: String): String? {
        return if (value.isBlank()) "La ville est requise" else null
    }

    fun validateCountry(value: String): String? {
        return if (value.isBlank()) "Le pays est requis" else null
    }

    fun validateExpiresAt(value: String): String? {
        if (value.isBlank()) return null // Optional field
        return try {
            dateFormatter.parse(value)
            null
        } catch (e: Exception) {
            "Format de date invalide. Utilisez YYYY-MM-DD"
        }
    }

    // Update validation on field changes - only if field has been touched
    LaunchedEffect(title, titleTouched) {
        if (titleTouched) {
            titleError = validateTitle(title)
        }
    }

    LaunchedEffect(description, descriptionTouched) {
        if (descriptionTouched) {
            descriptionError = validateDescription(description)
        }
    }

    LaunchedEffect(address, addressTouched) {
        if (addressTouched) {
            addressError = validateAddress(address)
        }
    }

    LaunchedEffect(city, cityTouched) {
        if (cityTouched) {
            cityError = validateCity(city)
        }
    }

    LaunchedEffect(country, countryTouched) {
        if (countryTouched) {
            countryError = validateCountry(country)
        }
    }

    // Load current user data
    LaunchedEffect(token) {
        if (token != null) {
            try {
                val response = userRepository.getCurrentUser(token)
                currentUser = response.data ?: User(
                    _id = response._id,
                    nom = response.nom,
                    email = response.email,
                    contact = response.contact,
                    role = response.role,
                    image = response.image,
                    password = response.password,
                    createdAt = response.createdAt,
                    updatedAt = response.updatedAt,
                    modeExamens = response.modeExamens,
                    isArchive = response.isArchive,
                    trustXP = response.trustXP,
                    isOrganization = response.isOrganization,
                    likedOffres = null
                )
            } catch (e: Exception) {
                println("Error loading current user: ${e.message}")
            }
        }
    }

    // Auto-clear success message after 3 seconds and show snackbar
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            snackBarHostState?.showSnackbar(
                message = successMessage!!,
                duration = SnackbarDuration.Short
            )
            delay(3000)
            successMessage = null
        }
    }

    // Auto-clear error message after 5 seconds and show snackbar
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackBarHostState?.showSnackbar(
                message = errorMessage!!,
                duration = SnackbarDuration.Long
            )
            delay(5000)
            errorMessage = null
        }
    }

    // Handle location picker results
    LaunchedEffect(navController) {
        navController.currentBackStackEntry?.savedStateHandle?.let { savedState ->
            savedState.get<String>("selected_address")?.let { selectedAddress ->
                address = selectedAddress
                savedState.remove<String>("selected_address")
            }
            savedState.get<String>("selected_city")?.let { selectedCity ->
                city = selectedCity
                savedState.remove<String>("selected_city")
            }
            savedState.get<String>("selected_country")?.let { selectedCountry ->
                country = selectedCountry
                savedState.remove<String>("selected_country")
            }
            savedState.get<String>("selected_latitude")?.let { selectedLat ->
                latitude = selectedLat
                savedState.remove<String>("selected_latitude")
            }
            savedState.get<String>("selected_longitude")?.let { selectedLng ->
                longitude = selectedLng
                savedState.remove<String>("selected_longitude")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Créer une offre",
                        color = TitleText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = TitleText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TitleText
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to LavenderBgTop,
                        0.7f to LavenderBgMid,
                        1f to Color.White
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Success/Error messages
                if (successMessage != null && snackBarHostState == null) {
                    SuccessMessagePro(message = successMessage!!)
                }

                if (errorMessage != null && snackBarHostState == null) {
                    ErrorMessagePro(message = errorMessage!!)
                }

                // Image upload section
                FormSectionPro(title = "Images de l'offre") {
                    // Horizontal LazyRow for selected images
                    if (selectedImageUris.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(selectedImageUris.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                ) {
                                    Image(
                                        bitmap = imageBitmaps[index].asImageBitmap(),
                                        contentDescription = "Offer image $index",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Remove button
                                    IconButton(
                                        onClick = { removeImage(index) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .background(Color.Red, CircleShape)
                                    ) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Remove image",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Add image button
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    )
                    {
                        if (selectedImageUris.size < 5) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        galleryLauncher.launch("image/*")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.PhotoCamera,
                                        contentDescription = "Add image",
                                        tint = SoftText,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = "Ajouter une image",
                                        color = SoftText,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${selectedImageUris.size}/5",
                                        color = SoftText,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Form sections
                FormSectionPro(title = "Informations principales") {
                    FormTextFieldPro(
                        value = title,
                        onValueChange = { title = it },
                        label = "Titre du poste *",
                        placeholder = "Ex: Développeur Android Senior",
                        icon = Icons.Filled.Title,
                        isRequired = true,
                        errorMessage = titleError,
                        onFocusChange = { titleTouched = true }
                    )

                    FormTextFieldPro(
                        value = description,
                        onValueChange = { description = it },
                        label = "Description *",
                        placeholder = "Décrivez le poste, les missions principales...",
                        icon = Icons.Filled.Description,
                        isRequired = true,
                        maxLines = 4,
                        errorMessage = descriptionError,
                        onFocusChange = { descriptionTouched = true }
                    )

                    FormTextFieldPro(
                        value = reference,
                        onValueChange = { reference = it },
                        label = "Référence",
                        placeholder = "Ex: REF-2024-001",
                        icon = Icons.Filled.Tag
                    )

                    // Tags input section
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Tags",
                            color = TitleText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Tags input row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = currentTagInput,
                                onValueChange = { currentTagInput = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Ajouter un tag...") },
                                leadingIcon = {
                                    Icon(Icons.Filled.Tag, null, tint = SoftText)
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Purple,
                                    unfocusedBorderColor = CardBorder,
                                    focusedLabelColor = Purple,
                                    unfocusedLabelColor = SoftText,
                                    cursorColor = Purple,
                                    focusedTextColor = TitleText,
                                    unfocusedTextColor = TitleText
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Add tag button
                            IconButton(
                                onClick = { addTag() },
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(Purple, RoundedCornerShape(12.dp)),
                                enabled = currentTagInput.trim().isNotEmpty()
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Add tag",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Display tags
                        if (tags.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(tags) { tag ->
                                    TagChipPro(
                                        text = tag,
                                        onRemove = { removeTag(tag) }
                                    )
                                }
                            }
                        }
                    }

                    // Display company name from current user - read-only
                    OutlinedTextField(
                        value = currentUser?.nom ?: "Chargement...",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Entreprise *") },
                        placeholder = { Text("Nom de votre entreprise") },
                        leadingIcon = {
                            Icon(Icons.Filled.Business, null, tint = SoftText)
                        },
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (currentUser?.nom.isNullOrBlank()) RedAccent else CardBorder,
                            unfocusedBorderColor = if (currentUser?.nom.isNullOrBlank()) RedAccent else CardBorder,
                            focusedLabelColor = if (currentUser?.nom.isNullOrBlank()) RedAccent else SoftText,
                            unfocusedLabelColor = if (currentUser?.nom.isNullOrBlank()) RedAccent else SoftText,
                            focusedTextColor = if (currentUser?.nom.isNullOrBlank()) RedAccent else SoftText,
                            unfocusedTextColor = if (currentUser?.nom.isNullOrBlank()) RedAccent else SoftText
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (currentUser?.nom.isNullOrBlank()) {
                        Text(
                            text = "Impossible de charger les informations de l'entreprise. Veuillez vous reconnecter.",
                            color = RedAccent,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }

                FormSectionPro(title = "Détails du poste") {
                    // Job Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = jobTypeExpanded,
                        onExpandedChange = { jobTypeExpanded = !jobTypeExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedJobType.let {
                                when (it) {
                                    JobType.JOB -> "CDI/CDD"
                                    JobType.STAGE -> "Stage"
                                    JobType.FREELANCE -> "Freelance"
                                }
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Type de contrat") },
                            placeholder = { Text("Sélectionnez le type de contrat") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = jobTypeExpanded)
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Work, null, tint = SoftText)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Purple,
                                unfocusedBorderColor = CardBorder,
                                focusedLabelColor = Purple,
                                unfocusedLabelColor = SoftText
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = jobTypeExpanded,
                            onDismissRequest = { jobTypeExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("CDI/CDD") },
                                onClick = {
                                    selectedJobType = JobType.JOB
                                    jobTypeExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Stage") },
                                onClick = {
                                    selectedJobType = JobType.STAGE
                                    jobTypeExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Freelance") },
                                onClick = {
                                    selectedJobType = JobType.FREELANCE
                                    jobTypeExpanded = false
                                }
                            )
                        }
                    }

                    // Shift Dropdown
                    ExposedDropdownMenuBox(
                        expanded = shiftExpanded,
                        onExpandedChange = { shiftExpanded = !shiftExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedShift.let {
                                when (it) {
                                    Shift.JOUR -> "Jour"
                                    Shift.NUIT -> "Nuit"
                                    Shift.FLEXIBLE -> "Flexible"
                                }
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Horaire de travail") },
                            placeholder = { Text("Sélectionnez l'horaire") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = shiftExpanded)
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Schedule, null, tint = SoftText)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Purple,
                                unfocusedBorderColor = CardBorder,
                                focusedLabelColor = Purple,
                                unfocusedLabelColor = SoftText
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = shiftExpanded,
                            onDismissRequest = { shiftExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Jour") },
                                onClick = {
                                    selectedShift = Shift.JOUR
                                    shiftExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Nuit") },
                                onClick = {
                                    selectedShift = Shift.NUIT
                                    shiftExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Flexible") },
                                onClick = {
                                    selectedShift = Shift.FLEXIBLE
                                    shiftExpanded = false
                                }
                            )
                        }
                    }

                    // Exigences input
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Exigences",
                            color = TitleText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = currentExigenceInput,
                                onValueChange = { currentExigenceInput = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Ajouter une exigence...") },
                                leadingIcon = {
                                    Icon(Icons.Filled.EditNote, null, tint = SoftText)
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Purple,
                                    unfocusedBorderColor = CardBorder,
                                    focusedLabelColor = Purple,
                                    unfocusedLabelColor = SoftText,
                                    cursorColor = Purple,
                                    focusedTextColor = TitleText,
                                    unfocusedTextColor = TitleText
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Add exigence button
                            IconButton(
                                onClick = { addExigence() },
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(Purple, RoundedCornerShape(12.dp)),
                                enabled = currentExigenceInput.trim().isNotEmpty()
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Add exigence",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        if (exigences.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(exigences) { exigence ->
                                    TagChipPro(
                                        text = exigence,
                                        onRemove = { removeExigence(exigence) }
                                    )
                                }
                            }
                        }
                    }

                    FormTextFieldPro(
                        value = salary,
                        onValueChange = { salary = it },
                        label = "Salaire",
                        placeholder = "Ex: 3000€ - 4000€ / mois",
                        icon = Icons.Filled.Euro,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    FormTextFieldPro(
                        value = category,
                        onValueChange = { category = it },
                        label = "Catégorie",
                        placeholder = "Ex: Informatique, Marketing, Vente...",
                        icon = Icons.Filled.Work
                    )

                    // Expiration date field
                    FormTextFieldPro(
                        value = expiresAt,
                        onValueChange = { expiresAt = it },
                        label = "Date d'expiration",
                        placeholder = "YYYY-MM-DD",
                        icon = Icons.Filled.Schedule,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        errorMessage = expiresAtError
                    )
                }

                FormSectionPro(title = "Localisation") {
                    // Map button and address field row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        FormTextFieldPro(
                            value = address,
                            onValueChange = { address = it },
                            label = "Adresse *",
                            placeholder = "Numéro et rue",
                            icon = Icons.Filled.LocationOn,
                            isRequired = true,
                            errorMessage = addressError,
                            modifier = Modifier.weight(1f),
                            onFocusChange = { addressTouched = true }
                        )
                        
                        // Map button
                        IconButton(
                            onClick = {
                                navController.navigate(sim2.app.talleb_5edma.Routes.LocationPicker)
                            },
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(48.dp)
                                .background(Purple, RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Sélectionner sur la carte",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FormTextFieldPro(
                            value = city,
                            onValueChange = { city = it },
                            label = "Ville *",
                            placeholder = "Ex: Paris",
                            icon = null,
                            modifier = Modifier.weight(1f),
                            isRequired = true,
                            errorMessage = cityError,
                            onFocusChange = { cityTouched = true }
                        )

                        FormTextFieldPro(
                            value = country,
                            onValueChange = { country = it },
                            label = "Pays *",
                            placeholder = "Ex: France",
                            icon = null,
                            modifier = Modifier.weight(1f),
                            isRequired = true,
                            errorMessage = countryError,
                            onFocusChange = { countryTouched = true }
                        )
                    }

                    // Coordinates section
                    Text(
                        text = "Coordonnées GPS (optionnel)",
                        color = SoftText,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FormTextFieldPro(
                            value = latitude,
                            onValueChange = { latitude = it },
                            label = "Latitude",
                            placeholder = "Ex: 36.8998",
                            icon = null,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        FormTextFieldPro(
                            value = longitude,
                            onValueChange = { longitude = it },
                            label = "Longitude",
                            placeholder = "Ex: 10.1891",
                            icon = null,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }

                // Submit button - enabled only when form is valid
                val isFormValid = titleError == null &&
                        descriptionError == null &&
                        addressError == null &&
                        cityError == null &&
                        countryError == null &&
                        expiresAtError == null &&
                        currentUser != null &&
                        currentUser?.nom?.isNotBlank() == true

                Button(
                    onClick = {
                        if (isFormValid) {
                            isLoading = true
                            errorMessage = null
                            successMessage = null

                            scope.launch {
                                try {
                                    // Get authentication token
                                    val authToken = token ?: getToken(context)

                                    if (authToken.isNullOrEmpty()) {
                                        val authError = "Authentification requise. Veuillez vous connecter."
                                        errorMessage = authError
                                        snackBarHostState?.showSnackbar(
                                            message = authError,
                                            duration = SnackbarDuration.Long
                                        )
                                        isLoading = false
                                        return@launch
                                    }

                                    // Process multiple image uploads - prepare files for multipart
                                    val imageFiles = mutableListOf<Pair<ByteArray, String>>()
                                    selectedImageUris.forEach { uri ->
                                        try {
                                            val imageBytes = FileUtils.uriToByteArray(context, uri)
                                            val imageFileName = FileUtils.getFileName(context, uri) ?: "image_${System.currentTimeMillis()}.jpg"
                                            imageFiles.add(Pair(imageBytes!!, imageFileName))
                                        } catch (e: Exception) {
                                            println("Error processing image: ${e.message}")
                                        }
                                    }

                                    // Create location object (required in DTO)
                                    // Parse coordinates if provided
                                    val lat = latitude.toDoubleOrNull()
                                    val lng = longitude.toDoubleOrNull()
                                    val locationObj = Location(
                                        address = address,
                                        city = city,
                                        country = country,
                                        coordinates = if (lat != null && lng != null) {
                                            sim2.app.talleb_5edma.models.Coordinates(lat = lat, lng = lng)
                                        } else null
                                    )

                                    // Create Offre object aligned with backend DTO
                                    val offreData = CreateOffreRequest(
                                        title = title,
                                        description = description,
                                        reference = reference.ifEmpty { null },
                                        location = locationObj,
                                        company = currentUser?.nom ?: "",
                                        tags = if (tags.isNotEmpty()) tags else null,
                                        exigences = if (exigences.isNotEmpty()) exigences else null,
                                        category = category.ifEmpty { null },
                                        salary = salary.ifEmpty { null },
                                        expiresAt = expiresAt.ifEmpty { null },
                                        jobType = selectedJobType,
                                        shift = selectedShift,
                                        isActive = true,
                                        imageFiles = null // This will be handled in multipart
                                    )

                                    // *** THIS IS THE CALL YOU NEED TO UPDATE ***
                                    // Call repository function to create offre with image files
                                    println ("CatLog : Offre -> " + offreData)
                                    val response = offreRepository.createOffre(
                                        token = authToken,
                                        createOffreRequest = offreData,
                                        imageFiles = imageFiles  // Add this parameter
                                    )
                                    println ("CatLog : HEre -> ")

                                    if (response.statusCode == null) {
                                        val successMsg = "Offre créée avec succès!"
                                        successMessage = successMsg
                                        snackBarHostState?.showSnackbar(
                                            message = successMsg,
                                            duration = SnackbarDuration.Short
                                        )
                                        println ("CatLog : HEre -> ")

                                        // Clear form after success
                                        title = ""
                                        description = ""
                                        salary = ""
                                        address = ""
                                        city = ""
                                        country = ""
                                        category = ""
                                        expiresAt = ""
                                        tags = emptyList()
                                        currentTagInput = ""
                                        exigences = emptyList()
                                        currentExigenceInput = ""
                                        selectedJobType = JobType.JOB
                                        selectedShift = Shift.JOUR
                                        selectedImageUris = emptyList()
                                        imageBitmaps = emptyList()

                                        // Navigate back after delay
                                        println ("CatLog : HEre -> ")

                                        delay(2000)
                                        navController.popBackStack()

                                    } else {
                                        val errorMsg = response.message ?: "Erreur inconnue lors de la création"
                                        errorMessage = errorMsg
                                        snackBarHostState?.showSnackbar(
                                            message = errorMsg,
                                            duration = SnackbarDuration.Long
                                        )
                                        println("CatLog : Offre -> "+response.errors )
                                    }

                                } catch (e: Exception) {
                                    val errorMsg = when {
                                        e.message?.contains("401", true) == true -> "Authentification requise"
                                        e.message?.contains("403", true) == true -> "Vous n'avez pas les permissions nécessaires"
                                        e.message?.contains("422", true) == true -> "Données invalides"
                                        e.message?.contains("timeout", true) == true -> "Délai d'attente dépassé"
                                        e.message?.contains("unable to resolve", true) == true -> "Impossible de contacter le serveur"
                                        e.message?.contains("failed to connect", true) == true -> "Impossible de se connecter au serveur"
                                        else -> "Erreur lors de la création: ${e.localizedMessage ?: "Veuillez réessayer"}"
                                    }
                                    errorMessage = errorMsg
                                    snackBarHostState?.showSnackbar(
                                        message = errorMsg,
                                        duration = SnackbarDuration.Long
                                    )
                                    println("CatLog : Exception -> ${e.message}")
                                    e.printStackTrace()
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            val validationError = "Veuillez corriger les erreurs dans le formulaire avant de soumettre"
                            errorMessage = validationError
                            scope.launch {
                                snackBarHostState?.showSnackbar(
                                    message = validationError,
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFormValid) Purple else Purple.copy(alpha = 0.5f),
                        contentColor = Color.White,
                        disabledContainerColor = Purple.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isFormValid && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Création en cours...")
                    } else {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Créer",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Créer l'offre",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun TagChipPro(
    text: String,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = TagBackground
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = text,
                color = Purple,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remove tag",
                    tint = Purple,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun FormSectionPro(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                color = TitleText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
fun FormTextFieldPro(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector? = null,
    isRequired: Boolean = false,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    onFocusChange: ((Boolean) -> Unit)? = null
) {
    Column(
        modifier = modifier // Use the passed modifier directly
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(), // Make the text field itself take full width
            label = {
                Text(
                    text = if (isRequired) "$label *" else label,
                    color = if (isRequired) RedAccent else SoftText
                )
            },
            placeholder = {
                Text(
                    text = placeholder,
                    color = SoftText.copy(alpha = 0.7f)
                )
            },
            leadingIcon = icon?.let {
                {
                    Icon(it, null, tint = SoftText)
                }
            },
            keyboardOptions = keyboardOptions,
            maxLines = maxLines,
            isError = errorMessage != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (errorMessage != null) RedAccent else Purple,
                unfocusedBorderColor = if (errorMessage != null) RedAccent else CardBorder,
                focusedLabelColor = if (errorMessage != null) RedAccent else Purple,
                unfocusedLabelColor = if (errorMessage != null) RedAccent else SoftText,
                cursorColor = Purple,
                focusedTextColor = TitleText,
                unfocusedTextColor = TitleText,
                errorTextColor = RedAccent,
                errorBorderColor = RedAccent,
                errorLabelColor = RedAccent
            ),
            shape = RoundedCornerShape(12.dp),
            interactionSource = remember { MutableInteractionSource() }
                .also { interactionSource ->
                    androidx.compose.runtime.LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect { interaction ->
                            when (interaction) {
                                is androidx.compose.foundation.interaction.FocusInteraction.Focus -> {
                                    onFocusChange?.invoke(true)
                                }
                            }
                        }
                    }
                }
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = RedAccent,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun SuccessMessagePro(message: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = GreenSuccess.copy(alpha = 0.1f),
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "Succès",
                tint = GreenSuccess,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = message,
                color = GreenSuccess,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ErrorMessagePro(message: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = RedAccent.copy(alpha = 0.1f),
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Error,
                contentDescription = "Erreur",
                tint = RedAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = message,
                color = RedAccent,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenCreateOffreProPreview() {
    ScreenCreateOffre(navController = rememberNavController())
}