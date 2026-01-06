package sim2.app.talleb_5edma.interfaces

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Accent = Color(0xFFB71C1C)
private val CardBg = Color(0xFFF7F7F9)
private val Border = Color(0xFFE0E0E4)

@Composable
fun PreferenceWizardScreen(
    onClose: () -> Unit = {},
    onFinished: () -> Unit = {}
) {
    var currentStep by remember { mutableStateOf(0) } // 0..4

    // État des choix (tu pourras les connecter à ton backend plus tard)
    var studyLevel by remember { mutableStateOf<String?>(null) }
    var studyField by remember { mutableStateOf<String?>(null) }

    var searchType by remember { mutableStateOf<String?>(null) }
    var mainMotivation by remember { mutableStateOf<String?>(null) }

    val maxSoftSkills = 2
    var selectedSoftSkills by remember { mutableStateOf(setOf<String>()) }

    var arabicLevel by remember { mutableStateOf<String?>(null) }
    var frenchLevel by remember { mutableStateOf<String?>(null) }
    var englishLevel by remember { mutableStateOf<String?>(null) }

    var selectedInterests by remember { mutableStateOf(setOf<String>()) }
    var hasSecondHobby by remember { mutableStateOf(false) }

    val titles = listOf(
        "Informations académiques",
        "Préférences de recherche",
        "Compétences",
        "Langues",
        "Centres d'intérêt"
    )

    val stepsCount = titles.size

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F4F7))
                .padding(padding)
        ) {
            // Barre d'étape
            StepHeader(
                stepIndex = currentStep,
                total = stepsCount,
                title = titles[currentStep]
            )

            // Contenu scrollable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardBg,
                    tonalElevation = 0.dp,
                    border = BorderStroke(1.dp, Border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(16.dp)
                    ) {
                        when (currentStep) {
                            0 -> AcademicStep(
                                studyLevel = studyLevel,
                                onSelectStudyLevel = { studyLevel = it },
                                studyField = studyField,
                                onSelectStudyField = { studyField = it }
                            )
                            1 -> SearchPreferenceStep(
                                searchType = searchType,
                                onSelectSearchType = { searchType = it },
                                motivation = mainMotivation,
                                onSelectMotivation = { mainMotivation = it }
                            )
                            2 -> SoftSkillsStep(
                                selected = selectedSoftSkills,
                                maxSelected = maxSoftSkills,
                                onToggle = { label ->
                                    selectedSoftSkills =
                                        if (label in selectedSoftSkills) {
                                            selectedSoftSkills - label
                                        } else {
                                            if (selectedSoftSkills.size < maxSoftSkills)
                                                selectedSoftSkills + label
                                            else selectedSoftSkills
                                        }
                                }
                            )
                            3 -> LanguagesStep(
                                arabicLevel = arabicLevel,
                                frenchLevel = frenchLevel,
                                englishLevel = englishLevel,
                                onArabicChange = { arabicLevel = it },
                                onFrenchChange = { frenchLevel = it },
                                onEnglishChange = { englishLevel = it }
                            )
                            4 -> InterestsStep(
                                selected = selectedInterests,
                                onToggle = { label ->
                                    selectedInterests =
                                        if (label in selectedInterests)
                                            selectedInterests - label
                                        else
                                            selectedInterests + label
                                },
                                hasSecondHobby = hasSecondHobby,
                                onSecondHobbyChange = { hasSecondHobby = it }
                            )
                        }
                    }
                }
            }

            // Boutons bas
            BottomNavButtons(
                stepIndex = currentStep,
                total = stepsCount,
                onPrevious = {
                    if (currentStep == 0) onClose() else currentStep--
                },
                onNext = {
                    if (currentStep == stepsCount - 1) {
                        // TODO : envoyer les données au backend
                        onFinished()
                    } else {
                        currentStep++
                    }
                }
            )
        }
    }
}

/* ------------------ HEADER ------------------ */

@Composable
private fun StepHeader(stepIndex: Int, total: Int, title: String) {
    Column(Modifier.fillMaxWidth()) {
        // petite barre rouge en haut
        LinearProgressIndicator(
            progress = (stepIndex + 1) / total.toFloat(),
            color = Accent,
            trackColor = Color(0xFFE5E5EA),
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Étape ${stepIndex + 1} sur $total",
                fontSize = 13.sp,
                color = Color.Gray
            )
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
        Spacer(Modifier.height(12.dp))
    }
}

/* ------------------ ÉTAPE 1 : ACADÉMIQUE ------------------ */

@Composable
private fun AcademicStep(
    studyLevel: String?,
    onSelectStudyLevel: (String) -> Unit,
    studyField: String?,
    onSelectStudyField: (String) -> Unit
) {
    Text("Informations académiques", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(Modifier.height(16.dp))

    Text("Niveau d'étude", fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SelectRow(
            items = listOf("Licence 1", "Licence 2"),
            selected = studyLevel,
            onSelect = onSelectStudyLevel
        )
        SelectRow(
            items = listOf("Licence 3", "Ingénierie"),
            selected = studyLevel,
            onSelect = onSelectStudyLevel
        )
        SelectRow(
            items = listOf("Mastère", "Autre"),
            selected = studyLevel,
            onSelect = onSelectStudyLevel
        )
    }

    Spacer(Modifier.height(20.dp))

    Text("Domaine d'étude", fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SelectRow(
            items = listOf("Informatique", "Infirmier"),
            selected = studyField,
            onSelect = onSelectStudyField
        )
        SelectRow(
            items = listOf("Médecine", "Mécanique"),
            selected = studyField,
            onSelect = onSelectStudyField
        )
        SelectRow(
            items = listOf("Électrique", "Autre"),
            selected = studyField,
            onSelect = onSelectStudyField
        )
    }
}

/* ------------------ ÉTAPE 2 : PRÉFÉRENCES RECHERCHE ------------------ */

@Composable
private fun SearchPreferenceStep(
    searchType: String?,
    onSelectSearchType: (String) -> Unit,
    motivation: String?,
    onSelectMotivation: (String) -> Unit
) {
    Text("Vos préférences de recherche", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(Modifier.height(16.dp))

    Text("Que cherchez-vous ?", fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SelectRow(
            items = listOf("Job", "Stage"),
            selected = searchType,
            onSelect = onSelectSearchType
        )
        SelectRow(
            items = listOf("Mission freelance"),
            selected = searchType,
            onSelect = onSelectSearchType,
            singleRow = true
        )
    }

    Spacer(Modifier.height(20.dp))

    Text("Motivation principale", fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SelectRow(
            items = listOf("Argent"),
            selected = motivation,
            onSelect = onSelectMotivation,
            singleRow = true
        )
        SelectRow(
            items = listOf("Expérience"),
            selected = motivation,
            onSelect = onSelectMotivation,
            singleRow = true
        )
        SelectRow(
            items = listOf("Enrichissement du CV"),
            selected = motivation,
            onSelect = onSelectMotivation,
            singleRow = true
        )
    }
}

/* ------------------ ÉTAPE 3 : SOFT SKILLS ------------------ */

@Composable
private fun SoftSkillsStep(
    selected: Set<String>,
    maxSelected: Int,
    onToggle: (String) -> Unit
) {
    Text("Vos compétences", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(Modifier.height(6.dp))
    Text(
        "Sélectionnez $maxSelected soft skills",
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    )
    Spacer(Modifier.height(4.dp))
    Text(
        "Vous pouvez sélectionner jusqu'à $maxSelected compétences",
        fontSize = 12.sp,
        color = Color.Gray
    )

    Spacer(Modifier.height(16.dp))

    val skills = listOf(
        "Communication", "Organisation",
        "Sérieux", "Adaptabilité",
        "Travail d'équipe", "Leadership",
        "Créativité", "Résolution de problèmes"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        skills.chunked(2).forEach { row ->
            SelectRow(
                items = row,
                selectedItems = selected,
                onToggle = onToggle
            )
        }
    }
}

/* ------------------ ÉTAPE 4 : LANGUES ------------------ */

@Composable
private fun LanguagesStep(
    arabicLevel: String?,
    frenchLevel: String?,
    englishLevel: String?,
    onArabicChange: (String) -> Unit,
    onFrenchChange: (String) -> Unit,
    onEnglishChange: (String) -> Unit
) {
    Text("Niveaux linguistiques", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(Modifier.height(16.dp))

    LanguageSection(
        label = "Arabe",
        selected = arabicLevel,
        onSelect = onArabicChange
    )
    Spacer(Modifier.height(14.dp))
    LanguageSection(
        label = "Français",
        selected = frenchLevel,
        onSelect = onFrenchChange
    )
    Spacer(Modifier.height(14.dp))
    LanguageSection(
        label = "Anglais",
        selected = englishLevel,
        onSelect = onEnglishChange
    )
}

@Composable
private fun LanguageSection(
    label: String,
    selected: String?,
    onSelect: (String) -> Unit
) {
    Text(label, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(6.dp))

    val levels = listOf("Débutant", "Intermédiaire", "Avancé", "Courant")

    SelectRow(
        items = levels,
        selected = selected,
        onSelect = onSelect
    )
}

/* ------------------ ÉTAPE 5 : INTÉRÊTS ------------------ */

@Composable
private fun InterestsStep(
    selected: Set<String>,
    onToggle: (String) -> Unit,
    hasSecondHobby: Boolean,
    onSecondHobbyChange: (Boolean) -> Unit
) {
    Text("Centres d'intérêt", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(Modifier.height(6.dp))
    Text("Sélectionnez vos centres d'intérêt", fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(12.dp))

    val interests = listOf(
        "Sport", "Jeux vidéo",
        "Musique", "Design",
        "Lecture", "Voyage",
        "Cuisine", "Photographie"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        interests.chunked(2).forEach { row ->
            SelectRow(
                items = row,
                selectedItems = selected,
                onToggle = onToggle
            )
        }
    }

    Spacer(Modifier.height(20.dp))
    Text("Avez-vous un deuxième hobby ?", fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = hasSecondHobby,
            onClick = { onSecondHobbyChange(true) }
        )
        Text("Oui")
        Spacer(Modifier.width(16.dp))
        RadioButton(
            selected = !hasSecondHobby,
            onClick = { onSecondHobbyChange(false) }
        )
        Text("Non")
    }
}

/* ------------------ COMPOSANTS GÉNÉRIQUES ------------------ */

// Ligne de boutons (1 ou 2 éléments)
@Composable
private fun SelectRow(
    items: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    singleRow: Boolean = false
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (singleRow) Arrangement.Start else Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { label ->
            SelectableChip(
                text = label,
                selected = selected == label,
                onClick = { onSelect(label) },
                modifier = if (singleRow) Modifier.fillMaxWidth() else Modifier.weight(1f)
            )
        }
    }
}

// Version multi-sélection (soft skills / intérêts)
@Composable
private fun SelectRow(
    items: List<String>,
    selectedItems: Set<String>,
    onToggle: (String) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { label ->
            SelectableChip(
                text = label,
                selected = label in selectedItems,
                onClick = { onToggle(label) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(10.dp),
        border = if (selected) null else BorderStroke(1.dp, Border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) Accent else Color.White,
            contentColor = if (selected) Color.White else Color.Black
        )
    ) {
        Text(text, fontSize = 14.sp)
    }
}

@Composable
private fun BottomNavButtons(
    stepIndex: Int,
    total: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPrevious,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Accent
            ),
            border = BorderStroke(1.dp, Accent)
        ) {
            Text(if (stepIndex == 0) "Précédent" else "Précédent")
        }

        Spacer(Modifier.width(12.dp))

        val isLast = stepIndex == total - 1
        Button(
            onClick = onNext,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                contentColor = Color.White
            )
        ) {
            Text(if (isLast) "Terminer ✓" else "Suivant")
        }
    }
}
