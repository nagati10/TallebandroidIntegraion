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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import sim2.app.talleb_5edma.models.JobType
import sim2.app.talleb_5edma.models.Location
import sim2.app.talleb_5edma.models.Shift
import sim2.app.talleb_5edma.models.UpdateOffreRequest
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
fun ScreenUpdateOffre(
    navController: NavController,
    offerId: String,
    snackBarHostState: SnackbarHostState? = null,
    token: String? = null
) {
    println("CatLog : UpdateOffre ScreenUpdateOffre - Composable called")
    println("CatLog : UpdateOffre offerId = $offerId, token = ${token?.take(10)}...")

    val offreRepository = remember { OffreRepository() }
    val userRepository = remember { UserRepository() }
    val context = LocalContext.current

    // Form state - aligned with backend DTO
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var expiresAt by remember { mutableStateOf("") }

    var exigences by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentExigenceInput by remember { mutableStateOf("") }

    // Tags state
    var tags by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentTagInput by remember { mutableStateOf("") }

    // Dropdown states
    var jobTypeExpanded by remember { mutableStateOf(false) }
    var selectedJobType by remember { mutableStateOf(JobType.JOB) }

    var shiftExpanded by remember { mutableStateOf(false) }
    var selectedShift by remember { mutableStateOf(Shift.JOUR) }

    // Multiple images state
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var imageBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    // Current user state
    var currentUser by remember { mutableStateOf<User?>(null) }

    // UI states
    var isLoading by remember { mutableStateOf(false) }
    var isInitialLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Validation states
    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var cityError by remember { mutableStateOf<String?>(null) }
    var countryError by remember { mutableStateOf<String?>(null) }
    var expiresAtError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Date formatter for expiresAt
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Debug: Track LaunchedEffect execution
    var launchedEffectExecutionCount by remember { mutableStateOf(0) }

    // Load offer data when screen opens
    LaunchedEffect(offerId, token) {
        launchedEffectExecutionCount++
        println("CatLog : UpdateOffre LaunchedEffect triggered - Execution #$launchedEffectExecutionCount")
        println("CatLog : UpdateOffre offerId = $offerId, token = ${token?.take(10)}...")
        println("CatLog : UpdateOffre isInitialLoading = $isInitialLoading")

        if (token != null) {
            try {
                isInitialLoading = true
                println("CatLog : UpdateOffre Loading offer data for ID: $offerId")

                // Load current user
                println("CatLog : UpdateOffre Loading current user...")
                val userResponse = userRepository.getCurrentUser(token)
                currentUser = userResponse.data ?: User(
                    _id = userResponse._id,
                    nom = userResponse.nom,
                    email = userResponse.email,
                    contact = userResponse.contact,
                    role = userResponse.role,
                    image = userResponse.image,
                    password = userResponse.password,
                    createdAt = userResponse.createdAt,
                    updatedAt = userResponse.updatedAt,
                    modeExamens = userResponse.modeExamens,
                    isArchive = userResponse.isArchive,
                    trustXP = userResponse.trustXP,
                    isOrganization = userResponse.isOrganization,
                    likedOffres = null
                )
                println("CatLog : UpdateOffre Current user loaded: ${currentUser?.nom}")

                // Load offer data
                println("CatLog : UpdateOffre Loading offer data...")
                val offer = offreRepository.getOffreById(offerId)
                println("CatLog : UpdateOffre Loaded offer: ${offer.title}")

                // Pre-fill form with existing data
                title = offer.title ?: ""
                description = offer.description ?: ""
                salary = offer.salary ?: ""
                category = offer.category ?: ""
                expiresAt = offer.expiresAt ?: ""

                // Location
                address = offer.location?.address ?: ""
                city = offer.location?.city ?: ""
                country = offer.location?.country ?: ""

                // Tags and exigences
                tags = offer.tags ?: emptyList()
                exigences = offer.exigences ?: emptyList()

                // Job type and shift
                selectedJobType = offer.jobType ?: JobType.JOB
                selectedShift = offer.shift ?: Shift.JOUR

                println("CatLog : UpdateOffre Form pre-filled successfully")
                println("CatLog : UpdateOffre Title = '$title', Description length = ${description.length}")
                println("CatLog : UpdateOffre Address = '$address', City = '$city', Country = '$country'")
                println("CatLog : UpdateOffre Tags count = ${tags.size}, Exigences count = ${exigences.size}")

            } catch (e: Exception) {
                errorMessage = "Erreur de chargement: ${e.message}"
                println("CatLog : UpdateOffre Error loading offer: ${e.message}")
                println("CatLog : UpdateOffre Error stack trace:")
                e.printStackTrace()
            } finally {
                isInitialLoading = false
                println("CatLog : UpdateOffre Loading completed, isInitialLoading set to false")
            }
        } else {
            println("CatLog : UpdateOffre Token is null, skipping data loading")
            isInitialLoading = false
        }
    }

    // Debug: Track form state changes
    LaunchedEffect(title, description, address, city, country) {
        println("CatLog : UpdateOffre Form fields changed - Title: '$title', Desc: '${description.take(20)}...', Addr: '$address', City: '$city', Country: '$country'")
    }

    // Debug: Track loading state
    LaunchedEffect(isInitialLoading) {
        println("CatLog : UpdateOffre isInitialLoading state changed to: $isInitialLoading")
    }

    // Multiple Image Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            println("CatLog : UpdateOffre Image picker result - ${uris.size} images selected")
            uris.take(5 - selectedImageUris.size).forEach { uri ->
                try {
                    val previewBitmap = FileUtils.uriToBitmap(context, uri)
                    selectedImageUris = selectedImageUris + uri
                    imageBitmaps = imageBitmaps + previewBitmap!!
                    println("CatLog : UpdateOffre Added image, total: ${selectedImageUris.size}")
                } catch (e: Exception) {
                    println("CatLog : UpdateOffre Error loading image: ${e.message}")
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
        println("CatLog : UpdateOffre Removing image at index $index")
        selectedImageUris = selectedImageUris.toMutableList().apply { removeAt(index) }
        imageBitmaps = imageBitmaps.toMutableList().apply { removeAt(index) }
        println("CatLog : UpdateOffre Images after removal: ${selectedImageUris.size}")
    }

    // Add tag function
    fun addTag() {
        val trimmedTag = currentTagInput.trim()
        println("CatLog : UpdateOffre Adding tag: '$trimmedTag'")
        if (trimmedTag.isNotEmpty() && !tags.contains(trimmedTag)) {
            tags = tags + trimmedTag
            currentTagInput = ""
            println("CatLog : UpdateOffre Tag added, total tags: ${tags.size}")
        } else {
            println("CatLog : UpdateOffre Tag not added - empty or duplicate")
        }
    }

    // Remove tag function
    fun removeTag(tag: String) {
        println("CatLog : UpdateOffre Removing tag: '$tag'")
        tags = tags.filter { it != tag }
        println("CatLog : UpdateOffre Tags after removal: ${tags.size}")
    }

    fun addExigence() {
        val trimmedExigence = currentExigenceInput.trim()
        println("CatLog : UpdateOffre Adding exigence: '$trimmedExigence'")
        if (trimmedExigence.isNotEmpty() && !exigences.contains(trimmedExigence)) {
            exigences = exigences + trimmedExigence
            currentExigenceInput = ""
            println("CatLog : UpdateOffre Exigence added, total: ${exigences.size}")
        } else {
            println("CatLog : UpdateOffre Exigence not added - empty or duplicate")
        }
    }

    fun removeExigence(exigence: String) {
        println("CatLog : UpdateOffre Removing exigence: '$exigence'")
        exigences = exigences.filter { it != exigence }
        println("CatLog : UpdateOffre Exigences after removal: ${exigences.size}")
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

    // Update validation on field changes
    LaunchedEffect(title) {
        titleError = validateTitle(title)
        println("CatLog : UpdateOffre Title validation - error: $titleError")
    }

    LaunchedEffect(description) {
        descriptionError = validateDescription(description)
        println("CatLog : UpdateOffre Description validation - error: $descriptionError")
    }

    LaunchedEffect(address) {
        addressError = validateAddress(address)
        println("CatLog : UpdateOffre Address validation - error: $addressError")
    }

    LaunchedEffect(city) {
        cityError = validateCity(city)
        println("CatLog : UpdateOffre City validation - error: $cityError")
    }

    LaunchedEffect(country) {
        countryError = validateCountry(country)
        println("CatLog : UpdateOffre Country validation - error: $countryError")
    }

    LaunchedEffect(expiresAt) {
        expiresAtError = validateExpiresAt(expiresAt)
        println("CatLog : UpdateOffre ExpiresAt validation - error: $expiresAtError")
    }

    // Auto-clear success message after 3 seconds and show snackbar
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            println("CatLog : UpdateOffre Showing success message: $successMessage")
            snackBarHostState?.showSnackbar(
                message = successMessage!!,
                duration = SnackbarDuration.Short
            )
            delay(3000)
            successMessage = null
            println("CatLog : UpdateOffre Success message cleared")
        }
    }

    // Auto-clear error message after 5 seconds and show snackbar
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            println("CatLog : UpdateOffre Showing error message: $errorMessage")
            snackBarHostState?.showSnackbar(
                message = errorMessage!!,
                duration = SnackbarDuration.Long
            )
            delay(5000)
            errorMessage = null
            println("CatLog : UpdateOffre Error message cleared")
        }
    }

    // Debug: Track scaffold composition
    println("CatLog : UpdateOffre Scaffold composition - isInitialLoading: $isInitialLoading")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Modifier l'offre",
                        color = TitleText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        println("CatLog : UpdateOffre Back button clicked")
                        navController.popBackStack()
                    }) {
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
            if (isInitialLoading) {
                // Loading indicator while fetching offer data
                println("CatLog : UpdateOffre Showing loading indicator")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Purple,
                            strokeWidth = 4.dp
                        )
                        Text(
                            "Chargement de l'offre...",
                            color = SoftText,
                            fontSize = 16.sp
                        )
                        Text(
                            "Execution #$launchedEffectExecutionCount",
                            color = SoftText,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                println("CatLog : UpdateOffre Showing form content")
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Success/Error messages
                    if (successMessage != null && snackBarHostState == null) {
                        SuccessMessage(message = successMessage!!)
                    }

                    if (errorMessage != null && snackBarHostState == null) {
                        ErrorMessage(message = errorMessage!!)
                    }

                    // Image upload section
//                    FormSection(title = "Images de l'offre") {
//                        // Horizontal LazyRow for selected images
//                        if (selectedImageUris.isNotEmpty()) {
//                            LazyRow(
//                                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                                modifier = Modifier.fillMaxWidth()
//                            ) {
//                                items(selectedImageUris.size) { index ->
//                                    Box(
//                                        modifier = Modifier
//                                            .size(120.dp)
//                                    ) {
//                                        Image(
//                                            bitmap = imageBitmaps[index].asImageBitmap(),
//                                            contentDescription = "Offer image $index",
//                                            modifier = Modifier
//                                                .size(120.dp)
//                                                .clip(RoundedCornerShape(12.dp)),
//                                            contentScale = ContentScale.Crop
//                                        )
//                                        // Remove button
//                                        IconButton(
//                                            onClick = { removeImage(index) },
//                                            modifier = Modifier
//                                                .align(Alignment.TopEnd)
//                                                .size(24.dp)
//                                                .background(Color.Red, CircleShape)
//                                        ) {
//                                            Icon(
//                                                Icons.Filled.Close,
//                                                contentDescription = "Remove image",
//                                                tint = Color.White,
//                                                modifier = Modifier.size(16.dp)
//                                            )
//                                        }
//                                    }
//                                }
//                            }
//                            Spacer(modifier = Modifier.height(8.dp))
//                        }
//
//                        // Add image button
//                        Row(
//                            Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.Center
//                        )
//                        {
//                            if (selectedImageUris.size < 5) {
//                                Box(
//                                    modifier = Modifier
//                                        .size(120.dp)
//                                        .clickable(
//                                            interactionSource = remember { MutableInteractionSource() },
//                                            indication = null
//                                        ) {
//                                            println("CatLog : UpdateOffre Add image button clicked")
//                                            galleryLauncher.launch("image/*")
//                                        },
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Column(
//                                        horizontalAlignment = Alignment.CenterHorizontally,
//                                        verticalArrangement = Arrangement.spacedBy(8.dp)
//                                    ) {
//                                        Icon(
//                                            Icons.Filled.PhotoCamera,
//                                            contentDescription = "Add image",
//                                            tint = SoftText,
//                                            modifier = Modifier.size(40.dp)
//                                        )
//                                        Text(
//                                            text = "Ajouter une image",
//                                            color = SoftText,
//                                            fontSize = 14.sp
//                                        )
//                                        Text(
//                                            text = "${selectedImageUris.size}/5",
//                                            color = SoftText,
//                                            fontSize = 12.sp
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }

                    // Form sections
                    FormSection(title = "Informations principales") {
                        FormTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = "Titre du poste *",
                            placeholder = "Ex: Développeur Android Senior",
                            icon = Icons.Filled.Title,
                            isRequired = true,
                            errorMessage = titleError
                        )

                        FormTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = "Description *",
                            placeholder = "Décrivez le poste, les missions principales...",
                            icon = Icons.Filled.Description,
                            isRequired = true,
                            maxLines = 4,
                            errorMessage = descriptionError
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
                                        TagChip(
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

                    FormSection(title = "Détails du poste") {
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
                                        println("CatLog : UpdateOffre JobType selected: JOB")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Stage") },
                                    onClick = {
                                        selectedJobType = JobType.STAGE
                                        jobTypeExpanded = false
                                        println("CatLog : UpdateOffre JobType selected: STAGE")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Freelance") },
                                    onClick = {
                                        selectedJobType = JobType.FREELANCE
                                        jobTypeExpanded = false
                                        println("CatLog : UpdateOffre JobType selected: FREELANCE")
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
                                        println("CatLog : UpdateOffre Shift selected: JOUR")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Nuit") },
                                    onClick = {
                                        selectedShift = Shift.NUIT
                                        shiftExpanded = false
                                        println("CatLog : UpdateOffre Shift selected: NUIT")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Flexible") },
                                    onClick = {
                                        selectedShift = Shift.FLEXIBLE
                                        shiftExpanded = false
                                        println("CatLog : UpdateOffre Shift selected: FLEXIBLE")
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
                                        TagChip(
                                            text = exigence,
                                            onRemove = { removeExigence(exigence) }
                                        )
                                    }
                                }
                            }
                        }

                        FormTextField(
                            value = salary,
                            onValueChange = { salary = it },
                            label = "Salaire",
                            placeholder = "Ex: 3000€ - 4000€ / mois",
                            icon = Icons.Filled.Euro,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )

                        FormTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = "Catégorie",
                            placeholder = "Ex: Informatique, Marketing, Vente...",
                            icon = Icons.Filled.Work
                        )

                        // Expiration date field
                        FormTextField(
                            value = expiresAt,
                            onValueChange = { expiresAt = it },
                            label = "Date d'expiration",
                            placeholder = "YYYY-MM-DD",
                            icon = Icons.Filled.Schedule,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            errorMessage = expiresAtError
                        )
                    }

                    FormSection(title = "Localisation") {
                        FormTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = "Adresse *",
                            placeholder = "Numéro et rue",
                            icon = Icons.Filled.LocationOn,
                            isRequired = true,
                            errorMessage = addressError
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FormTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = "Ville *",
                                placeholder = "Ex: Paris",
                                icon = null,
                                modifier = Modifier.weight(1f),
                                isRequired = true,
                                errorMessage = cityError
                            )

                            FormTextField(
                                value = country,
                                onValueChange = { country = it },
                                label = "Pays *",
                                placeholder = "Ex: France",
                                icon = null,
                                modifier = Modifier.weight(1f),
                                isRequired = true,
                                errorMessage = countryError
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

                    println("CatLog : UpdateOffre Form validation - isFormValid: $isFormValid")
                    println("CatLog : UpdateOffre Form validation details - titleError: $titleError, descError: $descriptionError, addrError: $addressError")
                    println("CatLog : UpdateOffre Form validation - cityError: $cityError, countryError: $countryError, currentUser: ${currentUser != null}")

                    Button(
                        onClick = {
                            println("CatLog : UpdateOffre Update button clicked - isFormValid: $isFormValid, isLoading: $isLoading")
                            if (isFormValid) {
                                isLoading = true
                                errorMessage = null
                                successMessage = null
                                println("CatLog : UpdateOffre Starting update process...")

                                scope.launch {
                                    try {
                                        // Get authentication token
                                        val authToken = token ?: getToken(context)
                                        println("CatLog : UpdateOffre Auth token: ${authToken?.take(10)}...")

                                        if (authToken.isNullOrEmpty()) {
                                            val authError = "Authentification requise. Veuillez vous connecter."
                                            errorMessage = authError
                                            println("CatLog : UpdateOffre Authentication error")
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
                                                println("CatLog : UpdateOffre Error processing image: ${e.message}")
                                            }
                                        }

                                        // Create location object (required in DTO)
                                        val locationObj = Location(
                                            address = address,
                                            city = city,
                                            country = country,
                                            coordinates = null
                                        )

                                        // Create UpdateOffreRequest object
                                        val updateOffreRequest = UpdateOffreRequest(
                                            title = title,
                                            description = description,
                                            location = locationObj,
                                            company = currentUser?.nom ?: "",
                                            tags = if (tags.isNotEmpty()) tags else null,
                                            exigences = if (exigences.isNotEmpty()) exigences else null,
                                            category = category.ifEmpty { null },
                                            salary = salary.ifEmpty { null },
                                            expiresAt = expiresAt.ifEmpty { null },
                                            jobType = selectedJobType,
                                            shift = selectedShift,
                                            isActive = true
                                        )

                                        println("CatLog : UpdateOffre Updating offer: $updateOffreRequest")

                                        // Call repository function to update offre
                                        val response = offreRepository.updateOffre(
                                            token = authToken,
                                            id = offerId,
                                            updateOffreRequest = updateOffreRequest,
                                        )

                                        println("CatLog >>>>: UpdateOffre Update response: $response")


                                        if (response.id != null) {
                                            val successMsg = "Offre modifiée avec succès!"
                                            successMessage = successMsg
                                            println("CatLog : UpdateOffre Update successful")
                                            snackBarHostState?.showSnackbar(
                                                message = successMsg,
                                                duration = SnackbarDuration.Short
                                            )

                                            // Navigate back after delay
                                            delay(2000)
                                            println("CatLog : UpdateOffre Navigating back after successful update")
                                            navController.popBackStack()

                                        } else {
                                            val errorMsg = response.message ?: "Erreur inconnue lors de la modification"
                                            errorMessage = errorMsg
                                            println("CatLog : UpdateOffre Update failed: $errorMsg")
                                            snackBarHostState?.showSnackbar(
                                                message = errorMsg,
                                                duration = SnackbarDuration.Long
                                            )
                                            println("CatLog : UpdateOffre Update errors: ${response.errors}")
                                        }

                                    } catch (e: Exception) {
                                        val errorMsg = when {
                                            e.message?.contains("401", true) == true -> "Authentification requise"
                                            e.message?.contains("403", true) == true -> "Vous n'avez pas les permissions nécessaires"
                                            e.message?.contains("422", true) == true -> "Données invalides"
                                            e.message?.contains("timeout", true) == true -> "Délai d'attente dépassé"
                                            e.message?.contains("unable to resolve", true) == true -> "Impossible de contacter le serveur"
                                            e.message?.contains("failed to connect", true) == true -> "Impossible de se connecter au serveur"
                                            else -> "Erreur lors de la modification: ${e.localizedMessage ?: "Veuillez réessayer"}"
                                        }
                                        errorMessage = errorMsg
                                        println("CatLog : UpdateOffre Update exception: ${e.message}")
                                        snackBarHostState?.showSnackbar(
                                            message = errorMsg,
                                            duration = SnackbarDuration.Long
                                        )
                                        println("CatLog : UpdateOffre ScreenUpdateOffre - Exception -> ${e.message}")
                                        e.printStackTrace()
                                    } finally {
                                        isLoading = false
                                        println("CatLog : UpdateOffre Update process completed, isLoading set to false")
                                    }
                                }
                            } else {
                                val validationError = "Veuillez corriger les erreurs dans le formulaire avant de soumettre"
                                errorMessage = validationError
                                println("CatLog : UpdateOffre Form validation failed")
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
                            Text("Modification en cours...")
                        } else {
                            Icon(
                                Icons.Filled.Add, // You might want to change this to an edit icon
                                contentDescription = "Modifier",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Modifier l'offre",
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
}

// Reuse the same helper composables from ScreenCreateOffre
// (TagChip, FormSection, FormTextField, SuccessMessage, ErrorMessage)

@Preview(showBackground = true)
@Composable
fun ScreenUpdateOffrePreview() {
    ScreenUpdateOffre(
        navController = rememberNavController(),
        offerId = "sample_offer_id"
    )
}