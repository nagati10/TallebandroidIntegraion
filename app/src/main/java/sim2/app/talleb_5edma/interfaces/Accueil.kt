package com.example.projet1.interfaces

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/* ====== Palette ====== */
private val LavenderBgTop   = Color(0xFFF7F4FF)
private val LavenderBgMid   = Color(0xFFF1ECFF)
private val LavenderChip    = Color(0x88FFFFFF)
private val CardBorder      = Color(0xFFE6DEFF)
private val SoftText        = Color(0xFF6F6B80)
private val TitleText       = Color(0xFF1F1B2E)
private val Purple          = Color(0xFF7C4DFF)
private val PurpleDark      = Color(0xFF5E35B1)

enum class Shift { Jour, Nuit }
enum class JobType { JOB, STAGE, FREELANCE }

/* ===================== Model ===================== */
data class JobOffer(
    val id: String,
    val title: String,
    val company: String,
    val city: String,
    val type: JobType,
    val salaryRange: String,
    val date: String,
    val days: Int,
    val shift: Shift,
    val liked: Boolean = false,
    val description: String =
        "Nous recherchons un développeur junior pour rejoindre notre équipe..."
)

/* ===================== State ===================== */
@Stable
class AccueilState(offers: List<JobOffer> = demoOffers()) {

    var query by mutableStateOf("")
    var list  by mutableStateOf(offers)

    var onlyPopular   by mutableStateOf(false)
    var onlyFavorites by mutableStateOf(false)

    var selectedType: JobType? by mutableStateOf(null)
    var selectedCity: String?  by mutableStateOf(null)

    val allCities: List<String>
        get() = list.map { it.city }.distinct()

    val filtered: List<JobOffer>
        get() = list
            .filter {
                if (query.isBlank()) true
                else it.title.contains(query, true) ||
                        it.company.contains(query, true) ||
                        it.city.contains(query, true)
            }
            .filter { selectedType?.let { t -> it.type == t } ?: true }
            .filter { selectedCity?.let { c -> it.city == c } ?: true }
            .filter { if (onlyPopular) it.days <= 3 else true }
            .filter { if (onlyFavorites) it.liked else true }

    fun toggleLike(id: String) {
        list = list.map { if (it.id == id) it.copy(liked = !it.liked) else it }
    }
}

@Composable
fun rememberAccueilState(): AccueilState = remember { AccueilState() }

/* ===================== Screen ===================== */
@Composable
fun AccueilContent(
    state: AccueilState = rememberAccueilState(),
    onOpenFilter: () -> Unit = {}
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to LavenderBgTop,
                    0.7f to LavenderBgMid,
                    1f to Color.White
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            /* ====== Barre de recherche ====== */
            item {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { state.query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    placeholder = {
                        Text(
                            "Rechercher un job, stage, freelance…",
                            color = SoftText
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Red)
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CardBorder,
                        unfocusedBorderColor = CardBorder,
                        cursorColor = Color.Red,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            /* ====== UNE SEULE ROW : Types + Villes + Filtre + Populaire + Favorites ====== */
            item {
                FiltersAndChipsRow(
                    state = state,
                    onOpenFilter = onOpenFilter
                )
            }

            /* ====== Bannière rouge “Nouvelles opportunités” ====== */
            item {
                NewOpportunitiesBanner(count = state.filtered.size)
            }


            /* ====== Cards style “capture 2” ====== */
            items(state.filtered, key = { it.id }) { offer ->
                JobCardCapture2(
                    offer = offer,
                    onLike = { state.toggleLike(offer.id) },
                    onShare = { /* TODO share */ },
                    onClick = { /* TODO détails */ }
                )
            }
        }
    }
}

/* ===================== Row combinée ===================== */
@Composable
private fun FiltersAndChipsRow(
    state: AccueilState,
    onOpenFilter: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Dropdown Type
        DropdownFilter(
            label = "Tous les types",
            current = when (state.selectedType) {
                JobType.JOB        -> "Job"
                JobType.STAGE      -> "Stage"
                JobType.FREELANCE  -> "Freelance"
                null               -> "Tous les types"
            },
            items = listOf("Tous", "Job", "Stage", "Freelance"),
            onSelectedIndex = {
                state.selectedType = when (it) {
                    0 -> null
                    1 -> JobType.JOB
                    2 -> JobType.STAGE
                    3 -> JobType.FREELANCE
                    else -> null
                }
            }
        )

        // Dropdown Ville
        val cityItems = listOf("Toutes les villes") + state.allCities
        DropdownFilter(
            label = "Toutes les villes",
            current = state.selectedCity ?: "Toutes les villes",
            items = cityItems,
            onSelectedIndex = { index ->
                state.selectedCity = if (index == 0) null else cityItems[index]
            }
        )

        // Chip Filtre (ouvre page filtre)
        GlassChip("Filtre", Icons.Filled.FilterList) {
            onOpenFilter()
        }

        // Chip Populaire
        ToggleGlassChip("Populaire", state.onlyPopular) {
            state.onlyPopular = it
        }

        // Chip Favorites
        ToggleGlassChip("Favorites", state.onlyFavorites) {
            state.onlyFavorites = it
        }
    }
}

/* ===================== Bannière rouge ===================== */
@Composable
private fun NewOpportunitiesBanner(count: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFF4D4D),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Nouvelles opportunités",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    "$count offres pour vous",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
            }
            Icon(
                Icons.Filled.TrendingUp,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

/* ===================== Dropdown générique ===================== */
@Composable
private fun DropdownFilter(
    label: String,
    current: String,
    items: List<String>,
    onSelectedIndex: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = Modifier
                .height(40.dp)
                .clickable { expanded = true },
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, CardBorder),
            color = Color.White,
            shadowElevation = 0.dp
        ) {
            Row(
                Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = current,
                    fontSize = 13.sp,
                    color = TitleText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = SoftText
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEachIndexed { index, text ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        expanded = false
                        onSelectedIndex(index)
                    }
                )
            }
        }
    }
}

/* ===================== Chips ===================== */
@Composable
private fun GlassChip(text: String, icon: ImageVector? = null, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = LavenderChip,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 0.dp
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, null, Modifier.size(16.dp), tint = PurpleDark)
                Spacer(Modifier.width(6.dp))
            }
            Text(text, fontSize = 13.sp, color = TitleText)
        }
    }
}

@Composable
private fun ToggleGlassChip(text: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Surface(
        onClick = { onChange(!checked) },
        shape = RoundedCornerShape(24.dp),
        color = if (checked) Color(0xFFEDE4FF) else LavenderChip,
        border = BorderStroke(1.dp, if (checked) Purple else CardBorder)
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (checked) {
                Box(
                    Modifier
                        .size(16.dp)
                        .background(Purple.copy(alpha = .15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        null,
                        Modifier.size(12.dp),
                        tint = Purple
                    )
                }
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text,
                fontSize = 13.sp,
                color = if (checked) Purple else TitleText
            )
        }
    }
}

/* ===================== Card style capture 2 ===================== */
@Composable
fun JobCardCapture2(
    offer: JobOffer,
    onLike: () -> Unit,
    onShare: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Column {

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(0xFFFF4D4D))
            )

            Column(Modifier.padding(16.dp)) {

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        offer.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF202020),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(8.dp))

                    JobTypeBadge(offer.type)
                }

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    CompanyAvatar(offer.company)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        offer.company,
                        fontSize = 14.sp,
                        color = SoftText
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        offer.city,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    Spacer(Modifier.width(12.dp))

                    Icon(
                        Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        offer.salaryRange,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    offer.description,
                    fontSize = 13.sp,
                    color = Color(0xFF707070),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        offer.date,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    Spacer(Modifier.weight(1f))

                    IconButton(onClick = onLike) {
                        Icon(
                            imageVector = if (offer.liked)
                                Icons.Filled.Favorite
                            else
                                Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            tint = if (offer.liked) Color.Red else Color.Gray
                        )
                    }

                    IconButton(onClick = onShare) {
                        Icon(
                            Icons.Filled.IosShare,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

/* ===== Badges / Avatar ===== */
@Composable
private fun JobTypeBadge(type: JobType) {
    val (text, color) = when (type) {
        JobType.JOB        -> "Job" to Color(0xFF00C853)
        JobType.STAGE      -> "Stage" to Color(0xFF2962FF)
        JobType.FREELANCE  -> "Freelance" to Color(0xFFFFA000)
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun CompanyAvatar(seedText: String) {
    val colors = listOf(
        0xFFFFCDD2, 0xFFFFE0B2, 0xFFFFF9C4,
        0xFFC8E6C9, 0xFFBBDEFB
    ).map { Color(it) }
    val idx = abs(seedText.hashCode()) % colors.size
    val bg = colors[idx]
    val initials = seedText
        .split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initials,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB71C1C),
            fontSize = 13.sp
        )
    }
}

/* -------- Demo data -------- */
private fun demoOffers() = listOf(
    JobOffer(
        id = "1",
        title = "Développeur Web Junior",
        company = "Tech Solutions",
        city = "Paris",
        type = JobType.JOB,
        salaryRange = "30–35k€/an",
        date = "10/11/2024",
        days = 7,
        shift = Shift.Jour
    ),
    JobOffer(
        id = "2",
        title = "Stage Marketing Digital",
        company = "StartUp Innovante",
        city = "Lyon",
        type = JobType.STAGE,
        salaryRange = "800–1000€/mois",
        date = "12/11/2024",
        days = 5,
        shift = Shift.Jour
    ),
    JobOffer(
        id = "3",
        title = "Technicien support informatique",
        company = "Esprit Services",
        city = "Tunis",
        type = JobType.JOB,
        salaryRange = "Selon profil",
        date = "14/11/2024",
        days = 2,
        shift = Shift.Nuit
    ),
    JobOffer(
        id = "4",
        title = "Designer UI/UX Freelance",
        company = "Creative Agency",
        city = "Marseille",
        type = JobType.FREELANCE,
        salaryRange = "300€/jour",
        date = "15/11/2024",
        days = 1,
        shift = Shift.Jour
    )
)
