package sim2.app.talleb_5edma.interfaces

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* Palette nécessaire uniquement pour cette page */
private val RedDark    = Color(0xFFD32F2F)
private val GlassWhite = Color(0xBBFFFFFF)

@Composable
fun FilterScreen(
    onOpenQr: () -> Unit = {},
    onOpenMap: () -> Unit = {},
    onOpenAiCv: () -> Unit = {}
) {
    // Pas de TopBar ici : il vient de MainActivity
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Boutons "glass" premium
        FilterButton(icon = Icons.Filled.QrCode,  label = "QR",    onClick = onOpenQr)
        Spacer(Modifier.height(14.dp))
        FilterButton(icon = Icons.Filled.Map,     label = "Map",   onClick = onOpenMap)
        Spacer(Modifier.height(14.dp))
        FilterButton(icon = Icons.Filled.SmartToy,label = "AI-CV", onClick = onOpenAiCv)

        Spacer(Modifier.height(20.dp))

        // Texte d’explication trilingue
        Text(
            text =
                "QR : scanner un code pour trouver des offres rapidement.\n" +
                        "Map : voir les lieux des entreprises ou offres proches de vous.\n" +
                        "AI-CV : générer ou analyser votre CV via IA.\n\n" +
                        "QR: البحث عن العروض عبر مسح سريع.\n" +
                        "Maps: عرض مواقع الشركات أو العروض القريبة.\n" +
                        "AI-CV: إنشاء أو تحليل السيرة الذاتية باستخدام الذكاء الاصطناعي.",
            color = Color(0xFF636363),
            fontSize = 13.sp,
            textAlign = TextAlign.Start,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun FilterButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = GlassWhite,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0x22B71C1C)),
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = RedDark)
            Spacer(Modifier.width(12.dp))
            Text(label, color = RedDark, fontWeight = FontWeight.SemiBold)
        }
    }
}
