package sim2.app.talleb_5edma.screens
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun OfferComparisonScreen(navController: NavController = rememberNavController()) {
    val scrollState = rememberScrollState()
    var showSecondOffer by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFBBDEFB)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }, // Go back to offer screen
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFD32F2F)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Talleb 5edma",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            }

            // Title
            Text(
                text = "Comparaison d'Offres",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.Center,
                color = Color(0xFFFF4081)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Comparison Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .weight(1f), // CHANGE 2: Added .weight(1f) to make the card fill space
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    // CHANGE 3: Moved .verticalScroll(scrollState) inside the card
                    modifier = Modifier.verticalScroll(scrollState)
                ) {
                    // Header Row with Gradient and Offers
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFF4081),
                                        Color(0xFFFF6E40)
                                    )
                                )
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Top
                        ) {
                            // Spacer for alignment
                            Spacer(modifier = Modifier.weight(1f))

                            // Offre A
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier.size(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "A",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD32F2F)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF4CAF50)
                                ) {
                                    Text(
                                        text = "Restaurant",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Offre B
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (showSecondOffer) {
                                    Box {
                                        Surface(
                                            modifier = Modifier.size(56.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color.White
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "B",
                                                    fontSize = 28.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFD32F2F)
                                                )
                                            }
                                        }
                                        // Remove button
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .align(Alignment.TopEnd)
                                                .offset(x = 6.dp, y = (-6).dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFD32F2F))
                                                .clickable { showSecondOffer = false },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFFB300)
                                    ) {
                                        Text(
                                            text = "Café",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White)
                                            .clickable { showSecondOffer = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add offer",
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Feature Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF212121))
                            .padding(vertical = 10.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = "CRITÈRE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "OFFRE A",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            if (showSecondOffer) {
                                Text(
                                    text = "OFFRE B",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Match Score
                    ComparisonFeatureRow(
                        label = "Correspondance",
                        valueA = "80%",
                        valueB = "60%",
                        showSecondOffer = showSecondOffer,
                        valueAColor = Color(0xFF00C853),
                        valueBColor = Color(0xFFFF6F00),
                        isScore = true
                    )

                    // Study Compatibility Section
                    SectionHeader(text = "COMPATIBILITÉ AVEC LES ÉTUDES")

                    ComparisonFeatureRow(
                        label = "Horaire &\nFlexibilité",
                        valueA = "Flexible",
                        valueB = "Flexible",
                        showSecondOffer = showSecondOffer,
                        isText = true
                    )

                    ComparisonFeatureRow(
                        label = "Temps de trajet",
                        valueA = "15 min",
                        valueB = "45 min",
                        showSecondOffer = showSecondOffer,
                        backgroundColor = Color(0xFFFFF8E1)
                    )

                    ComparisonFeatureRow(
                        label = "Charge de travail",
                        valueA = "3h",
                        valueB = "8h",
                        showSecondOffer = showSecondOffer
                    )

                    // Compensation Section
                    SectionHeader(text = "RÉMUNÉRATION & AVANTAGES")

                    ComparisonFeatureRow(
                        label = "Salaire",
                        valueA = "40d",
                        valueB = "70d",
                        showSecondOffer = showSecondOffer
                    )

                    ComparisonFeatureRow(
                        label = "Avantages",
                        valueA = "Tickets resto",
                        valueB = "Aucun",
                        showSecondOffer = showSecondOffer,
                        backgroundColor = Color(0xFFFFF8E1),
                        isText = true
                    )
                }
            }

            // CHANGE 4: Removed the final Spacer
            // Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F),
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun ComparisonFeatureRow(
    label: String,
    valueA: String,
    valueB: String,
    showSecondOffer: Boolean,
    backgroundColor: Color = Color.White,
    valueAColor: Color? = null,
    valueBColor: Color? = null,
    isScore: Boolean = false,
    isText: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF424242),
            modifier = Modifier.weight(1f),
            lineHeight = 16.sp
        )

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = valueA,
                fontSize = if (isScore) 16.sp else 13.sp,
                fontWeight = if (isScore) FontWeight.Bold else FontWeight.Medium,
                color = valueAColor ?: if (isText) Color(0xFF1976D2) else Color(0xFF424242),
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (showSecondOffer) {
                Text(
                    text = valueB,
                    fontSize = if (isScore) 16.sp else 13.sp,
                    fontWeight = if (isScore) FontWeight.Bold else FontWeight.Medium,
                    color = valueBColor ?: if (isText) Color(0xFF757575) else Color(0xFF424242),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompareScreenPreview() {
    val navController = rememberNavController()
    OfferComparisonScreen(navController)
}