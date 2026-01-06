package sim2.app.talleb_5edma.interfaces

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Accent = Color(0xFFB71C1C)
private val Soft = Color(0xFF6B6B6B)
private val CardBg = Color(0xFFF6F6FB)

@Composable
fun ExamModeScreen() {
    var examModeEnabled by remember { mutableStateOf(true) }
    var blockOffers by remember { mutableStateOf(true) }
    var hideJobNotif by remember { mutableStateOf(false) }
    var keepAcceptedJobs by remember { mutableStateOf(false) }
    var revisionReminders by remember { mutableStateOf(false) }
    var breakSuggestions by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8FB))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {

        // Carte Mode examens
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardBg,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Mode examens",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = examModeEnabled,
                    onCheckedChange = { examModeEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Accent
                    )
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Titre "Tu peux personnaliser"
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Icon(
                Icons.Filled.Settings,
                contentDescription = null,
                tint = Accent
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Tu peux personnaliser",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        // Carte options
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CardBg,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ExamOptionRow(
                    label = "Bloquer nouvelles offres",
                    checked = blockOffers,
                    onCheckedChange = { blockOffers = it }
                )
                ExamOptionRow(
                    label = "Masquer notifications jobs",
                    checked = hideJobNotif,
                    onCheckedChange = { hideJobNotif = it }
                )
                ExamOptionRow(
                    label = "Conserver jobs acceptés",
                    checked = keepAcceptedJobs,
                    onCheckedChange = { keepAcceptedJobs = it }
                )
                ExamOptionRow(
                    label = "Rappels révision",
                    checked = revisionReminders,
                    onCheckedChange = { revisionReminders = it }
                )
                ExamOptionRow(
                    label = "Suggestions pauses",
                    checked = breakSuggestions,
                    onCheckedChange = { breakSuggestions = it }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Période examens (juste UI, pas encore de date picker)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = CardBg,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("Du: [15 Oct 2025 ▼]", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Text("Au:  [25 Oct 2025 ▼]", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(10.dp))
                Text("Durée: 11 jours", fontSize = 13.sp, color = Soft)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ExamOptionRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Accent,
                checkmarkColor = Color.White
            )
        )
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 14.sp)
    }
}
