package sim2.app.talleb_5edma.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import sim2.app.talleb_5edma.BottomDest
import sim2.app.talleb_5edma.R
import sim2.app.talleb_5edma.Routes
import sim2.app.talleb_5edma.util.forceClearAllData

private val DarkBackground = Color(0xFF2A2A2A)
private val CardBackground = Color(0xFF333333)
private val TextPrimary = Color(0xFFECECEC)
private val TextSecondary = Color(0xFFCCCCCC)
private val RedAccent = Color(0xFFCF1919)
private val RedButton = Color(0xFFDC2626)

@Composable
fun ScreenOffre(modifier: Modifier = Modifier, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(54.dp)
        ) {
            HeaderSection(navController=navController)
            SalaryTypeTags()
            ContentSection()
        }
        Box(
            modifier = modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(DarkBackground)
                .padding(horizontal = 21.dp, vertical = 16.dp)
        ) {
            BottomActionButtons(navController)
        }
    }
}
@Composable
fun HeaderSection(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(295.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray)
        )
        // Image(
        //     painter = painterResource(id = R.drawable.your_image_name),
        //     contentDescription = "Header Image",
        //     contentScale = ContentScale.Crop,
        //     modifier = Modifier.fillMaxSize()
        // )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        TopNavigationIcons(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            navController
        )

        NavigationArrow(
            isLeft = true,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 15.dp)
        )
        NavigationArrow(
            isLeft = false,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 15.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Assistant de chantier à centre ville Tunis",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.widthIn(max = 250.dp)
                )
                DistanceIndicator()
            }
        }
    }
}

@Composable
fun ContentSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-30).dp)
            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
            .background(DarkBackground)
            .padding(all = 21.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        MatchPercentageCard()
        JobDescriptionSection()
        RequirementsSection()
        MoreDetailsSection()
        CompanyInfoSection()
        SimilarOffersSection()
    }
}


@Composable
fun TopNavigationIcons(modifier: Modifier = Modifier,navController: NavController) {

    val ctx = LocalContext.current
    var isLike by remember { mutableStateOf(false) }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleIconButton(onClick = {
            navController.navigate(BottomDest.Home.route)
        }) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Row {
            IconButton(
                onClick = { isLike = !isLike },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (!isLike)
                            Color.Black.copy(alpha = 0.22f)
                        else
                            Color.White
                    ),
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
            ){
                Icon(
                    if (!isLike) Icons.Default.FavoriteBorder else Icons.Filled.Favorite,
                    contentDescription = "Favorite",
                    tint = if (!isLike) Color.White else Color.Red,
                    modifier = Modifier
                        .size(34.dp)
                )
            }
            Spacer(Modifier.width(20.dp))
            CircleIconButton(onClick = { /*TODO*/ }) {
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
fun NavigationArrow(modifier: Modifier = Modifier, isLeft: Boolean) {
    CircleIconButton(onClick = { /*TODO*/ }, modifier = modifier) {
        Icon(
            imageVector = if (isLeft) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = if (isLeft) "Previous" else "Next",
            tint = Color.White
        )
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
fun DistanceIndicator() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = "Location",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "220 m",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun SalaryTypeTags() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Tag(text = "45 – 60 DT")
        Tag(text = "temps partiel")
    }
}

@Composable
fun Tag(text: String) {
    Text(
        text = text,
        color = Color.Black,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFFBBBBBB))
            .padding(horizontal = 12.dp, vertical = 3.dp)
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
                text = "Votre expertise en planification avec MS Project et les diagrammes de Gantt correspond parfaitement au poste. L'offre salariale est au-dessus de vos attentes , bien que la charge de travail soit élevée.",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun JobDescriptionSection() {
    Text(
        text = "Nous recherchons un Assistant de chantier pour rejoindre notre équipe dynamique à Tunis.",
        color = TextPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp
    )
}

@Composable
fun RequirementsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Requirements",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "• Maîtrise des outils de planification (MS Project, Gantt, etc.)\n" +
                    "• Connaissance des réglementations du bâtiment et de la sécurité\n" +
                    "• Niveau du français : B2\n" +
                    "• Permis B indispensable",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun MoreDetailsSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            tint = Color.White
        )
    }
}

@Composable
fun CompanyInfoSection() {
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
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Gray)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Bâtiments Plus",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Entreprise générale de construction",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "4.8 rating",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SimilarOffersSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Offres similaires",
            color = TextPrimary,
            fontSize = 16.sp, // Taille conservée
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
            // Placeholder Logo
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
fun BottomActionButtons(navController: NavController) { // Added navController parameter
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = {
                // Navigate to comparison screen
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
                // Navigate to chat screen
                navController.navigate(Routes.ScreenChating)
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
}

@Preview(showBackground = true)
@Composable
fun ScreenOffrePreview() {
    ScreenOffre(navController = rememberNavController())
}