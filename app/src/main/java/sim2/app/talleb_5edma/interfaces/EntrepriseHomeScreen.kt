package sim2.app.talleb_5edma.interfaces

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import sim2.app.talleb_5edma.BottomDest
import sim2.app.talleb_5edma.Routes
import sim2.app.talleb_5edma.models.User
import sim2.app.talleb_5edma.network.OffreRepository
import sim2.app.talleb_5edma.network.UserRepository
import sim2.app.talleb_5edma.util.getToken

@Composable
fun EntrepriseHomeScreen(
    navController: NavHostController?= null,
    token: String? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userRepository = remember { UserRepository() }
    val offreRepository = remember { OffreRepository() }

    var currentUser by remember { mutableStateOf<User?>(null) }
    var offresCount by remember { mutableIntStateOf(0) }
    var candidatsCount by remember { mutableIntStateOf(0) }
    var interviewsCount by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    val actualToken = token ?: getToken(context)

    LaunchedEffect(actualToken) {
        if (actualToken.isEmpty()) {
            isLoading = false
            return@LaunchedEffect
        }
        try {
            val userResponse = userRepository.getCurrentUser(actualToken)
            currentUser = userResponse.data
            try {
                val offresList = offreRepository.getMyOffers(actualToken)
                offresCount = offresList.size
            } catch (_: Exception) {
                offresCount = 0
            }
            candidatsCount = 0
            interviewsCount = 0
        } catch (_: Exception) {
        } finally {
            isLoading = false
        }
    }

    // REDIRECTION : Si l'utilisateur n'est pas une organisation, on le renvoie vers l'accueil normal
    LaunchedEffect(currentUser, isLoading) {
        if (!isLoading && currentUser != null && currentUser?.isOrganization != true) {
            println("CatLog: EntrepriseHomeScreen - User is NOT an organization, redirecting to Home")
            navController?.navigate(BottomDest.Home.route) {
                popUpTo(Routes.ScreenHomeEntreprise) { inclusive = true }
            }
        }
    }

    val headerGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF7C4DFF),
            Color(0xFFE91E63)
        )
    )

    val screenBg = Brush.verticalGradient(
        colors = listOf(Color(0xFFF6F2FF), Color.White)
    )

    Scaffold(
        containerColor = Color.Transparent
        // Bottom bar is now handled by MainActivity to avoid duplication
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBg)
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF7C4DFF)
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {

                    // ===== HEADER GRADIENT (comme capture) =====
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .background(headerGradient)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.Top
                        ) {
                            Text(
                                text = "ESPACE",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Entreprise",
                                        fontSize = 32.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    // badge (tu peux mettre un vrai icon bleu si tu veux)
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = null,
                                        tint = Color(0xFF2D7DFF),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // icone bâtiment à droite (bouton rond)
                                IconButton(
                                    onClick = { /* TODO: ouvrir page entreprise */ },
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Apartment,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // ===== CONTENU SUR CARTE BLANCHE (comme capture) =====
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(y = (-34).dp) // remonte sur le header (effet capture)
                            .padding(horizontal = 16.dp)
                    ) {

                        // Welcome card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE91E63).copy(alpha = 0.10f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "👋", fontSize = 26.sp)
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Bonjour, ${currentUser?.nom ?: "najd"}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A1A1A)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Gérez vos offres et trouvez les talents qui\nferont grandir votre projet.",
                                        fontSize = 13.sp,
                                        color = Color(0xFF6B6B6B),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        Text(
                            text = "VOTRE ACTIVITÉ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF8A8A8A),
                            modifier = Modifier.padding(start = 6.dp)
                        )

                        Spacer(Modifier.height(10.dp))

                        // 3 cartes en ligne (comme capture)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MiniStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Filled.Work,
                                iconBg = Color(0xFF7C4DFF).copy(alpha = 0.12f),
                                iconTint = Color(0xFF7C4DFF),
                                value = offresCount.toString(),
                                label = "OFFRES",
                                onClick = {
                                    navController?.navigate(Routes.ScreenMesOffresEntreprise)
                                }
                            )
                            MiniStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Filled.People,
                                iconBg = Color(0xFF00BCD4).copy(alpha = 0.12f),
                                iconTint = Color(0xFF00BCD4),
                                value = candidatsCount.toString(),
                                label = "CANDIDATS"
                            )
                            MiniStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Filled.Videocam,
                                iconBg = Color(0xFFFF9800).copy(alpha = 0.12f),
                                iconTint = Color(0xFFFF9800),
                                value = interviewsCount.toString(),
                                label = "INTERVIEWS"
                            )
                        }

                        Spacer(Modifier.height(18.dp))

                        // gros bouton rouge dégradé
                        GradientMainButton(
                            text = "Publier une nouvelle offre",
                            onClick = { navController?.navigate(Routes.ScreenCreateOffre) }
                        )

                        Spacer(Modifier.height(90.dp)) // espace pour bottom bar + fab
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    value: String,
    label: String,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .height(112.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }

            Column {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8A8A8A)
                )
            }
        }
    }
}

@Composable
private fun GradientMainButton(
    text: String = "Publier une nouvelle offre",
    onClick: () -> Unit
) {
    val redGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF2D2D), Color(0xFFE91E63))
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(redGradient),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                }
                Spacer(Modifier.width(10.dp))
                Text(text = text, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Bottom bar avec FAB centré comme sur la capture
 * Now used as the main bottom navigation bar for the entire app
 */
@Composable
fun EntrepriseBottomBar(
    selected: String,
    onSelect: (String) -> Unit,
    onFabClick: () -> Unit
) {
    Box {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 8.dp
        ) {
            NavigationBarItem(
                selected = selected == BottomDest.Home.route,
                onClick = { onSelect(BottomDest.Home.route) },
                icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                label = { Text("Accueil") }
            )
            NavigationBarItem(
                selected = selected == BottomDest.Fav.route,
                onClick = { onSelect(BottomDest.Fav.route) },
                icon = { Icon(Icons.Filled.FavoriteBorder, contentDescription = null) },
                label = { Text("Favoris") }
            )
            // espace pour le FAB au centre
            Spacer(Modifier.weight(1f))

            NavigationBarItem(
                selected = selected == BottomDest.Time.route,
                onClick = { onSelect(BottomDest.Time.route) },
                icon = { Icon(Icons.Filled.Schedule, contentDescription = null) },
                label = { Text("Temps") }
            )
            NavigationBarItem(
                selected = selected == BottomDest.More.route,
                onClick = { onSelect(BottomDest.More.route) },
                icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                label = { Text("Profil") }
            )
        }

        FloatingActionButton(
            onClick = onFabClick,
            shape = CircleShape,
            containerColor = Color(0xFF7C4DFF),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-22).dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    }
}
@Preview(showBackground = true)
@Composable
fun EntrepriseHomeScreenPreview() {
    EntrepriseHomeScreen()
}

