package sim2.app.talleb_5edma.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import sim2.app.talleb_5edma.R
import sim2.app.talleb_5edma.models.CvStructuredResponse
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.collections.joinToString
import kotlin.text.isNullOrBlank

// --- Palette de Couleurs ---
private val PrimaryMagenta = Color(0xFFE91E63)
private val SecondaryText = Color(0xFF6B7280)
private val LightGrayBg = Color(0xFFF7F7F9)

/* -------------------------------------------------------------------------- */
/* MAIN COMPOSABLE : PAGE INDÉPENDANTE & CLEAN                                */
/* -------------------------------------------------------------------------- */
@Composable
fun CVAnalysisScreen(
    onCloseClick: () -> Unit,
    onExistingCVClick: () -> Unit = {},
    onGenerateCVClick: () -> Unit = {},
    onAnalysisSuccess: (CvStructuredResponse) -> Unit,
    viewModel: CvAiViewModel = viewModel()
) {
    val context = LocalContext.current
    var lastSelectedUri by remember { mutableStateOf<Uri?>(null) }
    val state: CvUiState = viewModel.uiState

    // 👉 IMPORTANT : on nettoie l’état à CHAQUE ouverture de la page
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    // Picker de fichier (PDF / image)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            lastSelectedUri = uri
            viewModel.uploadCv(context, uri)
        }
    }

    // Toute la page est scrollable → résultat jamais coupé
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Carte principale
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. En-tête haut
                TopHeader(onCloseClick = onCloseClick)

                Spacer(Modifier.height(24.dp))

                // 2. Texte & icône
                AnalysisHeader()

                Spacer(Modifier.height(32.dp))

                // 3. Zone upload
                UploadCVCard(
                    onUploadAreaClick = {
                        filePickerLauncher.launch("*/*") // "application/pdf" si tu veux forcer PDF
                    },
                    selectedFileName = state.selectedFileName
                )

                Spacer(Modifier.height(16.dp))

                // 4. Résultat JSON structuré -- déclenche la navigation si succès
                CvResultSection(
                    state = state,
                    onAnalysisSuccess = onAnalysisSuccess
                )

                Spacer(Modifier.height(24.dp))

                // 5. Boutons bas (CV existant / Générer CV IA)
                ActionButtons(
                    onExistingCVClick = {
                        filePickerLauncher.launch("*/*")
                        onExistingCVClick()
                    },
                    onGenerateCVClick = onGenerateCVClick
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* TOP HEADER (Titre + Fermer)                                                */
/* -------------------------------------------------------------------------- */
@Composable
fun TopHeader(onCloseClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Analyse CV IA",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        TextButton(
            onClick = onCloseClick,
            colors = ButtonDefaults.textButtonColors(contentColor = SecondaryText)
        ) {
            Text("Fermer", fontWeight = FontWeight.SemiBold)
        }
    }
}

/* -------------------------------------------------------------------------- */
/* HEADER AVEC ICÔNE LOUPE                                                    */
/* -------------------------------------------------------------------------- */
@Composable
fun AnalysisHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Analyse CV IA",
            tint = PrimaryMagenta,
            modifier = Modifier.size(64.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Analysez votre CV avec l'IA",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Générez ou analysez votre CV avec l'intelligence artificielle",
            fontSize = 14.sp,
            color = SecondaryText,
            textAlign = TextAlign.Center
        )
    }
}

/* -------------------------------------------------------------------------- */
/* CARTE D'UPLOAD                                                             */
/* -------------------------------------------------------------------------- */
@Composable
fun UploadCVCard(
    onUploadAreaClick: () -> Unit,
    selectedFileName: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(1.dp, PrimaryMagenta.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable { onUploadAreaClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightGrayBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = SecondaryText.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = selectedFileName ?: "Ajoutez votre CV",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* SECTION RÉSULTAT (NAME, EXPERIENCE, EDUCATION, SKILLS)                     */
/* -------------------------------------------------------------------------- */
@Composable
fun CvResultSection(
    state: CvUiState,
    onAnalysisSuccess: (CvStructuredResponse) -> Unit
) {
    if (state.isLoading) {
        // Loading uniquement pour upload au début
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = PrimaryMagenta)
        }
    } else if (state.error != null) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Erreur : ${state.error}",
            color = Color.Red,
            fontSize = 14.sp
        )
    } else if (state.result != null) {
        // Au lieu d'afficher, on notifie MainActivity pour naviguer
        LaunchedEffect(state.result) {
            onAnalysisSuccess(state.result)
        }
    }
}

/* -------------------------------------------------------------------------- */
/* BOUTONS D'ACTION EN BAS                                                    */
/* -------------------------------------------------------------------------- */
@Composable
fun ActionButtons(
    onExistingCVClick: () -> Unit,
    onGenerateCVClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onExistingCVClick,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, PrimaryMagenta.copy(alpha = 0.7f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = PrimaryMagenta
            )
        ) {
            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("CV existant", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.width(16.dp))

        Button(
            onClick = onGenerateCVClick,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryMagenta,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Générer CV IA", fontWeight = FontWeight.SemiBold)
        }
    }
}

/* -------------------------------------------------------------------------- */
/* PREVIEW SIMPLE                                                             */
/* -------------------------------------------------------------------------- */
@Preview(showBackground = true)
@Composable
fun PreviewCVAnalysisScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Image(
            painter = painterResource(id = R.drawable.back),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        CVAnalysisScreen(
            onCloseClick = { },
            onExistingCVClick = { },
            onGenerateCVClick = { },
            onAnalysisSuccess = { }
        )
    }
}
