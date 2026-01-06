package sim2.app.talleb_5edma

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp                  // <--- IMPORT AJOUTÉ
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.projet1.interfaces.AccueilContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import sim2.app.talleb_5edma.interfaces.CalendarScreen
import sim2.app.talleb_5edma.interfaces.FilterScreen
import sim2.app.talleb_5edma.interfaces.MapScreen
import sim2.app.talleb_5edma.interfaces.QrScreen
import sim2.app.talleb_5edma.interfaces.TimeScreen
import sim2.app.talleb_5edma.interfaces.AvailabilityScreen
import sim2.app.talleb_5edma.interfaces.ExamModeScreen
import sim2.app.talleb_5edma.interfaces.PreferenceWizardScreen
import sim2.app.talleb_5edma.models.User
import sim2.app.talleb_5edma.network.UserRepository
import sim2.app.talleb_5edma.screens.*
import sim2.app.talleb_5edma.ui.theme.Talleb_5edmaTheme
import sim2.app.talleb_5edma.util.*

/* ====== Palette ====== */
private val RedLight   = Color(0xFFFF6B6B)
private val RedDark    = Color(0xFFD32F2F)
private val RedDeep    = Color(0xFFB71C1C)
private val GlassWhite = Color(0xBBFFFFFF)
private val BarBorder  = Color(0x33FFFFFF)

private val TopBarGradient = Brush.verticalGradient(
    0f to RedLight, 0.5f to RedDark, 1f to RedDeep
)

/* ====== Routes secondaires ====== */
private const val AVAILABILITY_ROUTE = "availability"
private const val EXAM_MODE_ROUTE    = "exam_mode"
private const val PREF_ROUTE         = "preferences"

/* ====== Bottom Navigation Destinations ====== */
sealed class BottomDest(val route: String, val label: String, val icon: ImageVector) {
    data object Home : BottomDest("accueil", "Accueil", Icons.Filled.Home)
    data object Fav  : BottomDest("favoris", "Favoris", Icons.Filled.FavoriteBorder)
    data object Time : BottomDest("temps",   "Temps",   Icons.Filled.AccessTime)
    data object More : BottomDest("plus",    "Plus",    Icons.Filled.Settings)
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

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route?.split("/")?.first() ?: Routes.Screen1

    var currentToken by remember { mutableStateOf(getToken(context)) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var isLoadingUser by remember { mutableStateOf(false) }

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
    )

    val showBottomBar = currentRoute !in authRoutes && currentToken.isNotEmpty()
    val showTopBar = currentRoute !in authRoutes && currentToken.isNotEmpty()
    val showBack = currentRoute in setOf(
        "filter", "qr", "map", "calendar",
        AVAILABILITY_ROUTE,
        EXAM_MODE_ROUTE,
        PREF_ROUTE
    ) && currentToken.isNotEmpty()

    LaunchedEffect(context) {
        currentToken = getToken(context)
        println("CatLog: MainActivity - Initial token: '${currentToken.take(10)}...' (${currentToken.length} chars)")
    }

    val startingRoute = if (currentToken.isNotEmpty()) BottomDest.Home.route else Routes.Screen1

    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            val newToken = getToken(context)
            if (newToken != currentToken) {
                println("CatLog: MainActivity - TOKEN CHANGED from '${currentToken.take(10)}...' to '${newToken.take(10)}...'")
                currentToken = newToken
                currentUser = null
            }
        }
    }

    LaunchedEffect(currentToken) {
        println("CatLog: MainActivity - Token effect - Token: '${currentToken.take(10)}...' (${currentToken.length} chars)")

        if (currentToken.isNotEmpty()) {
            isLoadingUser = true
            try {
                val repository = UserRepository()
                println("CatLog: MainActivity - Fetching user with token: '${currentToken.take(10)}...'")
                val response = repository.getCurrentUser(currentToken)

                println("CatLog: MainActivity - User fetch response - success: ${response.success}, user: ${response.nom}")

                if (response.success == true && response._id != null) {
                    currentUser = response.data
                    println("CatLog: MainActivity - User data loaded: ${response.data?.email}")
                } else {
                    println("CatLog: MainActivity - Failed to load user data: ${response.message}")
                    currentUser = null

                    if (response.status == 401 || response.status == 403) {
                        println("CatLog: MainActivity - Token is invalid, clearing data")
                        forceClearAllData(context)
                        currentToken = ""
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
                isLoadingUser = false
            }
        } else {
            currentUser = null
            isLoadingUser = false
        }
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
                PremiumTopBarRed(
                    title = titleForRoute(currentRoute),
                    showBack = showBack,
                    onBack = { if (showBack) navController.popBackStack() },
                    onProfile = { navController.navigate(Routes.ScreenEditProfile) },
                    onBell = { /* TODO */ }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                PremiumBottomBarRed(
                    items = listOf(
                        BottomDest.Home,
                        BottomDest.Fav,
                        BottomDest.Time,
                        BottomDest.More
                    ),
                    currentRoute = currentRoute,
                    onSelect = { route ->
                        if (route != currentRoute) navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(BottomDest.Home.route) { saveState = true }
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

        NavHost(
            navController = navController,
            startDestination = startingRoute,
            modifier = modifier
        ) {
            // Auth
            composable(Routes.Screen1) {
                LoginScreen(navController, modifier, snackBarHostState)
            }
            composable(Routes.Screen2) {
                SignUpScreen(navController, modifier, snackBarHostState)
            }
            composable(Routes.ScreenOffre) {
                ScreenOffre(modifier, navController)
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

            // Bottom nav
            composable(BottomDest.Home.route) {
                AccueilContent(
                    onOpenFilter = { navController.navigate("filter") }
                )
            }
            composable(BottomDest.Fav.route) {
                PlaceholderScreen("Vos favoris")
            }
            composable(BottomDest.Time.route) {
                TimeScreen(
                    userName = currentUser?.nom ?: "User",
                    onOpenCalendar = { navController.navigate("calendar") },
                    onOpenAvailability = { navController.navigate(AVAILABILITY_ROUTE) },
                    onOpenRoutineAnalysis = { navController.navigate(Routes.ScreenRoutineAnalysis) },
                    onOpenScheduleUpload = { navController.navigate(Routes.ScreenScheduleImport) },
                    onOpenAiMatching = { navController.navigate(Routes.ScreenAiMatching) }
                )
            }
            composable(BottomDest.More.route) {
                // --------- ICI : écran Paramètres avec bouton Mes préférences ----------
                SettingsScreen(
                    onOpenPreferences = { navController.navigate(PREF_ROUTE) }
                )
            }

            // Secondaires
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
                MapScreen(onBack = { navController.popBackStack() })
            }
            composable("calendar") {
                CalendarScreen(
                    onBack = { navController.popBackStack() },
                    onManageAvailability = {
                        navController.navigate(AVAILABILITY_ROUTE)
                    },
                    navController = navController
                )
            }

            // Disponibilités
            composable(AVAILABILITY_ROUTE) {
                AvailabilityScreen(
                    onBack = { navController.popBackStack() },
                    onOpenExamMode = { navController.navigate(EXAM_MODE_ROUTE) },
                    navController = navController
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

            // Autres
            composable(Routes.OfferComparisonScreen) {
                OfferComparisonScreen(navController)
            }
            composable(Routes.ScreenChating) {
                ScreenChating(navController)
            }
            composable(Routes.ScreenCall + "/{isVideoCall}") {
                val isVideoCall = it.arguments?.getString("isVideoCall")?.toBooleanStrictOrNull()
                ScreenCall(isVideoCall!!, navController)
            }

            // Evenements
            composable(Routes.ScreenEvenements) {
                EvenementsScreen(navController, currentToken)
            }
            composable(Routes.ScreenEvenementCreate) {
                EvenementFormScreen(navController, token = currentToken)
            }
            composable(Routes.ScreenEvenementEdit + "/{id}") {
                val id = it.arguments?.getString("id")
                EvenementFormScreen(navController, eventId = id, token = currentToken)
            }

            // Disponibilites
            composable(Routes.ScreenDisponibilites) {
                DisponibilitesScreen(navController, currentToken)
            }
            composable(Routes.ScreenDisponibiliteCreate + "/{jour}") { backStackEntry ->
                val jour = backStackEntry.arguments?.getString("jour")
                DisponibiliteFormScreen(navController, token = currentToken, jourParam = jour)
            }
            composable(Routes.ScreenDisponibiliteCreate) {
                DisponibiliteFormScreen(navController, token = currentToken)
            }
            composable(Routes.ScreenDisponibiliteEdit + "/{id}") {
                val id = it.arguments?.getString("id")
                DisponibiliteFormScreen(navController, disponibiliteId = id, token = currentToken)
            }

            // Routine Analysis
            composable(Routes.ScreenRoutineAnalysis) {
                RoutineAnalysisScreen(navController, currentToken)
            }

            // Schedule Import
            composable(Routes.ScreenScheduleImport) {
                ScheduleUploadScreen(navController, currentToken)
            }

            // AI Matching
            composable(Routes.ScreenAiMatching) {
                MatchingScreen(navController, currentToken)
            }
        }
    }
}

/* ====== UI Components ====== */

@Composable
private fun PremiumTopBarRed(
    title: String,
    showBack: Boolean,
    onBack: () -> Unit,
    onProfile: () -> Unit,
    onBell: () -> Unit
) {
    Surface(color = Color.Transparent, shadowElevation = 8.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TopBarGradient, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                if (showBack) {
                    Surface(
                        onClick = onBack,
                        color = GlassWhite,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, BarBorder)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = RedDark)
                            Spacer(Modifier.width(6.dp))
                            Text("Retour", color = RedDark)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                }
                Text(text = title, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallGlassIconRed(icon = Icons.Filled.Notifications, tint = Color.White, onClick = onBell)
                Spacer(Modifier.width(8.dp))
                Surface(
                    onClick = onProfile,
                    color = GlassWhite,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, BarBorder)
                ) {
                    Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Person, null, tint = RedDark)
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallGlassIconRed(icon: ImageVector, tint: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, color = GlassWhite, shape = CircleShape, border = BorderStroke(1.dp, BarBorder)) {
        Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint)
        }
    }
}

@Composable
private fun PremiumBottomBarRed(
    items: List<BottomDest>,
    currentRoute: String,
    onSelect: (String) -> Unit
) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Surface(
            color = GlassWhite,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, BarBorder),
            modifier = Modifier.fillMaxWidth().navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { dest ->
                    val selected = dest.route == currentRoute
                    val bg by animateColorAsState(if (selected) RedDark else Color.Transparent, label = "bgAnim")
                    val fg by animateColorAsState(if (selected) Color.White else RedDark, label = "fgAnim")

                    Surface(
                        color = bg,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .padding(horizontal = 4.dp)
                            .clickable { onSelect(dest.route) }
                    ) {
                        Row(
                            Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(dest.icon, null, tint = fg)
                            if (selected) {
                                Spacer(Modifier.width(8.dp))
                                Text(dest.label, color = fg, maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ====== Settings screen avec bouton Mes préférences ====== */

@Composable
private fun SettingsScreen(
    onOpenPreferences: () -> Unit
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
            colors = ButtonDefaults.buttonColors(containerColor = RedDark)
        ) {
            Text("Mes préférences")
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
        BottomDest.Fav.route  -> "Favoris"
        BottomDest.Time.route -> "Temps"
        BottomDest.More.route -> "Paramètres"
        "filter"              -> "Filtres"
        "qr"                  -> "Code QR"
        "map"                 -> "Carte"
        "calendar"            -> "Calendrier"
        AVAILABILITY_ROUTE    -> "Gérer mes disponibilités"
        EXAM_MODE_ROUTE       -> "Mode examens"
        PREF_ROUTE            -> "Préférences"
        "ai_cv"               -> "Analyse CV"
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
