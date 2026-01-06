package sim2.app.talleb_5edma.interfaces

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sim2.app.talleb_5edma.Routes
import sim2.app.talleb_5edma.BottomDest
import sim2.app.talleb_5edma.models.JobType
import sim2.app.talleb_5edma.models.Offre
import sim2.app.talleb_5edma.models.User
import sim2.app.talleb_5edma.network.OffreRepository
import sim2.app.talleb_5edma.network.UserRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/* ====== Theme System ====== */
data class AppTheme(
    val primary: Color,
    val primaryDark: Color,
    val accent: Color,
    val bgTop: Color,
    val bgMid: Color,
    val chip: Color,
    val cardBorder: Color
)

// Theme definitions
private val OrganizationTheme = AppTheme(
    primary = Color(0xFF7C4DFF), // Violet Principal (Capture)
    primaryDark = Color(0xFF5E35B1), // Pourpre Foncé (Capture)
    // CORRECTION : Utilisation du Rose Fuchsia/Magenta pour l'accentuation
    accent = Color(0xFFFF0055), // Rose Fuchsia (Accent Capture)
    bgTop = Color(0xFFF7F4FF), // Lavande Claire (Capture)
    bgMid = Color(0xFFF1ECFF), // Mauve Pâle (Capture)
    chip = Color(0x66FFFFFF), // Chip en verre légèrement blanc (Capture)
    cardBorder = Color(0xFFE6DEFF) // Bordure d'accentuation (Capture)
)
private val CasualTheme = AppTheme(
    primary = Color(0xF3F8F6FA), // Green 0xF3765E88
    primaryDark = Color(0xFFF6F3F3), // Dark Green
    accent = Color(0xF3765E88), // Green 0xE4663280
    bgTop = Color(0xFFF8E7FC), // Light Green
    bgMid = Color(0xFFF5F1F6), // Medium Green
    chip = Color(0xFFF6F2F6), // Glass chip
    cardBorder = Color(0xFFEEE8EF) // Light Green border
)
private val ProfessionalTheme = AppTheme(
    primary = Color(0xFF57A2DE), // Blue
    primaryDark = Color(0xFF3B73AB), // Dark Blue
    accent = Color(0xE93F51B5), // Blue
    bgTop = Color(0xFFF4F9FF), // Light Blue
    bgMid = Color(0xFFECF5FF), // Medium Blue
    chip = Color(0x88FFFFFF), // Glass chip
    cardBorder = Color(0xFFDEEBFF) // Light Blue border
)

// Current theme state
class ThemeState {
    var currentTheme by mutableStateOf(OrganizationTheme)
}

/** État pour le type d'offre (Occasionnel / Professionnel) + thème associé */
class OfferTypeState(
    isProfessionalInit: Boolean = false,
    val themeState: ThemeState
) {
    // observable → recomposition quand on change
    var isProfessional by mutableStateOf(isProfessionalInit)
}

@Composable
fun rememberThemeState(): ThemeState = remember { ThemeState() }

@Composable
fun rememberOfferTypeState(
    themeState: ThemeState = rememberThemeState()
): OfferTypeState {
    return remember { OfferTypeState(themeState = themeState) }
}

/* ===================== State ===================== */
@Stable
class AccueilState(
    private val offreRepository: OffreRepository = OffreRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val coroutineScope: CoroutineScope
) {
    var query by mutableStateOf("")
    var list by mutableStateOf<List<Offre>>(emptyList())
    var loading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var currentUser by mutableStateOf<User?>(null)
    var onlyPopular by mutableStateOf(false)
    var onlyFavorites by mutableStateOf(false)
    var selectedType: JobType? by mutableStateOf(null)
    var selectedCity: String? by mutableStateOf(null)
    // Improved refresh trigger with throttle logic
    var refreshTrigger by mutableIntStateOf(0)
    private var lastRefreshTime by mutableLongStateOf(0L)
    private val refreshCooldown = 1000L // 1 second cooldown between refreshes
    val allCities: List<String>
        get() = list.mapNotNull { it.location?.city }.distinct()
    val filtered: List<Offre>
        get() = list
            .filter {
                if (query.isBlank()) true
                else it.title?.contains(query, true) == true ||
                        it.company?.contains(query, true) == true ||
                        it.location?.city?.contains(query, true) == true ||
                        it.tags?.any { tag -> tag.contains(query, true) } == true
            }
            .filter { selectedType?.let { t -> it.jobType == t } ?: true }
            .filter { selectedCity?.let { c -> it.location?.city == c } ?: true }
            .filter { if (onlyPopular) (it.viewCount ?: 0) >= 50 else true }
            .filter {
                if (onlyFavorites) {
                    currentUser?.likedOffres?.contains(it.id) == true
                } else true
            }
    // Smart refresh function that respects cooldown
    private fun safeRefresh() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRefreshTime > refreshCooldown) {
            refreshTrigger++
            lastRefreshTime = currentTime
            println("AccueilState - Safe refresh triggered (cooldown respected)")
        } else {
            println("AccueilState - Refresh skipped (cooldown active, ${currentTime - lastRefreshTime}ms since last refresh)")
        }
    }
    // Helper function to check if an offer is liked by current user
    fun isOfferLiked(offerId: String?): Boolean {
        return if (offerId != null && currentUser?.likedOffres != null) {
            currentUser!!.likedOffres!!.contains(offerId)
        } else {
            false
        }
    }
    // Load current user data
    fun loadCurrentUser(token: String) {
        coroutineScope.launch {
            try {
                println("AccueilState - Loading current user...")
                val response = userRepository.getCurrentUser(token)
                // Extract the user from the response - try data field first, then direct fields
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
                println("AccueilState - Loaded current user: ${currentUser?.nom}, isOrganization: ${currentUser?.isOrganization}")
                // Load liked offers separately
                loadUserLikedOffres(token)
            } catch (e: Exception) {
                println("AccueilState - Error loading current user: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    // Load user's liked offers separately
    private fun loadUserLikedOffres(token: String) {
        coroutineScope.launch {
            try {
                println("AccueilState - Loading user liked offers...")
                val likedResponse = userRepository.getLikedOffres(token)
                currentUser = currentUser?.copy(likedOffres = likedResponse.likedOffres)
                println("AccueilState - Loaded user liked offers: ${likedResponse.likedOffres?.size}")
                // Force UI refresh after loading liked offers
                safeRefresh()
            } catch (e: Exception) {
                println("AccueilState - Error loading liked offers: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    fun loadOffres() {
        println("AccueilState - loadOffres called")
        loading = true
        error = null
        coroutineScope.launch {
            try {
                println("AccueilState - Fetching offers from repository...")
                list = offreRepository.getAllActiveOffers()
                println("AccueilState - Successfully loaded ${list.size} offers")
                // Use safe refresh instead of direct increment
                safeRefresh()
            } catch (e: Exception) {
                error = "Erreur de chargement: ${e.message}"
                println("AccueilState - Error loading offers: ${e.message}")
                e.printStackTrace()
                list = emptyList()
            } finally {
                loading = false
                println("AccueilState - Loading completed, error: $error")
            }
        }
    }
    fun refreshOffres() {
        println("AccueilState - refreshOffres called")
        loadOffres()
    }
    fun toggleLike(token: String, offreId: String) {
        println("AccueilState - toggleLike called with offreId: $offreId")
        coroutineScope.launch {
            try {
                val response = offreRepository.toggleLikeOffre(token, offreId)
                if (response.liked != null) {
                    // Update the local list with the updated like count
                    list = list.map { offer ->
                        if (offer.id == offreId) {
                            offer.copy(
                                likeCount = response.likeCount ?: offer.likeCount
                            )
                        } else {
                            offer
                        }
                    }
                    // Update current user's likedOffres
                    currentUser = if (response.liked) {
                        // Add to likedOffres
                        val newLikedOffres = (currentUser?.likedOffres ?: emptyList()) + offreId
                        currentUser?.copy(likedOffres = newLikedOffres)
                    } else {
                        // Remove from likedOffres
                        val newLikedOffres = currentUser?.likedOffres?.filter { it != offreId } ?: emptyList()
                        currentUser?.copy(likedOffres = newLikedOffres)
                    }
                    println("AccueilState - Like toggled successfully: ${response.liked}")
                    println("AccueilState - Updated likedOffres: ${currentUser?.likedOffres}")
                    println("AccueilState - Updated like count for offer $offreId: ${response.likeCount}")
                    // Use safe refresh instead of direct increment
                    safeRefresh()
                }
            } catch (e: Exception) {
                error = "Erreur de like: ${e.message}"
                println("AccueilState - Error toggling like: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    fun searchOffres(query: String) {
        println("AccueilState - searchOffres called with query: '$query'")
        if (query.isBlank()) {
            println("AccueilState - Query is blank, loading all offers")
            loadOffres()
            return
        }
        loading = true
        error = null
        coroutineScope.launch {
            try {
                println("AccueilState - Searching offers with query: '$query'")
                list = offreRepository.searchOffers(query)
                println("AccueilState - Search completed, found ${list.size} offers")
                // Use safe refresh instead of direct increment
                safeRefresh()
            } catch (e: Exception) {
                error = "Erreur de recherche: ${e.message}"
                println("AccueilState - Error searching offers: ${e.message}")
                e.printStackTrace()
                list = emptyList()
            } finally {
                loading = false
                println("AccueilState - Search loading completed")
            }
        }
    }
    fun clearFilters() {
        println("AccueilState - clearFilters called")
        selectedType = null
        selectedCity = null
        onlyPopular = false
        onlyFavorites = false
        query = ""
        loadOffres()
    }
    // New method to load user's liked offers
    fun loadLikedOffres(token: String) {
        println("AccueilState - loadLikedOffres called")
        loading = true
        error = null
        coroutineScope.launch {
            try {
                list = offreRepository.getLikedOffers(token)
                println("AccueilState - Successfully loaded ${list.size} liked offers")
                // Use safe refresh instead of direct increment
                safeRefresh()
            } catch (e: Exception) {
                error = "Erreur de chargement des favoris: ${e.message}"
                println("AccueilState - Error loading liked offers: ${e.message}")
                list = emptyList()
            } finally {
                loading = false
            }
        }
    }
    // New method to load user's own offers
    fun loadMyOffres(token: String) {
        println("AccueilState - loadMyOffres called")
        loading = true
        error = null
        coroutineScope.launch {
            try {
                list = offreRepository.getMyOffers(token)
                println("AccueilState - Successfully loaded ${list.size} user offers")
                // Use safe refresh instead of direct increment
                safeRefresh()
            } catch (e: Exception) {
                error = "Erreur de chargement de vos offres: ${e.message}"
                println("AccueilState - Error loading user offers: ${e.message}")
                list = emptyList()
            } finally {
                loading = false
            }
        }
    }
    // Helper method to refresh user data
    fun refreshUserData(token: String) {
        coroutineScope.launch {
            loadCurrentUser(token)
        }
    }
    // Force UI refresh with throttle
    fun forceRefresh() {
        safeRefresh()
    }
    // Add this method to your AccueilState class in Accueil.kt
    fun deleteOffre(token: String, offreId: String) {
        println("AccueilState - deleteOffre called with offreId: $offreId")
        coroutineScope.launch {
            try {
                val response = offreRepository.deleteOffre(token, offreId)
                println("AccueilState - Delete response: ${response.message}")
                // Remove the deleted offer from the local list
                list = list.filter { it.id != offreId }
                println("AccueilState - Offer deleted successfully")
            } catch (e: Exception) {
                error = "Erreur de suppression: ${e.message}"
                println("AccueilState - Error deleting offer: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}

@Composable
fun rememberAccueilState(coroutineScope: CoroutineScope): AccueilState = remember {
    println("CatLog: AccueilState - rememberAccueilState created")
    AccueilState(coroutineScope = coroutineScope)
}

/* ===================== Main Screen ===================== */
@Composable
fun AccueilScreen(
    navController: NavHostController,
    token: String? = null,
    isProfessionalMode: Boolean = false,
    onProfessionalModeChange: (Boolean) -> Unit = {},
    onOpenFilter: () -> Unit = {},
    onShareOffer: (Offre) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val state = rememberAccueilState(coroutineScope = coroutineScope)
    val offerTypeState = rememberOfferTypeState()
    
    // Sync local state with passed state
    LaunchedEffect(isProfessionalMode) {
        offerTypeState.isProfessional = isProfessionalMode
    }
    
    // Sync back when switch changes
    LaunchedEffect(offerTypeState.isProfessional) {
        onProfessionalModeChange(offerTypeState.isProfessional)
    }
    val screenType = if (state.currentUser?.isOrganization == true) "my" else "all"
    // Load current user if token is available
    println("CatLog: AccueilScreen - Composed with token: ${token?.take(10)}..., screenType: $screenType")
    LaunchedEffect(token) {
        token?.let {
            println("CatLog: AccueilScreen - Loading current user with token")
            state.loadCurrentUser(it)
        }
    }
    // REDIRECTION : Si l'utilisateur est une organisation, on le redirige vers l'accueil entreprise
    LaunchedEffect(state.currentUser) {
        if (state.currentUser?.isOrganization == true) {
            println("CatLog: AccueilScreen - User is organization, redirecting to EntrepriseHomeScreen")
            navController.navigate(Routes.ScreenHomeEntreprise) {
                // On s'assure qu'on ne peut pas revenir en arrière sur l'accueil job-seeker
                popUpTo(BottomDest.Home.route) { inclusive = true }
            }
        }
    }
    // Update theme based on offer type
    LaunchedEffect(offerTypeState.isProfessional, state.currentUser?.isOrganization) {
        val newTheme = when {
            state.currentUser?.isOrganization == true -> OrganizationTheme
            offerTypeState.isProfessional -> ProfessionalTheme
            else -> CasualTheme
        }
        offerTypeState.themeState.currentTheme = newTheme
        println("CatLog: AccueilScreen - Theme updated: ${newTheme.accent}")
    }
    // Load offers based on screen type
    LaunchedEffect(screenType, token, state.refreshTrigger) {
        println("CatLog: AccueilScreen - LaunchedEffect triggered with screenType: $screenType, refreshTrigger: ${state.refreshTrigger}")
        when (screenType) {
            "liked" -> {
                if (token != null) {
                    state.loadLikedOffres(token)
                } else {
                    state.error = "Authentication required for liked offers"
                }
            }
            "my" -> {
                if (token != null) {
                    state.loadMyOffres(token)
                } else {
                    state.error = "Authentication required for your offers"
                }
            }
            else -> state.loadOffres()
        }
    }
    AccueilContent(
        navController = navController,
        token = token,
        state = state,
        screenType = screenType,
        offerTypeState = offerTypeState,
        onOpenFilter = onOpenFilter,
        onOfferClick = { offerId ->
            println("CatLog: AccueilScreen - Navigating to offer details: $offerId")
            navController.navigate("${Routes.ScreenOffre}/$offerId")
        },
        onShareOffer = onShareOffer
    )
}

@Composable
fun AccueilContent(
    navController: NavHostController,
    token: String? = null,
    state: AccueilState = rememberAccueilState(coroutineScope = rememberCoroutineScope()),
    screenType: String = "all",
    offerTypeState: OfferTypeState = rememberOfferTypeState(),
    onOpenFilter: () -> Unit = {},
    onOfferClick: (String) -> Unit = {},
    onShareOffer: (Offre) -> Unit = {}
) {
    val context = LocalContext.current
    val theme = offerTypeState.themeState.currentTheme
    // ====== Filtre logique selon Occasionnel / Professionnel ======
    val displayedOffers: List<Offre> =
        if (screenType == "all" && state.currentUser?.isOrganization == false) {
            if (offerTypeState.isProfessional) {
                // Professionnel -> Stage + Freelance
                state.filtered.filter {
                    it.jobType == JobType.STAGE || it.jobType == JobType.FREELANCE
                }
            } else {
                // Occasionnel -> uniquement Job
                state.filtered.filter { it.jobType == JobType.JOB }
            }
        } else {
            // Pour les organisations ou autres écrans on garde le filtre normal
            state.filtered
        }
    println("CatLog: AccueilContent - Composed, screenType: $screenType, loading: ${state.loading}, error: ${state.error}, offers count: ${state.list.size}, filtered: ${state.filtered.size}, displayed: ${displayedOffers.size}")
    println("CatLog: AccueilContent - Current user: ${state.currentUser?.nom}, isOrganization: ${state.currentUser?.isOrganization}")
    println("CatLog: AccueilContent - Current theme accent: ${theme.accent}")
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to theme.bgTop,
                    0.7f to theme.bgMid,
                    1f to Color.White
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            /* ====== Screen Title ====== */
            item {
                when (screenType) {
                    "liked" -> ScreenTitle("Vos offres favorites", theme)
                    "my" -> {
                        if (state.currentUser?.isOrganization == true) {
                            ScreenTitle("Nos offres", theme)
                        } else {
                            ScreenTitle("Vos offres publiées", theme)
                        }
                    }
                   // else -> ScreenTitle("les offres", theme)
                }
            }
            /* ====== Search Bar ====== */
            if (screenType == "all") {
                item {
                    println("CatLog: AccueilContent - Rendering SearchBar")
                    SearchBar(
                        query = state.query,
                        onQueryChange = { newQuery ->
                            println("CatLog: AccueilContent - Search query changed to: '$newQuery'")
                            state.query = newQuery
                            if (newQuery.isNotBlank()) {
                                state.searchOffres(newQuery)
                            } else {
                                state.loadOffres()
                            }
                        },
                        onSearch = {
                            println("CatLog: AccueilContent - Manual search triggered")
                            state.searchOffres(state.query)
                        },
                        theme = theme
                    )
                }
            }
            /* ====== Loading State ====== */
            if (state.loading && state.list.isEmpty()) {
                item {
                    println("CatLog: AccueilContent - Rendering LoadingIndicator")
                    LoadingIndicator(theme)
                }
            }
            /* ====== Error State ====== */
            state.error?.let { errorMessage ->
                item {
                    println("CatLog: AccueilContent - Rendering ErrorMessage: $errorMessage")
                    ErrorMessage(
                        message = errorMessage,
                        onRetry = {
                            println("CatLog: AccueilContent - Retry button clicked")
                            when (screenType) {
                                "liked" -> token?.let { state.loadLikedOffres(it) }
                                "my" -> token?.let { state.loadMyOffres(it) }
                                else -> state.loadOffres()
                            }
                        },
                        theme = theme
                    )
                }
            }
            /* ====== Filters Row ====== */
            if (screenType == "all") {
                item {
                    println("CatLog: AccueilContent - Rendering FiltersAndChipsRow")
                    FiltersAndChipsRow(
                        state = state,
                        onOpenFilter = onOpenFilter,
                        theme = theme
                    )
                }
            }
            /* ====== New Opportunities Banner ====== */
            if (screenType == "all") {
                item {
                    println("CatLog: AccueilContent - Rendering NewOpportunitiesBanner with ${displayedOffers.size} offers")
                    NewOpportunitiesBanner(
                        count = displayedOffers.size,
                        onRefresh = {
                            println("CatLog: AccueilContent - Refresh banner clicked")
                            state.refreshOffres()
                        },
                        theme = theme
                    )
                }
            }
            /* ====== Offer Type Switch for Regular Users (centered under title) ====== */
            if (screenType == "all") {
                item {
                    if (state.currentUser?.isOrganization == false) {
                        println("CatLog: AccueilContent - Rendering OfferTypeSwitch for regular user")
                        OfferTypeSwitch(
                            offerTypeState = offerTypeState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            theme = theme
                        )
                    }
                }
            }
            /* ====== Offers List ====== */
            if (displayedOffers.isNotEmpty()) {
                println("CatLog: AccueilContent - Rendering ${displayedOffers.size} offers")
                items(displayedOffers, key = { it.id ?: "" }) { offer ->
                    println("CatLog: AccueilContent - Rendering JobCard for: ${offer.title}")
                    // Check if offer is liked by current user using the state method
                    val isLiked = state.isOfferLiked(offer.id)
                    val likeCount = offer.likeCount ?: 0
                    println("CatLog: AccueilContent - Offer ${offer.id}: isLiked=$isLiked, likeCount=$likeCount")
                    JobCard(
                        offer = offer,
                        isLiked = isLiked,
                        likeCount = likeCount,
                        onLike = {
                            println("CatLog: AccueilContent - Like clicked for offer: ${offer.title} (ID: ${offer.id})")
                            token?.let {
                                state.toggleLike(it, offer.id ?: "")
                            } ?: run {
                                println("CatLog: AccueilContent - Cannot like: token is null")
                            }
                        },
                        onShare = {
                            println("CatLog: AccueilContent - Share clicked for offer: ${offer.title}")
                            onShareOffer(offer)
                        },
                        onClick = {
                            println("CatLog: AccueilContent - Offer clicked: ${offer.title}")
                            offer.id?.let { onOfferClick(it) }
                        },
                        showLikeButton = token != null,
                        screenType = screenType,
                        onDelete = {
                            println("CatLog: AccueilContent - Delete clicked for offer: ${offer.title}")
                            token?.let {
                                state.deleteOffre(it, offer.id ?: "")
                            } ?: run {
                                println("CatLog: AccueilContent - Cannot delete: token is null")
                            }
                        },
                        onUpdate = {
                            println("CatLog: AccueilContent - Update clicked for offer: ${offer.title}")
                            offer.id?.let { offerId ->
                                navController.navigate("${Routes.ScreenUpdateOffre}/$offerId")
                            }
                        },
                        theme = theme
                    )
                }
            } else if (!state.loading && state.error == null) {
                /* ====== Empty State ====== */
                item {
                    println("CatLog: AccueilContent - Rendering EmptyState")
                    EmptyState(
                        screenType = screenType,
                        onRefresh = {
                            println("CatLog: AccueilContent - EmptyState refresh clicked")
                            when (screenType) {
                                "liked" -> token?.let { state.loadLikedOffres(it) }
                                "my" -> token?.let { state.loadMyOffres(it) }
                                else -> state.refreshOffres()
                            }
                        },
                        onClearFilters = {
                            println("CatLog: AccueilContent - EmptyState clear filters clicked")
                            state.clearFilters()
                        },
                        theme = theme
                    )
                }
            }
        }
        /* ====== Pull to Refresh Indicator ====== */
        if (state.loading && state.list.isNotEmpty()) {
            println("CatLog: AccueilContent - Showing refresh indicator")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = theme.accent,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

/* ====== Offer Type Switch ====== */
@Composable
private fun OfferTypeSwitch(
    offerTypeState: OfferTypeState,
    modifier: Modifier = Modifier,
    theme: AppTheme
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Type d'offre",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TitleText,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = SwitchBackground,
                    shape = RoundedCornerShape(50.dp)
                )
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .background(
                        color = if (!offerTypeState.isProfessional) theme.accent else Color.Transparent,
                        shape = RoundedCornerShape(50.dp)
                    )
                    .clickable {
                        println("CatLog: OfferTypeSwitch - Occasionnel selected")
                        offerTypeState.isProfessional = false
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Occasionnel",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (!offerTypeState.isProfessional) Color.White else SoftGray
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .background(
                        color = if (offerTypeState.isProfessional) theme.accent else Color.Transparent,
                        shape = RoundedCornerShape(50.dp)
                    )
                    .clickable {
                        println("CatLog: OfferTypeSwitch - Professionnel selected")
                        offerTypeState.isProfessional = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Professionnel",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (offerTypeState.isProfessional) Color.White else SoftGray
                )
            }
        }
    }
}

/* ====== Screen Title ====== */
@Composable
private fun ScreenTitle(title: String, theme: AppTheme) {
    Text(
        text = title,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = TitleText,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
    )
}

/* ====== Search Bar ====== */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    theme: AppTheme
) {
    println("CatLog: SearchBar - Composed with query: '$query'")
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        placeholder = {
            Text(
                "Rechercher un job, stage, freelance…",
                color = SoftText
            )
        },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = null, tint = theme.accent)
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = {
                    println("CatLog: SearchBar - Clear button clicked")
                    onQueryChange("")
                }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Effacer", tint = SoftText)
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = theme.cardBorder,
            unfocusedBorderColor = theme.cardBorder,
            cursorColor = theme.accent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

/* ====== Loading Indicator ====== */
@Composable
private fun LoadingIndicator(theme: AppTheme) {
    println("CatLog: LoadingIndicator - Composed")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = theme.accent,
                strokeWidth = 4.dp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Chargement des offres...",
                color = SoftText,
                fontSize = 14.sp
            )
        }
    }
}

/* ====== Error Message ====== */
@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    theme: AppTheme
) {
    println("CatLog: ErrorMessage - Composed with message: $message")
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFE0E0),
        border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Error,
                    contentDescription = null,
                    tint = Color.Red
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Erreur",
                    color = Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                color = Color.Red,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    println("CatLog: ErrorMessage - Retry button clicked")
                    onRetry()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.accent
                ),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Réessayer")
            }
        }
    }
}

/* ====== Filters and Chips Row ====== */
@Composable
private fun FiltersAndChipsRow(
    state: AccueilState,
    onOpenFilter: () -> Unit,
    theme: AppTheme
) {
    println("CatLog: FiltersAndChipsRow - Composed, selectedType: ${state.selectedType}, selectedCity: ${state.selectedCity}, onlyPopular: ${state.onlyPopular}, onlyFavorites: ${state.onlyFavorites}")
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dropdown Type
            DropdownFilter(
                label = "Tous les types",
                current = when (state.selectedType) {
                    JobType.JOB -> "Job"
                    JobType.STAGE -> "Stage"
                    JobType.FREELANCE -> "Freelance"
                    null -> "Tous les types"
                },
                items = listOf("Tous les types", "Job", "Stage", "Freelance"),
                onSelectedIndex = { index ->
                    val newType = when (index) {
                        0 -> null
                        1 -> JobType.JOB
                        2 -> JobType.STAGE
                        3 -> JobType.FREELANCE
                        else -> null
                    }
                    println("CatLog: FiltersAndChipsRow - Type filter changed to: $newType")
                    state.selectedType = newType
                },
                theme = theme
            )
            // Dropdown Ville
            val cityItems = listOf("Toutes les villes") + state.allCities
            println("CatLog: FiltersAndChipsRow - Available cities: $cityItems")
            DropdownFilter(
                label = "Toutes les villes",
                current = state.selectedCity ?: "Toutes les villes",
                items = cityItems,
                onSelectedIndex = { index ->
                    val newCity = if (index == 0) null else cityItems[index]
                    println("CatLog: FiltersAndChipsRow - City filter changed to: $newCity")
                    state.selectedCity = newCity
                },
                theme = theme
            )
            // Chip Filtre (ouvre page filtre)
            GlassChip("Filtre", Icons.Filled.FilterList, theme) {
                println("CatLog: FiltersAndChipsRow - Filter chip clicked")
                onOpenFilter()
            }
            // Chip Populaire
            ToggleGlassChip("Populaire", state.onlyPopular, theme) { newValue ->
                println("CatLog: FiltersAndChipsRow - Popular filter changed to: $newValue")
                state.onlyPopular = newValue
            }
            // Chip Favorites
            ToggleGlassChip("Favorites", state.onlyFavorites, theme) { newValue ->
                println("CatLog: FiltersAndChipsRow - Favorites filter changed to: $newValue")
                state.onlyFavorites = newValue
            }
            // Clear filters
            if (state.selectedType != null || state.selectedCity != null || state.onlyPopular || state.onlyFavorites) {
                GlassChip("Effacer", Icons.Filled.Clear, theme) {
                    println("CatLog: FiltersAndChipsRow - Clear filters clicked")
                    state.clearFilters()
                }
            }
        }
    }
}

/* ====== New Opportunities Banner ====== */
@Composable
private fun NewOpportunitiesBanner(
    count: Int,
    onRefresh: () -> Unit,
    theme: AppTheme
) {
    println("CatLog: NewOpportunitiesBanner - Composed with count: $count")
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = theme.accent,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                println("CatLog: NewOpportunitiesBanner - Banner clicked")
                onRefresh()
            }
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Nouvelles opportunités",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    "$count offres pour vous",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
            }
            Icon(
                Icons.Filled.Refresh,
                contentDescription = "Actualiser",
                tint = Color.White
            )
        }
    }
}

/* ====== Empty State ====== */
@Composable
private fun EmptyState(
    screenType: String = "all",
    onRefresh: () -> Unit,
    onClearFilters: () -> Unit,
    theme: AppTheme
) {
    println("CatLog: EmptyState - Composed for screenType: $screenType")
    val (title, description) = when (screenType) {
        "liked" -> "Aucune offre favorite" to "Les offres que vous aimez apparaîtront ici"
        "my" -> "Aucune offre publiée" to "Créez votre première offre pour commencer"
        else -> "Aucune offre trouvée" to "Essayez de modifier vos filtres ou d'actualiser la liste"
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.SearchOff,
                contentDescription = null,
                tint = SoftText,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                title,
                color = TitleText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                description,
                color = SoftText,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = {
                    println("CatLog: EmptyState - Refresh button clicked")
                    onRefresh()
                }) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Actualiser")
                }
                if (screenType == "all") {
                    Button(
                        onClick = {
                            println("CatLog: EmptyState - Clear filters button clicked")
                            onClearFilters()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.accent)
                    ) {
                        Icon(Icons.Filled.ClearAll, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Effacer les filtres")
                    }
                }
            }
        }
    }
}

/* ====== Job Card ====== */
@Composable
private fun JobCard(
    offer: Offre,
    isLiked: Boolean,
    likeCount: Int,
    onLike: () -> Unit,
    onShare: () -> Unit,
    onClick: () -> Unit,
    showLikeButton: Boolean = true,
    screenType: String = "all", // Add screenType parameter
    onDelete: (() -> Unit)? = null, // Add onDelete callback
    onUpdate: (() -> Unit)? = null, // Add onUpdate callback
    theme: AppTheme
) {
    println("CatLog: JobCard - Composed for offer: ${offer.title}, isLiked: $isLiked, likeCount: $likeCount, offerId: ${offer.id}, screenType: $screenType")
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                println("CatLog: JobCard - Card clicked for: ${offer.title}")
                onClick()
            },
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Column {
            // Top border with theme color
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(theme.accent)
            )
            Column(Modifier.padding(16.dp)) {
                // Title and Type
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        offer.title ?: "Titre non disponible",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TitleText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    offer.jobType?.let { jobType ->
                        JobTypeBadge(jobType, theme)
                    }
                }
                Spacer(Modifier.height(6.dp))
                // Company
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CompanyAvatar(offer.company ?: "Company")
                    Spacer(Modifier.width(8.dp))
                    Text(
                        offer.company ?: "Entreprise non spécifiée",
                        fontSize = 14.sp,
                        color = SoftText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                // Location and Salary
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        offer.location?.city ?: "Ville non spécifiée",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(12.dp))
                    Icon(
                        Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        offer.salary ?: "Salaire non spécifié",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                // Description
                Text(
                    offer.description ?: "Description non disponible",
                    fontSize = 13.sp,
                    color = Color(0xFF707070),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                // Tags
                offer.tags?.takeIf { it.isNotEmpty() }?.let { tags ->
                    Spacer(Modifier.height(8.dp))
                    TagsRow(tags = tags, theme = theme)
                }
                Spacer(Modifier.height(10.dp))
                // Footer with date and actions
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date and views
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            formatDate(offer.createdAt),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        offer.viewCount?.takeIf { it > 0 }?.let { views ->
                            Spacer(Modifier.width(12.dp))
                            Icon(
                                Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "$views",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    // Conditionally show Delete/Update or Like/Share buttons based on screenType
                    if (screenType == "my") {
                        // Delete and Update buttons for "my" screen type
                        Row {
                            // Update button
                            IconButton(
                                onClick = {
                                    println("CatLog: JobCard - Update button clicked for: ${offer.title}")
                                    onUpdate?.invoke()
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Modifier",
                                    tint = theme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(4.dp))
                            // Delete button
                            IconButton(
                                onClick = {
                                    println("CatLog: JobCard - Delete button clicked for: ${offer.title}")
                                    onDelete?.invoke()
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Supprimer",
                                    tint = theme.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        // Like and Share buttons for other screen types
                        Row {
                            // Like button with count (only show if user is authenticated)
                            if (showLikeButton) {
                                Box {
                                    IconButton(
                                        onClick = {
                                            println("CatLog: JobCard - Like button clicked for: ${offer.title}, current isLiked: $isLiked")
                                            onLike()
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isLiked)
                                                    Icons.Filled.Favorite
                                                else
                                                    Icons.Filled.FavoriteBorder,
                                                contentDescription = "Aimer",
                                                tint = if (isLiked) theme.accent else Color.Gray,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            if (likeCount > 0) {
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = if (likeCount > 99) "99+" else "$likeCount",
                                                    color = if (isLiked) theme.accent else Color.Gray,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.width(4.dp))
                            // Share button
                            IconButton(
                                onClick = {
                                    println("CatLog: JobCard - Share button clicked for: ${offer.title}")
                                    onShare()
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Filled.IosShare,
                                    contentDescription = "Partager",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ====== Tags Row ====== */
@Composable
private fun TagsRow(tags: List<String>, theme: AppTheme) {
    println("CatLog: TagsRow - Composed with tags: $tags")
    val visibleTags = tags.take(3)
    val remainingCount = tags.size - 3
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        visibleTags.forEach { tag ->
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = theme.bgMid,
                border = BorderStroke(1.dp, theme.cardBorder)
            ) {
                Text(
                    text = tag,
                    color = theme.primaryDark,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        if (remainingCount > 0) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = theme.chip
            ) {
                Text(
                    text = "+$remainingCount",
                    color = SoftText,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/* ====== Job Type Badge ====== */
@Composable
private fun JobTypeBadge(type: JobType, theme: AppTheme) {
    println("CatLog: JobTypeBadge - Composed for type: $type")
    val (text, color) = when (type) {
        JobType.JOB -> "Job" to Color(0xFF00C853)
        JobType.STAGE -> "Stage" to Color(0xFF2962FF)
        JobType.FREELANCE -> "Freelance" to Color(0xFFFFA000)
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/* ====== Company Avatar ====== */
@Composable
private fun CompanyAvatar(seedText: String) {
    println("CatLog: CompanyAvatar - Composed for seed: $seedText")
    val colors = listOf(
        0xFFFFCDD2, 0xFFFFE0B2, 0xFFFFF9C4,
        0xFFC8E6C9, 0xFFBBDEFB, 0xFFE1BEE7, 0xFFD7CCC8
    ).map { Color(it) }
    val idx = abs(seedText.hashCode()) % colors.size
    val bg = colors[idx]
    val initials = seedText
        .split(" ", ".", "-")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .take(2)
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initials,
            fontWeight = FontWeight.Bold,
            color = TitleText.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
    }
}

/* ====== Dropdown Filter ====== */
@Composable
private fun DropdownFilter(
    label: String,
    current: String,
    items: List<String>,
    onSelectedIndex: (Int) -> Unit,
    theme: AppTheme
) {
    var expanded by remember { mutableStateOf(false) }
    println("CatLog: DropdownFilter - Composed with current: '$current', items: $items")
    Box {
        Surface(
            modifier = Modifier
                .height(40.dp)
                .clickable {
                    println("CatLog: DropdownFilter - Dropdown clicked, expanding")
                    expanded = true
                },
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, theme.cardBorder),
            color = Color.White,
            shadowElevation = 0.dp
        ) {
            Row(
                Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = current,
                    fontSize = 13.sp,
                    color = TitleText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = SoftText
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                println("CatLog: DropdownFilter - Dropdown dismissed")
                expanded = false
            }
        ) {
            items.forEachIndexed { index, text ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text,
                            color = if (text == current) theme.primary else TitleText,
                            fontWeight = if (text == current) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        println("CatLog: DropdownFilter - Item selected: '$text' at index: $index")
                        expanded = false
                        onSelectedIndex(index)
                    }
                )
            }
        }
    }
}

/* ====== Glass Chips ====== */
@Composable
private fun GlassChip(text: String, icon: ImageVector? = null, theme: AppTheme, onClick: () -> Unit) {
    println("CatLog: GlassChip - Composed: $text")
    Surface(
        onClick = {
            println("CatLog: GlassChip - Clicked: $text")
            onClick()
        },
        shape = RoundedCornerShape(24.dp),
        color = theme.chip,
        border = BorderStroke(1.dp, theme.cardBorder),
        shadowElevation = 0.dp
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, null, Modifier.size(16.dp), tint = theme.primaryDark)
                Spacer(Modifier.width(6.dp))
            }
            Text(text, fontSize = 13.sp, color = TitleText)
        }
    }
}

@Composable
private fun ToggleGlassChip(text: String, checked: Boolean, theme: AppTheme, onChange: (Boolean) -> Unit) {
    println("CatLog: ToggleGlassChip - Composed: $text, checked: $checked")
    Surface(
        onClick = { onChange(!checked) },
        shape = RoundedCornerShape(24.dp),
        color = if (checked) Color(0xFFEDE4FF) else theme.chip,
        border = BorderStroke(1.dp, if (checked) theme.primary else theme.cardBorder)
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (checked) {
                Box(
                    Modifier
                        .size(16.dp)
                        .background(theme.primary.copy(alpha = .15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        null,
                        Modifier.size(12.dp),
                        tint = theme.primary
                    )
                }
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text,
                fontSize = 13.sp,
                color = if (checked) theme.primary else TitleText
            )
        }
    }
}

/* ====== Utility Functions ====== */
private fun formatDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "Date inconnue"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        "Date inconnue"
    }
}

// Keep the existing color constants for compatibility
private val TitleText = Color(0xFF1F1B2E)
private val SoftText = Color(0xFF6F6B80)
private val SoftGray = Color(0xFF9CA3AF)
private val SwitchBackground = Color(0xFFF3F4F7)