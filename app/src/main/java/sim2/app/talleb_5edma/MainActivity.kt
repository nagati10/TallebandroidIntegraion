package sim2.app.talleb_5edma

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sim2.app.talleb_5edma.network.WebSocketCallManager
import sim2.app.talleb_5edma.interfaces.AccueilScreen
import sim2.app.talleb_5edma.interfaces.EntrepriseHomeScreen
import sim2.app.talleb_5edma.interfaces.AvailabilityScreen
import sim2.app.talleb_5edma.interfaces.CalendarScreen
import sim2.app.talleb_5edma.interfaces.ExamModeScreen
import sim2.app.talleb_5edma.interfaces.FilterScreen
import sim2.app.talleb_5edma.interfaces.LocationPickerMapScreen
import sim2.app.talleb_5edma.interfaces.MapScreen
import sim2.app.talleb_5edma.interfaces.PreferenceWizardScreen
import sim2.app.talleb_5edma.interfaces.QrScreen
import sim2.app.talleb_5edma.interfaces.TimeScreen
import sim2.app.talleb_5edma.models.User
import sim2.app.talleb_5edma.network.InterviewInvitationRepository
import sim2.app.talleb_5edma.network.InterviewWebSocketManager
import sim2.app.talleb_5edma.network.UserRepository
import sim2.app.talleb_5edma.screens.CVAnalysisScreen
import sim2.app.talleb_5edma.screens.ChatScreen
import sim2.app.talleb_5edma.screens.ChatTestScreen
import sim2.app.talleb_5edma.screens.DisponibiliteFormScreen
import sim2.app.talleb_5edma.screens.DisponibilitesScreen
import sim2.app.talleb_5edma.screens.EditProfileScreen
import sim2.app.talleb_5edma.screens.EvenementFormScreen
import sim2.app.talleb_5edma.screens.EvenementsScreen
import sim2.app.talleb_5edma.screens.ForgotPasswordScreen
import sim2.app.talleb_5edma.screens.LoginScreen
import sim2.app.talleb_5edma.screens.OTPValidationScreen
import sim2.app.talleb_5edma.screens.OfferComparisonScreen
import sim2.app.talleb_5edma.screens.SplashScreen
import sim2.app.talleb_5edma.screens.CvResultScreen
import sim2.app.talleb_5edma.screens.AiInterviewTrainingScreen
import sim2.app.talleb_5edma.screens.ResetPasswordScreen
import sim2.app.talleb_5edma.screens.ScreenCall
import sim2.app.talleb_5edma.screens.ScreenCreateOffre
import sim2.app.talleb_5edma.screens.ScreenCreateOffreCasual
import sim2.app.talleb_5edma.screens.ScreenCreateOffrePro
import sim2.app.talleb_5edma.screens.ScreenOffre
import sim2.app.talleb_5edma.screens.ScreenUpdateOffre
import sim2.app.talleb_5edma.screens.SignUpScreen
import sim2.app.talleb_5edma.ui.theme.Talleb_5edmaTheme
import sim2.app.talleb_5edma.util.forceClearAllData
import sim2.app.talleb_5edma.util.getToken

/* ====== Palette ====== */
private val PurplePrimary = Color(0xFF7C4DFF) // Violet Principal
private val PurpleDark = Color(0xFF5E35B1)    // Pourpre Foncé


private val TopBarGradient = Brush.horizontalGradient(
    0f to Color(0xFFFFFFFF), // Dark deep purple
    0.5f to Color(0xFFFFFFFF), // Purple
    1f to Color(0xFFFFFFFF)  // Lighter purple/pinkish
)

/* ====== Routes secondaires ====== */
private const val AVAILABILITY_ROUTE = "availability"
private const val EXAM_MODE_ROUTE = "exam_mode"
private const val PREF_ROUTE = "preferences"

/* ====== Bottom Navigation Destinations ====== */
sealed class BottomDest(val route: String, val label: String, val icon: ImageVector) {
    data object Home : BottomDest("accueil", "Accueil", Icons.Filled.Home)
    data object Fav : BottomDest("favoris", "Favoris", Icons.Filled.FavoriteBorder)
    data object Add : BottomDest("create_offer", "Publier", Icons.Filled.Add)
    data object Time : BottomDest("temps", "Mes offres", Icons.Filled.AccessTime) // Renamed label to match screenshot roughly or keep logic
    data object More : BottomDest("plus", "Profil", Icons.Filled.Settings) // Renamed label to match screenshot roughly
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Talleb_5edmaTheme {
                Main()
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@Composable
fun Main() {
    val navController = rememberNavController()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Get current back stack entry for navigation state
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route?.split("/")?.first() ?: Routes.Screen1

    // FIX: Use reactive state for token and user
    var currentToken by remember { mutableStateOf(getToken(context)) }
    var currentUser by remember { mutableStateOf<User?>(null) }


    // Add WebSocketCallManager for call functionality with HARDCODED USER
    val callManager = remember {
        WebSocketCallManager(context).apply {
            // Initialize socket connection
            initializeSocket()
        }
    }

    // Add InterviewWebSocketManager for real-time interview invitations
    val interviewWsManager = remember { InterviewWebSocketManager(context) }

    // Add call state observation
    val callState by callManager.callState.collectAsState()
    val callData by callManager.callData.collectAsState()

    // Determine if we should show the bottom bar and top bar
    val authRoutes = setOf(
        Routes.Screen1,
        Routes.Screen2,
        Routes.ScreenForgot,
        Routes.ScreenOTP,
        Routes.ScreenResetP,
        Routes.ScreenEditProfile,
        Routes.ScreenOffre,
        Routes.OfferComparisonScreen,
        Routes.ScreenChating,
        Routes.ScreenCall,
        Routes.ScreenCreateOffre,
        Routes.ScreenCreateOffrePro,
        Routes.ScreenCreateOffreCasual,
        Routes.ScreenUpdateOffre,
        Routes.AiInterviewTraining,
        Routes.Splash,
        Routes.ScreenHomeEntreprise,
    )

    val showBottomBar = currentRoute !in authRoutes && currentToken.isNotEmpty()
    val showTopBar = currentRoute !in authRoutes && currentToken.isNotEmpty()
    val showBack = currentRoute in setOf(
        "filter", "qr", "map", "calendar",
        AVAILABILITY_ROUTE,
        EXAM_MODE_ROUTE,
        PREF_ROUTE
    ) && currentToken.isNotEmpty()

    // FIX: Update token state when context changes
    LaunchedEffect(context) {
        currentToken = getToken(context)
        println("CatLog: MainActivity - Initial token: '${currentToken.take(10)}...' (${currentToken.length} chars)")
    }


    var userId by remember { mutableStateOf<String?>(null) }

    // Cleanup WebSocket on dispose
    DisposableEffect(Unit) {
        onDispose {
            interviewWsManager.disconnect()
        }
    }

    LaunchedEffect(Unit) {
        // Check for token changes periodically
        while (true) {
            delay(100)
            val newToken = getToken(context)
            if (newToken != currentToken) {
                println("CatLog: MainActivity - TOKEN CHANGED from '${currentToken.take(10)}...' to '${newToken.take(10)}...'")
                currentToken = newToken
                currentUser = null // Reset user when token changes
            }
        }
    }

    val startingRoute = if (currentToken.isNotEmpty()) BottomDest.Home.route else Routes.Screen1

    // FIX: Load user data when token changes
    LaunchedEffect(currentToken) {
        println("CatLog: MainActivity - Token effect - Token: '${currentToken.take(10)}...' (${currentToken.length} chars)")

        if (currentToken.isNotEmpty()) {
            try {
                val repository = UserRepository()
                println("CatLog: MainActivity - Fetching user with token: '${currentToken.take(10)}...'")
                val response = repository.getCurrentUser(currentToken)

                println("CatLog: MainActivity - User fetch response - success: ${response.success}, user: ${response.nom}")

                if (response.success == true && response._id != null) {
                    currentUser = response.data
                    userId = response._id
                    callManager.setUserInfo(response._id, response.nom)
                    
                    println("CatLog: MainActivity - User data loaded: ${response.data?.email}")
                    
                    // Connect to interview WebSocket when user is available
                    println("🔌 Connecting InterviewWebSocketManager for user: ${response._id}")
                    interviewWsManager.connect(response._id)
                } else {
                    println("CatLog: MainActivity - Failed to load user data: ${response.message}")
                    currentUser = null

                    if (response.status == 401 || response.status == 403) {
                        println("CatLog: MainActivity - Token is invalid, clearing data")
                        forceClearAllData(context)
                        currentToken = "" // Update token state
                    }
                }
            } catch (e: Exception) {
                println("CatLog: MainActivity - Error loading user data: ${e.message}")
                currentUser = null
                scope.launch {
                    snackBarHostState.showSnackbar(
                        message = "Network error: ${e.message}",
                        duration = SnackbarDuration.Long
                    )
                }
            } finally {
                // Done loading
            }
        } else {
            currentUser = null
        }
    }


    var permissionsGranted by remember { mutableStateOf(false) }
    // State to track offer type selection (false = Occasionnel, true = Professionnel)
    var isProfessionalMode by remember { mutableStateOf(false) }
    
    if (!permissionsGranted) {
        NativePermissionHandler {
            permissionsGranted = true
        }
        return
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                snackbar = { snackBarData ->
                    Snackbar(
                        snackbarData = snackBarData,
                        containerColor = colorResource(R.color.snackBarSurfaceColor),
                        contentColor = colorResource(R.color.onSnackBarColor),
                    )
                }
            )
        },
        topBar = {
            if (showTopBar) {
                PremiumTopBar(
                    title = titleForRoute(currentRoute),
                    showBack = showBack,
                    onBack = { if (showBack) navController.popBackStack() },
                    onProfile = {
                        // Navigate to profile screen
                        navController.navigate(Routes.ScreenEditProfile)
                    },
                    onBell = { /* TODO: Implement notifications */ },
                    user = currentUser
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                PremiumBottomBar(
                    items = listOf(
                        BottomDest.Home,
                        BottomDest.Fav,
                        BottomDest.Add,
                        BottomDest.Time,
                        BottomDest.More
                    ),
                    currentRoute = currentRoute,
                    onSelect = { route ->
                        if (route == BottomDest.Add.route) {
                            // Check if user is organization
                            if (currentUser?.isOrganization == true) {
                                navController.navigate(Routes.ScreenCreateOffre)
                            } else {
                                // Use the switch state to navigate
                                val targetRoute = if (isProfessionalMode) {
                                    Routes.ScreenCreateOffrePro
                                } else {
                                    Routes.ScreenCreateOffreCasual
                                }
                                navController.navigate(targetRoute)
                            }
                        } else if (route == BottomDest.Home.route) {
                            // Rediriger vers l'accueil entreprise si l'utilisateur est une entreprise
                            val destination = if (currentUser?.isOrganization == true) {
                                Routes.ScreenHomeEntreprise
                            } else {
                                BottomDest.Home.route
                            }
                            navController.navigate(destination) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(BottomDest.Home.route) { saveState = true }
                            }
                        } else if (route != currentRoute) {
                            navController.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(BottomDest.Home.route) { saveState = true }
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(
            bottom = innerPadding.calculateBottomPadding(),
            top = innerPadding.calculateTopPadding()
        )

        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Routes.Splash,
                modifier = modifier
            )
            {
                composable(Routes.Splash) {

                    SplashScreen(
                        navController = navController,
                        startDestination =startingRoute ,
                    )
                }

                // Removed duplicate Time and Calendar routes


                // Evenements
                composable(Routes.ScreenEvenements) {
                    EvenementsScreen(navController, getToken(context))
                }
                composable(Routes.ScreenEvenementCreate) {
                    EvenementFormScreen(navController, token = getToken(context))
                }
                composable(Routes.ScreenEvenementEdit + "/{id}") {
                    val id = it.arguments?.getString("id")
                    EvenementFormScreen(navController, eventId = id, token = getToken(context))
                }

                // Disponibilites
                composable(Routes.ScreenDisponibilites) {
                    DisponibilitesScreen(navController, getToken(context))
                }
                composable(Routes.ScreenDisponibiliteCreate + "/{jour}") { backStackEntry ->
                    val jour = backStackEntry.arguments?.getString("jour")
                    DisponibiliteFormScreen(navController, token = getToken(context), jourParam = jour)
                }
                composable(Routes.ScreenDisponibiliteCreate) {
                    DisponibiliteFormScreen(navController, token = getToken(context))
                }
                composable(Routes.ScreenDisponibiliteEdit + "/{id}") {
                    val id = it.arguments?.getString("id")
                    DisponibiliteFormScreen(navController, disponibiliteId = id, token = getToken(context))
                }

                // Authentication routes
                composable(Routes.Screen1) {
                    LoginScreen(navController, modifier, snackBarHostState)
                }
                composable(Routes.Screen2) {
                    SignUpScreen(navController, modifier)
                }
                composable(Routes.ScreenOffre+"/{offerId}") { backStackEntry ->
                    val offerId = backStackEntry.arguments?.getString("offerId")
                    ScreenOffre(
                        navController = navController,
                        offerId = offerId,
                        token = currentToken
                    )
                }
                composable(Routes.ScreenForgot) {
                    ForgotPasswordScreen(navController, modifier, snackBarHostState)
                }
                composable(Routes.ScreenOTP + "/{Code}") {
                    val code = it.arguments?.getString("Code")
                    OTPValidationScreen(navController, modifier, code ?: "", snackBarHostState)
                }
                composable(Routes.ScreenResetP) {
                    ResetPasswordScreen(navController, modifier, snackBarHostState)
                }
                composable(Routes.ScreenEditProfile) {
                    println("CatLog: MainActivity - Navigating to EditProfile - User: ${currentUser != null}, Token: '${currentToken.take(10)}...' (${currentToken.length} chars)")

                    EditProfileScreen(
                        navController = navController,
                        user = currentUser,
                        token = currentToken,
                        snackBarHostState = snackBarHostState,
                        onProfileUpdated = { updatedUser ->
                            currentUser = updatedUser
                            scope.launch {
                                snackBarHostState.showSnackbar(
                                    message = "Profile updated successfully!",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )
                }

                // Create Offre routes
                composable(Routes.ScreenCreateOffre) {
                    ScreenCreateOffre(
                        navController = navController,
                        snackBarHostState = snackBarHostState,
                        token = currentToken
                    )
                }
                composable(Routes.ScreenCreateOffrePro) {
                    ScreenCreateOffrePro(
                        navController = navController,
                        snackBarHostState = snackBarHostState,
                        token = currentToken
                    )
                }
                composable(Routes.ScreenCreateOffreCasual) {
                    ScreenCreateOffreCasual(
                        navController = navController,
                        snackBarHostState = snackBarHostState,
                        token = currentToken
                    )
                }
                composable(Routes.ScreenUpdateOffre + "/{offerId}") {
                    val offerId = it.arguments?.getString("offerId")
                    println("CatLog : >>>>$offerId")
                    ScreenUpdateOffre(
                        navController = navController,
                        offerId = offerId!!,
                        token = getToken(context)
                    )
                }

                // Main app routes
                composable(BottomDest.Home.route) {
                    println("CatLog: Token!"+getToken(context))

                    AccueilScreen(
                        navController,
                        token = getToken(context),
                        isProfessionalMode = isProfessionalMode,
                        onProfessionalModeChange = { newValue ->
                            isProfessionalMode = newValue
                        }
                    )
                }

                // Route pour l'accueil entreprise
                composable(Routes.ScreenHomeEntreprise) {
                    EntrepriseHomeScreen(
                        navController = navController,
                        token = getToken(context)
                    )
                }

                composable(BottomDest.Fav.route)  {
                    PlaceholderScreen("Vos favoris")
                }
                composable(BottomDest.Time.route) {
                    TimeScreen(
                        userName = currentUser?.nom ?: "User",
                        onOpenCalendar = { navController.navigate("calendar") },
                        onOpenAvailability = { navController.navigate(AVAILABILITY_ROUTE) }

                    )
                }
                composable(BottomDest.More.route) {
                    SettingsScreen(
                        onOpenPreferences = { navController.navigate(PREF_ROUTE) },
                        onOpenMap = { navController.navigate("map") }
                    )
                }

                // Secondary routes
                composable("filter") {
                    FilterScreen(
                        onOpenQr   = { navController.navigate("qr") },
                        onOpenMap  = { navController.navigate("map") },
                        onOpenAiCv = { /* navController.navigate("ai_cv") */ }
                    )
                }
                composable("qr") {
                    QrScreen(
                        onBack = { navController.popBackStack() },
                        onQrDetected = { _ -> navController.popBackStack() }
                    )
                }
                composable("map") {
                    MapScreen(
                        navController = navController,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.LocationPicker) {
                    LocationPickerMapScreen(navController = navController)
                }
                composable("calendar") {
                    CalendarScreen(
                        onBack = { navController.popBackStack() },
                        onManageAvailability = {
                            navController.navigate(AVAILABILITY_ROUTE)
                        }
                    )
                }

                // Disponibilités
                composable(AVAILABILITY_ROUTE) {
                    AvailabilityScreen(
                        onBack = { navController.popBackStack() },
                        onOpenExamMode = { navController.navigate(EXAM_MODE_ROUTE) }
                    )
                }

                // Mode examens
                composable(EXAM_MODE_ROUTE) {
                    ExamModeScreen()
                }

                // Préférences (wizard 5 étapes)
                composable(PREF_ROUTE) {
                    PreferenceWizardScreen(
                        onClose = { navController.popBackStack() },
                        onFinished = { navController.popBackStack() }
                    )
                }

                // Additional routes
                composable(Routes.OfferComparisonScreen) {
                    OfferComparisonScreen(navController)
                }

                composable(Routes.TesT) {
                    ChatTestScreen()
                }

                composable(Routes.ScreenChating + "/{chatId}") { backStackEntry ->
                    val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                    ChatScreen(
                        navController = navController,
                        chatId = chatId,
                        callManager = callManager
                    )
                }

                composable(Routes.ScreenCall + "/{chatId}/{toUserId}/{isVideoCall}") {
                    val chatId = it.arguments?.getString("chatId") ?: ""
                    val toUserId = it.arguments?.getString("toUserId") ?: ""
                    val isVideoCall = it.arguments?.getString("isVideoCall")?.toBoolean() ?: false

                    ScreenCall(
                        isVideoCall = isVideoCall,
                        navController = navController,
                        toUserId = toUserId,
                        chatId = chatId,
                        callManager = callManager
                    )
                }
                // Écran Analyse CV IA
                composable("ai_cv") {
                    CVAnalysisScreen(
                        onCloseClick = { navController.popBackStack() },
                        onExistingCVClick = { /* géré dans l’écran si besoin */ },
                        onGenerateCVClick = { /* future feature */ },
                        onAnalysisSuccess = { result ->
                            // Serialize object to JSON to pass it
                            val json = com.google.gson.Gson().toJson(result)
                            // Encode URL to avoid issues with special characters
                            val encodedJson = java.net.URLEncoder.encode(json, "UTF-8")
                            navController.navigate("cv_result/$encodedJson")
                        }
                    )
                }

                // Écran Résultat CV
                composable(
                    route = "cv_result/{data}",
                    arguments = listOf(androidx.navigation.navArgument("data") { type = androidx.navigation.NavType.StringType })
                ) { backStackEntry ->
                    val json = backStackEntry.arguments?.getString("data")
                    if (json != null) {
                        // Parse JSON safely outside of Composable selection if possible, or just parse directly
                        val decodedJson = try {
                             java.net.URLDecoder.decode(json, "UTF-8")
                        } catch (_: Exception) { null }

                        if (decodedJson != null) {
                             val result = try {
                                 com.google.gson.Gson().fromJson(decodedJson, sim2.app.talleb_5edma.models.CvStructuredResponse::class.java)
                             } catch (_: Exception) { null }

                             if (result != null) {
                                 CvResultScreen(
                                     cvResult = result,
                                     onBackClick = { navController.popBackStack() }
                                 )
                             } else {
                                 // Handle error or pop back
                                 LaunchedEffect(Unit) { navController.popBackStack() }
                             }
                        } else {
                             LaunchedEffect(Unit) { navController.popBackStack() }
                        }
                    } else {
                         LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }
                 // AI Interview Training
                 composable(Routes.AiInterviewTraining + "/{chatId}/{mode}") { backStackEntry ->
                     val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                     val mode = backStackEntry.arguments?.getString("mode") ?: "coaching"
                     
                     AiInterviewTrainingScreen(
                         navController = navController,
                         chatId = chatId,
                         token = getToken(context),
                         initialMode = mode
                     )
                 }
            }

            // GLOBAL CALL POPUP - This will appear over any screen when there's an incoming call
            GlobalCallNotificationPopup(
                callManager = callManager,
                navController = navController,
                callState = callState,
                callData = callData
            )

            // GLOBAL INTERVIEW INVITATION POPUP (WebSocket)
            GlobalInterviewInvitationPopup(
                navController = navController,
                currentUserId = userId, // Use userId instead of currentUser?._id
                interviewWsManager = interviewWsManager
            )
        }
    }
}

// Global Call Notification Popup Composable
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun GlobalCallNotificationPopup(
    callManager: WebSocketCallManager,
    navController: NavController,
    callState: WebSocketCallManager.CallState,
    callData: WebSocketCallManager.CallData
) {
    val scope = rememberCoroutineScope()
    // Show outgoing call notification
    if (callState is WebSocketCallManager.CallState.OutgoingCall) {
        navController.navigate(
            "${Routes.ScreenCall}/${callData.chatId}/${callData.toUserId}/${callData.isVideoCall}"
        )
    }

    // Show incoming call notification (existing code)
    else if (callState is WebSocketCallManager.CallState.IncomingCall) {
        scope.launch {
            val userRepository = UserRepository()
            callManager.toUserName = callData.fromUserName
            try {
                callManager.toUserImage = userRepository.getOtherProfileImage(callData.fromUserId).imageUrl
            } catch (e: Exception) {
                println("Error fetching profile image: ${e.message}")
            }
        }


        AlertDialog(
            onDismissRequest = {
                // Don't allow dismissing by clicking outside
            },
            title = { Text("📞 Incoming Call") },
            text = {
                Column {
                    Text("Incoming ${if (callData.isVideoCall) "video" else "audio"} call from:")
                    Text(
                        callData.fromUserName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        callManager.acceptCall()
                        // Navigate to call screen
                        navController.navigate(
                            "${Routes.ScreenCall}/${callData.chatId}/${callData.fromUserId}/${callData.isVideoCall}"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("✅ Accept Call")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        callManager.rejectCall()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("❌ Decline")
                }
            }
        )
    }
}

@Composable
fun NativePermissionHandler(
    onPermissionsGranted: () -> Unit
) {
    var permissionsRequested by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            onPermissionsGranted()
        } else {
            // Even if some permissions are denied, we can still proceed
            onPermissionsGranted()
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionsRequested) {
            val permissions = buildList {
                add(Manifest.permission.CAMERA)
                add(Manifest.permission.RECORD_AUDIO)
                add(Manifest.permission.ACCESS_FINE_LOCATION)
                add(Manifest.permission.ACCESS_COARSE_LOCATION)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                    add(Manifest.permission.READ_MEDIA_IMAGES)
                    add(Manifest.permission.READ_MEDIA_VIDEO)
                    add(Manifest.permission.READ_MEDIA_AUDIO)
                }
            }.toTypedArray()

            permissionLauncher.launch(permissions)
            permissionsRequested = true
        }
    }

    // Show loading screen while requesting permissions
    if (!permissionsRequested) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Setting up your app...")
            }
        }
    }
}
/* ====== UI Components from Second Main ====== */

@Composable
private fun PremiumTopBar(
    title: String,
    showBack: Boolean,
    onBack: () -> Unit,
    onProfile: () -> Unit,
    onBell: () -> Unit,
    user: User?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TopBarGradient)
            .padding(start = 16.dp, end = 16.dp, top = 40.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Section: Menu/Back + Logo + Text
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showBack) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                } else {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menu",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))


                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))

                // Title and Subtitle
                Column {
                    Text(
                        text = title,
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                }
            }

            // Right Section: Bell + Settings + Profile
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Notification Bell
                Box {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.Black,
                        modifier = Modifier.size(26.dp).clickable { onBell() }
                    )
                    // Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .background(Color(0xFFFF3D00), CircleShape)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "3",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }



                Spacer(modifier = Modifier.width(16.dp))

                // Profile Initials
                val initials = user?.nom?.take(2)?.uppercase() ?: "JD"
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFE040FB), Color(0xFFFF80AB))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}



@Composable
private fun PremiumBottomBar(
    items: List<BottomDest>,
    currentRoute: String,
    onSelect: (String) -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 16.dp,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { dest ->
                val selected = dest.route == currentRoute
                
                if (dest is BottomDest.Add) {
                    // Central FAB
                    Box(
                        modifier = Modifier
                            .offset(y = (-24).dp) // Move up slightly
                            .size(56.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFE040FB), Color(0xFF7C4DFF))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelect(dest.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Publier",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else {
                    // Standard Icon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null // Remove ripple for cleaner look
                            ) { onSelect(dest.route) }
                            .padding(8.dp)
                    ) {
                        // Icon Container
                        Box(
                            modifier = Modifier
                                .size(if (selected) 40.dp else 24.dp)
                                .background(
                                    color = if (selected) Color(0xFF7C4DFF) else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = dest.label,
                                tint = if (selected) Color.White else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = dest.label,
                            fontSize = 10.sp,
                            color = if (selected) Color(0xFF7C4DFF) else Color.Gray,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

/* ====== Settings screen avec bouton Mes préférences ====== */

@Composable
private fun SettingsScreen(
    onOpenPreferences: () -> Unit,
    onOpenMap: () -> Unit // New parameter
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Paramètres",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onOpenPreferences,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurpleDark)
        ) {
            Text("Mes préférences")
        }

        Spacer(Modifier.height(16.dp))

        // New Map Button
        Button(
            onClick = onOpenMap,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
        ) {
            Text("Ouvrir la Carte (OSM)")
        }
    }
}

/* ====== Utils ====== */
@Composable
private fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, modifier = Modifier.padding(16.dp))
    }
}

private fun titleForRoute(route: String): String {
    return when (route) {
        BottomDest.Home.route -> "Accueil"
        BottomDest.Fav.route -> "Favoris"
        BottomDest.Time.route -> "Temps"
        BottomDest.More.route -> "Paramètres"
        "filter" -> "Filtres"
        "qr" -> "Code QR"
        "map" -> "Carte"
        "calendar" -> "Calendrier"
        AVAILABILITY_ROUTE -> "Gérer mes disponibilités"
        EXAM_MODE_ROUTE -> "Mode examens"
        PREF_ROUTE -> "Préférences"
        "ai_cv" -> "Analyse CV"
        Routes.Screen1 -> "Connexion"
        Routes.Screen2 -> "Inscription"
        Routes.ScreenEditProfile -> "Profil"
        Routes.ScreenOffre -> "Offres"
        Routes.ScreenForgot -> "Mot de passe oublié"
        Routes.ScreenOTP -> "Validation OTP"
        Routes.ScreenResetP -> "Réinitialisation"
        Routes.OfferComparisonScreen -> "Comparaison d'offres"
        Routes.ScreenChating -> "Chat"
        Routes.ScreenCall -> "Appel"
        Routes.ScreenCreateOffre -> "Créer une offre" // Add title for the new route
        Routes.ScreenCreateOffrePro -> "Créer une offre professionnelle"
        Routes.ScreenCreateOffreCasual -> "Créer une offre occasionnelle"
        Routes.ScreenUpdateOffre -> "Modifier une offre"
        Routes.AiInterviewTraining -> "Entraînement d'entretien"
        else -> "Accueil"
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Talleb_5edmaTheme {
        Main()
    }
}

// Global Interview Invitation Popup (WebSocket Based)
@Composable
fun GlobalInterviewInvitationPopup(
    navController: NavController,
    currentUserId: String?,
    interviewWsManager: InterviewWebSocketManager
) {
    println("👀 GlobalInterviewInvitationPopup: Composed with userId=$currentUserId")
    if (currentUserId == null) return
    
    val repository = remember { InterviewInvitationRepository() }
    val scope = rememberCoroutineScope()
    
    // ✨ COLLECT WEBSOCKET EVENTS INSTEAD OF POLLING ✨
    val pendingInvitation by interviewWsManager.pendingInvitation.collectAsState()
    val wsConnected by interviewWsManager.connectionStatus.collectAsState()
    
    // Log connection status
    LaunchedEffect(wsConnected) {
        println("🔌 Interview WebSocket connected: $wsConnected")
    }
    
    // Show popup when invitation is received via WebSocket
    pendingInvitation?.let { invitation ->
        println("🎉 Showing invitation popup for: ${invitation.fromUserName}")
        
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss */ },
            title = { Text("📄 Interview Invitation") },
            text = { Text("${invitation.fromUserName} has invited you to an AI Mock Interview. Do you want to accept?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val response = withContext(Dispatchers.IO) {
                                    repository.acceptInvitation(invitation.invitationId)
                                }
                                if (response.success) {
                                    // Clear the WebSocket pending invitation
                                    interviewWsManager.clearPendingInvitation()
                                    
                                    // Navigate to interview mode
                                    withContext(Dispatchers.Main) {
                                        navController.navigate("${Routes.AiInterviewTraining}/${invitation.chatId}/employer_interview")
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745))
                ) { Text("Accept") }
            },
            dismissButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    repository.rejectInvitation(invitation.invitationId)
                                }
                                // Clear the WebSocket pending invitation
                                interviewWsManager.clearPendingInvitation()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545))
                ) { Text("Decline") }
            }
        )
    }
}

