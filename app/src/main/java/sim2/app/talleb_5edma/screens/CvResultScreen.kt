package sim2.app.talleb_5edma.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import sim2.app.talleb_5edma.models.CvStructuredResponse

private val PrimaryMagenta = Color(0xFFE91E63)

@Composable
fun CvResultScreen(
    cvResult: CvStructuredResponse,
    onBackClick: () -> Unit,
    viewModel: CvAiViewModel = viewModel()
) {
    val context = LocalContext.current
    val state = viewModel.uiState

    // Set the result in the ViewModel so saveProfileFromCv can use it
    // NOTE: In a real app, passing complex objects via ViewModel shared state or ID is better,
    // but here we just set it locally or rely on the VM being scoped if shared, 
    // OR we modify saveProfileFromCv to accept the object. 
    // For now we will update saveProfileFormCv in VM to take the object or we update the state here.
    // Let's modify the VM to accept the object in the next step, but for UI, we pass it.

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F9))
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFF7F7F9))
                    )
                )
                .padding(top = 40.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Résultat Analyse",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Success Header
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "Analyse terminée avec succès !",
                        color = Color(0xFF1B5E20),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Result Content
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    ResultItem("Nom", cvResult.name)
                    ResultItem("Email", cvResult.email)
                    ResultItem("Téléphone", cvResult.phone)

                    Divider(Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))

                    ResultList("Expérience", cvResult.experience)
                    ResultList("Formation", cvResult.education)
                    
                    if (cvResult.skills.isNotEmpty()) {
                         Spacer(Modifier.height(16.dp))
                         Text("Compétences", fontWeight = FontWeight.Bold, color = PrimaryMagenta)
                         Spacer(Modifier.height(8.dp))
                         Text(cvResult.skills.joinToString(", "), fontSize = 14.sp, color = Color.Black)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = { viewModel.saveProfileFromCv(context, cvResult) }, // Modified signature
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryMagenta,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Enregistrer dans mon profil", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            
            if (state.error != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Erreur: ${state.error}",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun ResultItem(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Column(Modifier.padding(bottom = 12.dp)) {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        }
    }
}

@Composable
fun ResultList(label: String, items: List<String>) {
    if (items.isNotEmpty()) {
        Column(Modifier.padding(bottom = 16.dp)) {
            Text(label, fontWeight = FontWeight.Bold, color = PrimaryMagenta)
            Spacer(Modifier.height(8.dp))
            items.forEach {
                Row(Modifier.padding(bottom = 4.dp)) {
                    Text("•", color = PrimaryMagenta, modifier = Modifier.padding(end = 8.dp))
                    Text(it, fontSize = 14.sp, color = Color.Black)
                }
            }
        }
    }
}
