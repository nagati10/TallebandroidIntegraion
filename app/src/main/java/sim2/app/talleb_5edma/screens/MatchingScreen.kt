package sim2.app.talleb_5edma.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import sim2.app.talleb_5edma.models.*
import sim2.app.talleb_5edma.network.MatchingRepository
import sim2.app.talleb_5edma.util.getToken

// ==================== COULEURS ====================
private val RedLight = Color(0xFFFF6B6B)
private val RedDark = Color(0xFFD32F2F)
private val RedDeep = Color(0xFFB71C1C)
private val GreenSuccess = Color(0xFF10B981)
private val YellowWarning = Color(0xFFF59E0B)
private val BluePrimary = Color(0xFF3B82F6)
private val Soft = Color(0xFF6B6B6B)
private val Background = Color(0xFFF8F8FB)
private val CardBg = Color.White
private val YellowLight = Color(0xFFFFF9E6)
private val PinkLight = Color(0xFFFFF0F5)
private val PinkDark = Color(0xFFC2185B)
private val BlueLight = Color(0xFFE3F2FD)
private val PurpleLight = Color(0xFFF3E5F5)

/**
 * Écran principal d'analyse AI-Matching
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchingScreen(
    navController: NavController,
    token: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val actualToken = token ?: getToken(context)
    
    // États
    var matches by remember { mutableStateOf<List<Match>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val repository = remember { MatchingRepository() }
    
    // Calculer les statistiques
    val totalMatches = matches.size
    val averageScore = if (matches.isNotEmpty()) {
        matches.map { it.scores.score }.average().toInt()
    } else 0
    val bestMatch = matches.maxByOrNull { it.scores.score }
    
    // Charger les recommandations au démarrage
    LaunchedEffect(Unit) {
        if (actualToken.isNotEmpty()) {
            isLoading = true
            try {
                matches = repository.getQuickRecommendations(actualToken)
            } catch (e: Exception) {
                println("CatLog: Error loading quick recommendations: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    
    // UI
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // En-tête personnalisé
            item {
                CustomTopBar(
                    title = "Matching IA",
                    onBack = { navController.popBackStack() },
                    onFilter = { /* TODO */ },
                    onRefresh = {
                        scope.launch {
                            isLoading = true
                            try {
                                matches = repository.getQuickRecommendations(actualToken)
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Erreur: ${e.message}")
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )
            }
            
            // Cartes de résumé
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CheckCircle,
                        value = totalMatches.toString(),
                        label = "Matches",
                        iconColor = GreenSuccess
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.BarChart,
                        value = "$averageScore%",
                        label = "Score Moyen",
                        iconColor = BluePrimary
                    )
                }
            }
            
            // Meilleur match
            if (bestMatch != null) {
                item {
                    BestMatchCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        match = bestMatch
                    )
                }
            }
            
            // Loading
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = RedDark)
                    }
                }
            }
            
            // Carte détaillée du meilleur match
            if (bestMatch != null && !isLoading) {
                item {
                    DetailedMatchCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        match = bestMatch,
                        onApply = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Candidature envoyée pour ${bestMatch.titre}")
                            }
                        }
                    )
                }
            }
            
            // Autres matches
            if (matches.size > 1 && !isLoading) {
                item {
                    Text(
                        text = "Autres opportunités",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                items(matches.drop(1)) { match ->
                    CompactMatchCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        match = match,
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Détails: ${match.titre}")
                            }
                        }
                    )
                }
            }
            
            // État vide
            if (matches.isEmpty() && !isLoading) {
                item {
                    EmptyStateCard(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            
            // Espacement
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

// ==================== COMPOSANTS UI ====================

@Composable
private fun CustomTopBar(
    title: String,
    onBack: () -> Unit,
    onFilter: () -> Unit,
    onRefresh: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        "Retour",
                        tint = Color.Black
                    )
                }
                
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onFilter) {
                        Icon(
                            Icons.Default.FilterList,
                            "Filtres",
                            tint = Color.Black
                        )
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(
                            Icons.Default.Refresh,
                            "Rafraîchir",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = Soft
            )
        }
    }
}

@Composable
private fun BestMatchCard(
    modifier: Modifier = Modifier,
    match: Match
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = YellowLight,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Star,
                    null,
                    tint = YellowWarning,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Meilleur match",
                        fontSize = 14.sp,
                        color = Soft
                    )
                    Text(
                        text = match.titre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
            Text(
                text = "${match.scores.score}%",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = RedDark
            )
        }
    }
}

@Composable
private fun DetailedMatchCard(
    modifier: Modifier = Modifier,
    match: Match,
    onApply: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Titre et score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.titre,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Business,
                            null,
                            tint = Soft,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = match.entreprise ?: "Entreprise",
                            fontSize = 14.sp,
                            color = Soft
                        )
                    }
                }
                // Indicateur circulaire de score
                CircularScoreIndicator(score = match.scores.score)
            }
            
            // Tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (match.location != null) {
                    TagChip(
                        text = match.location,
                        icon = Icons.Default.Place,
                        backgroundColor = BlueLight,
                        iconColor = BluePrimary
                    )
                }
                if (match.type != null) {
                    TagChip(
                        text = match.type,
                        icon = Icons.Default.Work,
                        backgroundColor = PurpleLight,
                        iconColor = Color(0xFF9C27B0)
                    )
                }
            }
            
            // Boîte d'information
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PinkLight
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Warning,
                        null,
                        tint = YellowWarning,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = match.recommendation,
                        fontSize = 13.sp,
                        color = Color.Black,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Bouton Postuler
            Button(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PinkDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Send,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Postuler",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CircularScoreIndicator(score: Int) {
    val color = when {
        score >= 80 -> GreenSuccess
        score >= 60 -> YellowWarning
        else -> RedDark
    }
    
    val progress = score / 100f
    
    Box(
        modifier = Modifier.size(70.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val strokeWidth = 6.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)
            
            // Track (fond)
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.3f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Progress arc
            val sweepAngle = 360f * progress
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "%",
                fontSize = 12.sp,
                color = color
            )
        }
    }
}

@Composable
private fun TagChip(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = iconColor
            )
        }
    }
}

@Composable
private fun CompactMatchCard(
    modifier: Modifier = Modifier,
    match: Match,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = CardBg,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = match.titre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(Modifier.height(4.dp))
                if (match.entreprise != null) {
                    Text(
                        text = match.entreprise,
                        fontSize = 13.sp,
                        color = Soft
                    )
                }
            }
            Text(
                text = "${match.scores.score}%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = RedDark
            )
        }
    }
}

@Composable
private fun EmptyStateCard(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.SearchOff,
                null,
                tint = Soft,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Aucune opportunité pour le moment",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Soft
            )
            Text(
                text = "Ajustez vos préférences et relancez l'analyse",
                fontSize = 13.sp,
                color = Soft
            )
        }
    }
}
