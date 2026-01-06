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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sim2.app.talleb_5edma.models.*
import sim2.app.talleb_5edma.network.StudentPreferenceRepository
import sim2.app.talleb_5edma.util.getToken

private val Accent = Color(0xFFB71C1C)
private val CardBg = Color(0xFFF7F7F9)
private val Border = Color(0xFFE0E0E4)

@Composable
fun PreferenceWizardScreen(
    onClose: () -> Unit = {},
    onFinished: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { StudentPreferenceRepository() }
    val coroutineScope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    var currentStep by remember { mutableStateOf(0) } // 0..4

    // État des choix
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
    
    // Fonction pour mapper les valeurs UI vers les enums
    fun mapStudyLevel(level: String?): StudyLevel? {
        return when (level?.lowercase()) {
            "licence 1" -> StudyLevel.LICENCE_1
            "licence 2" -> StudyLevel.LICENCE_2
            "licence 3" -> StudyLevel.LICENCE_3
            "ingénierie" -> StudyLevel.INGENIERIE
            "mastère" -> StudyLevel.MASTERE
            "autre" -> StudyLevel.AUTRE
            else -> null
        }
    }
    
    fun mapStudyDomain(domain: String?): StudyDomain? {
        return when (domain?.lowercase()) {
            "informatique" -> StudyDomain.INFORMATIQUE
            "infirmier" -> StudyDomain.INFIRMIER
            "médecine" -> StudyDomain.MEDECINE
            "mécanique" -> StudyDomain.MECANIQUE
            "électrique" -> StudyDomain.ELECTRIQUE
            "autre" -> StudyDomain.AUTRE
            else -> null
        }
    }
    
    fun mapLookingFor(looking: String?): LookingFor? {
        return when (looking?.lowercase()) {
            "job" -> LookingFor.JOB
            "stage" -> LookingFor.STAGE
            "mission freelance", "freelance" -> LookingFor.FREELANCE
            else -> null
        }
    }
    
    fun mapMotivation(motivation: String?): MainMotivation? {
        return when (motivation?.lowercase()) {
            "argent" -> MainMotivation.ARGENT
            "expérience", "experience" -> MainMotivation.EXPERIENCE
            "enrichissement du cv", "enrichissement_cv" -> MainMotivation.ENRICHISSEMENT_CV
            else -> null
        }
    }
    
    fun mapSoftSkill(skill: String): SoftSkills? {
        return when (skill.lowercase()) {
            "communication" -> SoftSkills.COMMUNICATION
            "organisation" -> SoftSkills.ORGANISATION
            "sérieux" -> SoftSkills.SERIEUX
            "adaptabilité" -> SoftSkills.ADAPTABILITE
            "travail d'équipe", "travail_équipe" -> SoftSkills.TRAVAIL_EQUIPE
            "leadership" -> SoftSkills.LEADERSHIP
            "créativité" -> SoftSkills.CREATIVITE
            "résolution de problèmes", "résolution_problèmes" -> SoftSkills.RESOLUTION_PROBLEMES
            "autre" -> SoftSkills.AUTRE
            else -> null
        }
    }
    
    fun mapLanguageLevel(level: String?): LanguageLevel? {
        return when (level?.lowercase()) {
            "débutant" -> LanguageLevel.DEBUTANT
            "intermédiaire" -> LanguageLevel.INTERMEDIAIRE
            "avancé" -> LanguageLevel.AVANCE
            "courant" -> LanguageLevel.COURANT
            else -> null
        }
    }
    
    fun mapHobby(hobby: String): Hobbies? {
        return when (hobby.lowercase()) {
            "sport" -> Hobbies.SPORT
            "jeux vidéo", "jeux_video" -> Hobbies.JEUX_VIDEO
            "musique" -> Hobbies.MUSIQUE
            "design" -> Hobbies.DESIGN
            "lecture" -> Hobbies.LECTURE
            "voyage" -> Hobbies.VOYAGE
            "cuisine" -> Hobbies.CUISINE
            "photographie" -> Hobbies.PHOTOGRAPHIE
            "autre" -> Hobbies.AUTRE
            else -> null
        }
    }
    
    // Fonction pour sauvegarder toutes les préférences
    fun saveAllPreferences() {
        coroutineScope.launch {
            try {
                isLoading = true
                errorMessage = null
                successMessage = null
                
                val token = getToken(context)
                if (token.isEmpty()) {
                    errorMessage = "Erreur: Vous devez être connecté"
                    isLoading = false
                    return@launch
                }
                
                // Validation des champs requis
                val mappedStudyLevel = mapStudyLevel(studyLevel)
                val mappedStudyDomain = mapStudyDomain(studyField)
                val mappedLookingFor = mapLookingFor(searchType)
                val mappedMotivation = mapMotivation(mainMotivation)
                val mappedArabic = mapLanguageLevel(arabicLevel)
                val mappedFrench = mapLanguageLevel(frenchLevel)
                val mappedEnglish = mapLanguageLevel(englishLevel)
                
                if (mappedStudyLevel == null || mappedStudyDomain == null || 
                    mappedLookingFor == null || mappedMotivation == null ||
                    mappedArabic == null || mappedFrench == null || mappedEnglish == null) {
                    errorMessage = "Veuillez remplir tous les champs obligatoires"
                    isLoading = false
                    return@launch
                }
                
                if (selectedSoftSkills.size < 1 || selectedSoftSkills.size > maxSoftSkills) {
                    errorMessage = "Veuillez sélectionner entre 1 et $maxSoftSkills compétences"
                    isLoading = false
                    return@launch
                }
                
                if (selectedInterests.isEmpty()) {
                    errorMessage = "Veuillez sélectionner au moins un centre d'intérêt"
                    isLoading = false
                    return@launch
                }
                
                // Mapper les soft skills
                val mappedSoftSkills = selectedSoftSkills.mapNotNull { mapSoftSkill(it) }
                if (mappedSoftSkills.size != selectedSoftSkills.size) {
                    errorMessage = "Erreur lors du mapping des compétences"
                    isLoading = false
                    return@launch
                }
                
                // Mapper les hobbies
                val mappedHobbies = selectedInterests.mapNotNull { mapHobby(it) }
                if (mappedHobbies.isEmpty()) {
                    errorMessage = "Erreur lors du mapping des centres d'intérêt"
                    isLoading = false
                    return@launch
                }
                
                // Créer la requête
                val createRequest = CreateStudentPreferenceRequest(
                    study_level = mappedStudyLevel,
                    study_domain = mappedStudyDomain,
                    looking_for = mappedLookingFor,
                    main_motivation = mappedMotivation,
                    soft_skills = mappedSoftSkills,
                    langue_arabe = mappedArabic,
                    langue_francais = mappedFrench,
                    langue_anglais = mappedEnglish,
                    hobbies = mappedHobbies,
                    has_second_hobby = hasSecondHobby,
                    current_step = 5,
                    is_completed = true
                )
                
                // Sauvegarder
                val result = withContext(Dispatchers.IO) {
                    repository.createOrCompletePreference(token, createRequest)
                }
                
                withContext(Dispatchers.Main) {
                    if (result.id != null) {
                        successMessage = "Préférences enregistrées avec succès!"
                        // Fermer après 2 secondes
                        kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
                            kotlinx.coroutines.delay(2000)
                            onFinished()
                        }
                    } else {
                        errorMessage = "Erreur lors de l'enregistrement"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Erreur: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

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
            // Messages d'erreur/succès
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFFC62828),
                        fontSize = 13.sp
                    )
                }
            }
            
            successMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFF2E7D32),
                        fontSize = 13.sp
                    )
                }
            }
            
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
                isLoading = isLoading,
                onPrevious = {
                    if (currentStep == 0) onClose() else currentStep--
                },
                onNext = {
                    if (currentStep == stepsCount - 1) {
                        // Sauvegarder toutes les préférences
                        saveAllPreferences()
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
    isLoading: Boolean = false,
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
            ),
            enabled = !isLoading
        ) {
            if (isLast && isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
            } else {
                Text(if (isLast) "Terminer ✓" else "Suivant")
            }
        }
    }
}
