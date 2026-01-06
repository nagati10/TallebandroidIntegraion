package sim2.app.talleb_5edma.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Data Class to Hold Criteria Info ---

data class MatchCriterion(
    val name: String,
    val score: Int,
    val icon: ImageVector,
    val color: Color
)

// --- The Main Screen Composable ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchScoreScreen() {
    // Define the criteria list (you would get this from your matching algorithm)
    val criteria = listOf(
        MatchCriterion("Location", 100, Icons.Default.LocationOn, Color(0xFF_66D9E8)),
        MatchCriterion("Availability", 100, Icons.Default.Schedule, Color(0xFF_63E6BE)),
        MatchCriterion("Skills", 80, Icons.Default.Construction, Color(0xFF_FFD066)),
        MatchCriterion("Pay Rate", 100, Icons.Default.AttachMoney, Color(0xFF_FFAB66)),
        MatchCriterion("Interests", 60, Icons.Default.Favorite, Color(0xFF_FF8787)),
        MatchCriterion("Soft Skills", 75, Icons.Default.Psychology, Color(0xFF_E599F7))
    )

    // Define the gradient background
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF_0B2244), Color(0xFF_1A4A7A))
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Your Match Score",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(
                            imageVector = Icons.Default.TrackChanges,
                            contentDescription = "Target",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.background(backgroundGradient)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. Total Match Score (The big "95%")
            TotalScoreSection(totalScore = 95)

            Spacer(modifier = Modifier.height(32.dp))

            // 2. Grid of Matching Criteria
            CriteriaGrid(criteria = criteria)

            Spacer(modifier = Modifier.weight(1f)) // Pushes buttons to the bottom

            // 3. Action Buttons
            ActionButtons()
        }
    }
}

// --- Helper Composable for the Total Score ---

@Composable
fun TotalScoreSection(totalScore: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF_1F9BFF), Color(0xFF_106AB4)),
                    radius = 280f
                )
            )
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$totalScore%",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Total Match",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

// --- Helper Composable for the Criteria Grid ---

@Composable
fun CriteriaGrid(criteria: List<MatchCriterion>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // We'll use 3 columns
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(criteria) { criterion ->
            CriteriaCard(criterion = criterion)
        }
    }
}

// --- Helper Composable for a Single Criterion Card ---

@Composable
fun CriteriaCard(criterion: MatchCriterion) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF_2A3B5A).copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(criterion.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = criterion.icon,
                    contentDescription = criterion.name,
                    tint = criterion.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = criterion.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${criterion.score}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// --- Helper Composable for the Bottom Buttons ---

@Composable
fun ActionButtons() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { /* TODO: View Job Details */ },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF_1F9BFF)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Back To Job Details",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = { /* TODO: Show logic breakdown */ }) {
            Text(
                text = "Update your Prefrences",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}

// --- Preview Composable for Android Studio ---

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun MatchScoreScreenPreview() {
    // You would typically wrap this in your app's theme
    // For preview, we'll just call the screen directly
    MatchScoreScreen()
}