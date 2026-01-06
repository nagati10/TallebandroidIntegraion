package sim2.app.talleb_5edma.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sim2.app.talleb_5edma.R
import sim2.app.talleb_5edma.Routes
import sim2.app.talleb_5edma.interfaces.rememberAccueilState
import sim2.app.talleb_5edma.models.CreateChatRequest
import sim2.app.talleb_5edma.models.GetUserChatsResponse
import sim2.app.talleb_5edma.models.JobType
import sim2.app.talleb_5edma.models.Offre
import sim2.app.talleb_5edma.models.Shift
import sim2.app.talleb_5edma.models.User
import sim2.app.talleb_5edma.network.ChatRepository
import sim2.app.talleb_5edma.network.OffreRepository
import sim2.app.talleb_5edma.network.TrustLevelResponse
import sim2.app.talleb_5edma.network.UserRepository
import sim2.app.talleb_5edma.util.BASE_URL

private val DarkBackground = Color(0xFF2A2A2A)
private val CardBackground = Color(0xFF333333)
private val TextPrimary = Color(0xFFECECEC)
private val TextSecondary = Color(0xFFCCCCCC)
private val RedAccent = Color(0xFFCF1919)
private val RedButton = Color(0xFFDC2626)

// Sample image list - replace with your actual images
val sampleImages = listOf(
    R.drawable.ic_launcher_foreground,
    R.drawable.ic_launcher_foreground,
    R.drawable.ic_launcher_foreground,
    R.drawable.ic_launcher_foreground
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenOffre(
    modifier: Modifier = Modifier,
    navController: NavController,
    offerId: String? = null,
    token: String? = null
) {
    val offreRepository = remember { OffreRepository() }
    var offer by remember { mutableStateOf<Offre?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Use the same state management pattern as Accueil
    val accueilState = rememberAccueilState(coroutineScope = CoroutineScope(Dispatchers.Main))
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(0) }

    // Load offer details when screen appears or offerId changes
    LaunchedEffect(offerId) {
        if (offerId != null) {
            loading = true
            error = null
            try {
                println("ScreenOffre - Loading offer details for ID: $offerId")
                offer = offreRepository.getOffreById(offerId)
                likeCount = offer?.likeCount ?: 0
                loading = false
                println("ScreenOffre - Successfully loaded offer: ${offer?.title}")
            } catch (e: Exception) {
                error = "Erreur de chargement: ${e.message}"
                loading = false
                println("ScreenOffre - Error loading offer: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // Check if offer is liked when token and offer are available
    LaunchedEffect(offerId, token, offer, accueilState.currentUser) {
        if (token != null && offerId != null && offer != null) {
            try {
                // Check if the current offer ID is in the user's likedOffres
                val userLikedOffres = accueilState.currentUser?.likedOffres ?: emptyList()
                val isOfferLiked = userLikedOffres.contains(offerId)
                isLiked = isOfferLiked
                println("ScreenOffre - Like status for offer $offerId: $isLiked (from user data)")
            } catch (e: Exception) {
                println("ScreenOffre - Error checking like status: ${e.message}")
                // Fallback: check if current user has liked this offer
                try {
                    val likedBy = offer?.likedBy ?: emptyList()
                    val currentUserId = accueilState.currentUser?._id
                    if (currentUserId != null) {
                        isLiked = likedBy.contains(currentUserId)
                        println("ScreenOffre - Fallback like status: $isLiked (from offer likedBy)")
                    }
                } catch (e2: Exception) {
                    println("ScreenOffre - Fallback like check also failed: ${e2.message}")
                }
            }
        }
    }

    // Refresh user data when token changes to ensure we have latest likedOffres
    LaunchedEffect(token) {
        if (token != null) {
            try {
                accueilState.refreshUserData(token)
                println("ScreenOffre - Refreshed user data for like checking")
            } catch (e: Exception) {
                println("ScreenOffre - Error refreshing user data: ${e.message}")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        if (loading) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color.White,
                        strokeWidth = 4.dp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Chargement de l'offre...",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else if (error != null) {
            // Error state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Erreur",
                        color = Color.Red,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        error!!,
                        color = TextSecondary,
                        fontSize = 14.sp,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            // Retry loading
                            if (offerId != null) {
                                loading = true
                                error = null
                                CoroutineScope(Dispatchers.Main).launch {
                                    try {
                                        offer = offreRepository.getOffreById(offerId)
                                        loading = false
                                    } catch (e: Exception) {
                                        error = "Erreur de chargement: ${e.message}"
                                        loading = false
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedButton)
                    ) {
                        Text("Réessayer")
                    }
                }
            }
        } else if (offer != null) {
            // Success state - display offer details
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                HeaderSection(
                    navController = navController,
                    offer = offer!!,
                    isLiked = isLiked,
                    likeCount = likeCount,
                    onLikeToggle = {
                        if (token != null && offerId != null) {
                            println("ScreenOffre - Toggling like for offer: $offerId")
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    val response = offreRepository.toggleLikeOffre(token, offerId)
                                    isLiked = response.liked == true
                                    likeCount = response.likeCount ?: likeCount
                                    println("ScreenOffre - Like toggled: $isLiked, new count: $likeCount")

                                    // Also update the accueil state to keep consistency
                                    accueilState.refreshUserData(token)
                                } catch (e: Exception) {
                                    println("ScreenOffre - Error toggling like: ${e.message}")
                                    e.printStackTrace()
                                }
                            }
                        } else {
                            println("ScreenOffre - Cannot toggle like: token or offerId is null")
                        }
                    }
                )
                ContentSection(offer = offer!!)
            }
            Box(
                modifier = modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .padding(horizontal = 21.dp, vertical = 16.dp)
            ) {
                BottomActionButtons(
                    navController = navController,
                    currentUser = accueilState.currentUser,
                    token = token,
                    offer = offer
                )

            }
        } else {
            // No offer state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Aucune offre sélectionnée",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(containerColor = RedButton)
                    ) {
                        Text("Retour")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeaderSection(
    navController: NavController,
    offer: Offre,
    isLiked: Boolean = false,
    likeCount: Int = 0,
    onLikeToggle: () -> Unit = {}
) {
    // Use offer images if available, otherwise fall back to sample images
    val imagesToUse = if (!offer.images.isNullOrEmpty()) {
        offer.images
    } else {
        // Convert drawable resources to placeholder URLs or keep using sampleImages
        sampleImages.map { "placeholder_$it" }
    }

    val pagerState = rememberPagerState(pageCount = { imagesToUse.size })

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(295.dp)
    ) {

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray)
            ) {
                if (!offer.images.isNullOrEmpty() && page < offer.images.size) {
                    // Load actual offer images from network
                    val imageUrl = "$BASE_URL/${offer.images[page]}"


                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Offer Image ${page + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                        error = painterResource(id = R.drawable.ic_launcher_foreground)
                    )
                } else {
                    // Fallback to sample images (local drawables)
                    Image(
                        painter = painterResource(id = sampleImages[page % sampleImages.size]),
                        contentDescription = "Header Image ${page + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.22f))
                )
            }
        }

        TopNavigationIcons(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            navController = navController,
            isLiked = isLiked,
            likeCount = likeCount,
            onLikeToggle = onLikeToggle
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = offer.title ?: "Titre non disponible",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )

                DistanceIndicator(offer = offer)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                DotsIndicator(
                    totalDots = pagerState.pageCount,
                    selectedIndex = pagerState.currentPage
                )
            }
        }
    }
}

@Composable
fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalDots) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == selectedIndex) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                    .padding(2.dp)
            )
            if (index != totalDots - 1) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
fun ContentSection(offer: Offre) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
            .background(DarkBackground)
            .padding(all = 21.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SalaryTypeTags(offer = offer)
        MatchPercentageCard()

        // View count at the top of description
        ViewCountSection(offer = offer)

        JobDescriptionSection(offer = offer)
        RequirementsSection(offer = offer)
        MoreDetailsSection(offer = offer)
        CompanyInfoSection(offer = offer)
        SimilarOffersSection()
    }
}

@Composable
fun ViewCountSection(offer: Offre) {
    offer.viewCount?.takeIf { it > 0 }?.let { views ->
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = "Views",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "$views vues",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TopNavigationIcons(
    modifier: Modifier = Modifier,
    navController: NavController,
    isLiked: Boolean = false,
    likeCount: Int = 0,
    onLikeToggle: () -> Unit = {}
) {
    Row(
        modifier = modifier.padding(top = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        CircleIconButton(onClick = {
            navController.popBackStack()
        }) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Row(
            verticalAlignment = Alignment.Top
        ) {
            // Like button with count
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            )
            {
                IconButton(
                    onClick = {
                        println("ScreenOffre - Like button clicked, current isLiked: $isLiked")
                        onLikeToggle()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (!isLiked)
                                Color.Black.copy(alpha = 0.22f)
                            else
                                Color.White
                        ),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(
                        if (!isLiked) Icons.Default.FavoriteBorder else Icons.Filled.Favorite,
                        contentDescription = "Favorite",
                        tint = if (!isLiked) Color.White else Color.Red,
                        modifier = Modifier.size(36.dp)
                    )
                }

                if (likeCount > 0) {
                    Text(
                        text = if (likeCount > 99) "99+" else "$likeCount",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                } else {
                    // Empty space to maintain consistent height even when no count
                    Spacer(Modifier.height(14.dp))
                }
            }

            Spacer(Modifier.width(20.dp))

            CircleIconButton(onClick = {
                println("ScreenOffre - Share button clicked")
                // TODO: Implement share functionality
            }) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun CircleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.22f)),
        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
    ) {
        content()
    }
}

@Composable
fun DistanceIndicator(offer: Offre) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = "Location",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = offer.location?.city ?: "Ville non spécifiée",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun SalaryTypeTags(offer: Offre) {
    FlowRow(
        mainAxisSpacing = 8.dp,
        crossAxisSpacing = 8.dp,
        mainAxisAlignment = MainAxisAlignment.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        offer.salary?.let { salary ->
            Tag(text = salary)
        }
        offer.jobType?.let { jobType ->
            Tag(text = when (jobType) {
                JobType.JOB -> "CDI/CDD"
                JobType.STAGE -> "Stage"
                JobType.FREELANCE -> "Freelance"
            })
        }
        offer.shift?.let { shift ->
            Tag(text = when (shift) {
                Shift.JOUR -> "Jour"
                Shift.NUIT -> "Nuit"
                Shift.FLEXIBLE -> "Flexible"
            })
        }
        offer.tags?.forEach { tag ->
            Tag(text = tag)
        }
    }
}

@Composable
fun Tag(text: String) {
    Text(
        text = text,
        color = Color.Black,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFBBBBBB))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
fun MatchPercentageCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(RedAccent)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Match",
            tint = Color(0xFFE3E3E3),
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "Correspondance avec l'offre : 92%",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Votre expertise correspond parfaitement au poste. L'offre salariale est au-dessus de vos attentes, bien que la charge de travail soit élevée.",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun JobDescriptionSection(offer: Offre) {
    Text(
        text = offer.description ?: "Description non disponible",
        color = TextPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp
    )
}

@Composable
fun RequirementsSection(offer: Offre) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Exigences",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
        // Use tags as requirements or show placeholder
        if (!offer.exigences.isNullOrEmpty()) {
            Column {
                offer.exigences.forEach { tag ->
                    Text(
                        text = "• $tag",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            Text(
                text = "Aucune exigence spécifique mentionnée",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun MoreDetailsSection(offer: Offre) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Plus de details",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "More details",
                tint = Color.White,
                modifier = Modifier.rotate(if (expanded) 180f else 0f)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))

                // Build a list of available details
                val details = buildList {
                    // Category
                    offer.category?.let { category ->
                        add("Catégorie: $category")
                    }

                    // Location details
                    offer.location?.let { location ->
                        val locationDetails = buildString {
                            location.city?.let { city ->
                                append(city)
                            }
                            location.address?.let { address ->
                                if (isNotEmpty()) append(", ")
                                append(address)
                            }
                            location.country?.let { country ->
                                if (isNotEmpty()) append(", ")
                                append(country)
                            }
                        }
                        if (locationDetails.isNotEmpty()) {
                            add("Localisation: $locationDetails")
                        }
                    }

                    // Salary
                    offer.salary?.let { salary ->
                        add("Salaire: $salary")
                    }

                    // Job Type
                    offer.jobType?.let { jobType ->
                        add("Type d'emploi: ${when (jobType) {
                            JobType.JOB -> "CDI/CDD"
                            JobType.STAGE -> "Stage"
                            JobType.FREELANCE -> "Freelance"
                        }}")
                    }
                    // Tags count
                    if (!offer.tags.isNullOrEmpty()) {
                        add("Compétences requises: ${offer.tags.size}")
                    }

                    // Shift
                    offer.shift?.let { shift ->
                        add("Horaire: ${when (shift) {
                            Shift.JOUR -> "Jour"
                            Shift.NUIT -> "Nuit"
                            Shift.FLEXIBLE -> "Flexible"
                        }}")
                    }

                    // Created date
                    offer.createdAt?.let { createdAt ->
                        add("Publié le: ${formatDate(createdAt)}")
                    }

                    // Expiration date
                    offer.expiresAt?.let { expiresAt ->
                        add("Expire le: ${formatDate(expiresAt)}")
                    }
                }

                if (details.isNotEmpty()) {
                    details.forEach { detail ->
                        Text(
                            text = "• $detail",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Informations complémentaires non disponibles",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


@Composable
fun CompanyInfoSection(offer: Offre, token: String? = null) {
    val createdBy = offer.createdBy
    var trustLevel by remember { mutableStateOf<TrustLevelResponse?>(null) }
    var loadingTrust by remember { mutableStateOf(false) }

    // Fetch trust level when component is composed or createdBy changes
    LaunchedEffect(createdBy?._id, token) {
        val userId = createdBy?._id
        if (userId != null && token != null) {
            loadingTrust = true
            try {
                val userRepository = UserRepository()
                trustLevel = userRepository.getTrustLevelForUser(token, userId).data
                println("ScreenOffre - Fetched trust level for user $userId: ${trustLevel?.level} - ${trustLevel?.text} (${createdBy.trustXP} XP)")
            } catch (e: Exception) {
                println("ScreenOffre - Error fetching trust level: ${e.message}")
                e.printStackTrace()
            } finally {
                loadingTrust = false
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Info Entreprise",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(17.dp))
                .background(CardBackground)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Use the actual user profile image if available, otherwise fallback to avatar
            if (!createdBy?.image.isNullOrEmpty()) {
                // Load actual profile image
                val imageUrl = if (createdBy.image.startsWith("http")) {
                    createdBy.image.replace("localhost:3005", "10.0.2.2:3005")
                } else {
                    "$BASE_URL/${createdBy.image}"
                }

                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                    error = painterResource(id = R.drawable.ic_launcher_foreground)
                )
            } else {
                // Fallback to generated avatar with user's name
                CompanyAvatar(seedText = createdBy?.nom ?: offer.company ?: "Company")
            }

            Spacer(Modifier.width(12.dp))
            Column {
                // Company/User Name
                Text(
                    text = createdBy?.nom ?: offer.company ?: "Entreprise non spécifiée",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Display email if available
                createdBy?.email?.let { email ->
                    Text(
                        text = email,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Display contact if available, otherwise show message
                if (!createdBy?.contact.isNullOrEmpty() && createdBy.contact !="Not provided" ) {
                    Text(
                        text = createdBy.contact,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Trust Level with XP from API
                if (loadingTrust) {
                    // Loading state for trust level
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = TextSecondary,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Chargement confiance...",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                else if (trustLevel != null) {
                    // Display trust level from API with the new structure
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Trust Level",
                            tint = getTrustLevelColor(trustLevel!!.level ?: 0),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = getTrustLevelText(trustLevel!!.text ?: "NotRecommended"),
                            color = getTrustLevelColor(trustLevel!!.level ?: 0),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        createdBy?.trustXP?.let { xp ->
                            if (xp > 0) {
                                Text(
                                    text = "($xp XP)",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
                else {
                    // Fallback to local calculation if API fails
                    val fallbackXP = createdBy?.trustXP ?: 0
                    val fallbackLevel = calculateTrustLevel(fallbackXP)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Trust Level",
                            tint = getTrustLevelColor(fallbackLevel.first),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = fallbackLevel.second,
                            color = getTrustLevelColor(fallbackLevel.first),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (fallbackXP > 0) {
                            Text(
                                text = "($fallbackXP XP)",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                // Fallback if no user info is available
                if (createdBy == null) {
                    Text(
                        text = "Entreprise de recrutement",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// Helper function to determine trust level color based on level number
private fun getTrustLevelColor(level: Int): Color {
    return when (level) {
        6 -> Color(0xFFFFD700) // Gold for Legend
        5 -> Color(0xFFC0C0C0) // Silver for Master
        4 -> Color(0xFFCD7F32) // Bronze for Expert
        3 -> Color(0xFF4CAF50) // Green for Advanced
        2 -> Color(0xFF2196F3) // Blue for Intermediate
        1 -> Color(0xFFFF9800) // Orange for Beginner
        else -> Color(0xFF9E9E9E) // Gray for NotRecommended
    }
}

// Helper function to translate trust level text to French
private fun getTrustLevelText(levelText: String): String {
    return when (levelText) {
        "Legend" -> "Légende"
        "Master" -> "Maître"
        "Expert" -> "Expert"
        "Advanced" -> "Avancé"
        "Intermediate" -> "Intermédiaire"
        "Beginner" -> "Débutant"
        "NotRecommended" -> "Non recommandé"
        else -> levelText
    }
}

// Fallback function to calculate trust level locally (matching backend logic)
private fun calculateTrustLevel(trustXP: Int): Pair<Int, String> {
    val trustLevels = listOf(
        Pair(0, "Non recommandé"),
        Pair(10, "Débutant"),
        Pair(100, "Intermédiaire"),
        Pair(1000, "Avancé"),
        Pair(10000, "Expert"),
        Pair(100000, "Maître"),
        Pair(1000000, "Légende")
    )

    var level = 0
    var levelText = "Non recommandé"

    for (i in trustLevels.indices.reversed()) {
        if (trustXP >= trustLevels[i].first) {
            level = i
            levelText = trustLevels[i].second
            break
        }
    }

    return Pair(level, levelText)
}
@Composable
fun CompanyAvatar(seedText: String) {
    val colors = listOf(
        0xFFFFCDD2, 0xFFFFE0B2, 0xFFFFF9C4,
        0xFFC8E6C9, 0xFFBBDEFB, 0xFFE1BEE7, 0xFFD7CCC8
    ).map { Color(it) }
    val idx = kotlin.math.abs(seedText.hashCode()) % colors.size
    val bg = colors[idx]

    // Extract initials from the name
    val initials = seedText
        .split(" ", ".", "-")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .take(2)

    Box(
        Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initials,
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.8f),
            fontSize = 16.sp
        )
    }
}

@Composable
fun SimilarOffersSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Offres similaires",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
        SimilarOfferCard(
            title = "Technicien support informatique",
            matchPercentage = "62%",
            matchColor = Color.White
        )
        SimilarOfferCard(
            title = "Assistant marketing digital / CRM",
            matchPercentage = "51%",
            matchColor = Color.White
        )
    }
}

@Composable
fun SimilarOfferCard(
    title: String,
    matchPercentage: String,
    matchColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(CardBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Gray)
            )
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.widthIn(max = 178.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp,
                    maxLines = 2
                )
                Text(
                    text = "Correspondance: $matchPercentage",
                    color = matchColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "View offer",
                tint = Color.White
            )
        }
    }
}

@Composable
fun BottomActionButtons(
    navController: NavController,
    currentUser: User?,
    token: String?,
    offer: Offre?
) {
    val scope = rememberCoroutineScope()
    var showChatListPopup by remember { mutableStateOf(false) }
    var userChats by remember { mutableStateOf<List<GetUserChatsResponse>>(emptyList()) }
    var loadingChats by remember { mutableStateOf(false) }

    // Function to load user's chats filtered by offer
    fun loadUserChats() {
        if (token == null || offer?.id == null) return

        scope.launch {
            loadingChats = true
            try {
                val chatRepository = ChatRepository()
                val allChats = chatRepository.getMyChats(token)

                // Filter chats by this offer ID
                val filteredChats = allChats.filter { chat ->
                    chat.offer?.id == offer.id || chat.offer?.id == offer.id
                }

                userChats = filteredChats
                showChatListPopup = true
            } catch (e: Exception) {
                println("Error loading chats: ${e.message}")
            } finally {
                loadingChats = false
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = {
                navController.navigate(Routes.OfferComparisonScreen)
            },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2E2A2A)
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_compare_arrows_24),
                contentDescription = "Matching details",
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Matching",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Button(
            onClick = {
                if (currentUser?.isOrganization == true) {
                    // Organization: Show popup with filtered chats
                    loadUserChats()
                } else {
                    // Candidate: Create new chat
                    if (currentUser != null && token != null && offer != null) {
                        navigateToChat(navController, currentUser, token, offer, scope)
                    } else {
                        println("ScreenOffre - Missing data for chat navigation")
                    }
                }
            },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RedButton
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_chat_24),
                contentDescription = "Discuter",
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Discuter",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }

    // Chat List Popup for Organizations
    if (showChatListPopup) {
        ChatListPopup(
            chats = userChats,
            loading = loadingChats,
            onDismiss = { showChatListPopup = false },
            onChatSelected = { chatId ->
                showChatListPopup = false
                navController.navigate("${Routes.ScreenChating}/$chatId")
            },
            offer = offer
        )
    }
}

@Composable
fun ChatListPopup(
    chats: List<GetUserChatsResponse>,
    loading: Boolean,
    onDismiss: () -> Unit,
    onChatSelected: (String) -> Unit,
    offer: Offre?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Conversations - ${offer?.title ?: "Cette offre"}",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        },
        text = {
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ChatPrimaryColor)
                }
            } else if (chats.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "No chats",
                        tint = Color.Gray,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Aucune conversation pour cette offre",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.
                    heightIn(max = 400.dp)
                ) {
                    items(chats) { chat ->
                        ChatListItem(
                            chat = chat,
                            onClick = { onChatSelected(chat.id) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Fermer")
            }
        },
        containerColor = Color.White,
        titleContentColor = Color.Black,
        textContentColor = Color.Black
    )
}

@Composable
fun ChatListItem(
    chat: GetUserChatsResponse,
    onClick: () -> Unit
) {
    val candidate = chat.candidate
    val lastMessage = chat.lastMessage ?: "Aucun message"
    val lastActivity = chat.lastActivity ?: ""
    val unreadCount = chat.unreadEntreprise ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Candidate Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (!candidate?.image.isNullOrEmpty()) {
                    AsyncImage(
                        model = "$BASE_URL/${candidate.image}",
                        contentDescription = "Candidate avatar",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Unread badge
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(RedAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadCount > 9) "9+" else "$unreadCount",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            // Chat Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = candidate?.nom ?: "Candidat inconnu",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 16.sp
                )

                Text(
                    text = lastMessage,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = formatLastActivity(lastActivity),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            // Last message type indicator
            chat.lastMessageType?.let { messageType ->
                Icon(
                    imageVector = when (messageType) {
                        "text" -> Icons.Default.TextFields
                        "image" -> Icons.Default.Image
                        "video" -> Icons.Default.Videocam
                        "audio" -> Icons.Default.Mic
                        "emoji" -> Icons.Default.EmojiEmotions
                        else -> Icons.AutoMirrored.Filled.Chat
                    },
                    contentDescription = "Message type",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Helper function to format last activity time
private fun formatLastActivity(isoDate: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        val date = inputFormat.parse(isoDate) ?: return ""

        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply { time = date }

        val diff = now.timeInMillis - target.timeInMillis
        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)
        val days = diff / (1000 * 60 * 60 * 24)

        when {
            minutes < 1 -> "À l'instant"
            minutes < 60 -> "Il y a ${minutes.toInt()} min"
            hours < 24 -> "Il y a ${hours.toInt()} h"
            days < 7 -> "Il y a ${days.toInt()} j"
            else -> {
                val outputFormat = java.text.SimpleDateFormat("dd/MM/yy", java.util.Locale.getDefault())
                outputFormat.format(date)
            }
        }
    } catch (e: Exception) {
        ""
    }
}

// Update the helper function to format dates for the more details section
private fun formatDate(isoDate: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        val date = inputFormat.parse(isoDate) ?: return isoDate

        val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        outputFormat.format(date)
    } catch (e: Exception) {
        isoDate
    }
}

// Helper function to navigate to chat
private fun navigateToChat(
    navController: NavController,
    currentUser: User,
    token: String,
    offer: Offre,
    scope: CoroutineScope,
    onError: (String) -> Unit = {} // Optional error callback
) {
    scope.launch {
        try {
            // Validate required fields - FIXED: Added null safety checks
            val entrepriseId = offer.createdBy?._id
            val offerId = offer.id

            if (entrepriseId.isNullOrEmpty()) {
                onError("Enterprise ID is missing")
                return@launch
            }

            if (offerId.isNullOrEmpty()) {
                onError("Offer ID is missing")
                return@launch
            }

            val chatRepository = ChatRepository()
            val createChatRequest = CreateChatRequest(
                entreprise = entrepriseId,
                offer = offerId
            )

            println("Creating chat for offer: $offerId, enterprise: $entrepriseId")

            val chatResponse = chatRepository.createOrGetChat(token, createChatRequest)

            println("Chat created successfully: ${chatResponse.id}")

            // FIXED: Ensure we're on main thread for navigation
            withContext(Dispatchers.Main) {
                navController.navigate("${Routes.ScreenChating}/${chatResponse.id}")
            }

        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("network", ignoreCase = true) == true -> "Network error. Please check your connection."
                e.message?.contains("timeout", ignoreCase = true) == true -> "Request timeout. Please try again."
                e.message?.contains("unauthorized", ignoreCase = true) == true -> "Authentication failed. Please login again."
                else -> "Failed to create chat: ${e.message ?: "Unknown error"}"
            }

            println("Error creating chat: ${e.message}")
            e.printStackTrace()

            // FIXED: Ensure error callback is called on main thread
            withContext(Dispatchers.Main) {
                onError(errorMessage)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenOffrePreview() {
    ScreenOffre(navController = rememberNavController())
}