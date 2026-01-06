package sim2.app.talleb_5edma.interfaces

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimeScreen(
    userName: String,
    onOpenCalendar: () -> Unit,
    onOpenAvailability: () -> Unit = {}
) {
    val RedDark = Color(0xFFD32F2F)
    val SoftGray = Color(0xFF888888)
    val CardBg = Color(0xFFFDFDFD)

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            "Gestion du temps",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = RedDark
        )

        Spacer(Modifier.height(16.dp))
        Text("Bonjour $userName ;", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text("Cette semaine", fontSize = 15.sp, color = SoftGray)
        Spacer(Modifier.height(12.dp))

        // Carte heures semaine
        Surface(
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.LightGray),
            color = CardBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Heures de job effectuées", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = 1f / 20f,
                    color = RedDark,
                    trackColor = Color(0xFFEAEAEA),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
                Spacer(Modifier.height(6.dp))
                Text("1h / 20h", color = SoftGray, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Divider(color = Color(0xFFEAEAEA))
                Spacer(Modifier.height(8.dp))
                Text("Cette semaine :", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column {
                        Text("Jobs effectués :", fontSize = 13.sp, color = SoftGray)
                        Text("1h", fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Cours planifiés :", fontSize = 13.sp, color = SoftGray)
                        Text("18h", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Aujourd’hui
        Text("Aujourd'hui - Lundi 7 Oct", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.LightGray),
            color = CardBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                TimeItem("9h-11h", "Cours UI/UX", RedDark)
                Spacer(Modifier.height(8.dp))
                TimeItem("14h-18h", "Job Café", RedDark)
                Spacer(Modifier.height(8.dp))
                TimeItem("19h", "Deadline", Color.Black)
            }
        }

        Spacer(Modifier.height(24.dp))

        // 🔹 Bouton "Voir Calendrier"
        Button(
            onClick = onOpenCalendar,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RedDark)
        ) {
            Text("Voir Calendrier", color = Color.White, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(12.dp))

        // 🔹 Bouton "Gérer mes disponibilités"
        OutlinedButton(
            onClick = onOpenAvailability,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = RedDark
            ),
            border = BorderStroke(1.5.dp, RedDark)
        ) {
            Text("Gérer mes disponibilités", color = RedDark, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun TimeItem(time: String, title: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.AccessTime,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text("$time  $title", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
