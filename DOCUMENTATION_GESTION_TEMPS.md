# 📚 Documentation d'Intégration - Module Gestion du Temps

## Vue d'ensemble

Ce document décrit comment intégrer le module de **gestion du temps** dans votre projet Android existant. Ce module ajoute les fonctionnalités suivantes :

- ✅ Gestion des événements (travail, études, activités)
- ✅ Gestion des disponibilités horaires
- ✅ Analyse de routine avec IA
- ✅ Import d'emploi du temps depuis PDF
- ✅ Matching IA pour trouver des opportunités
- ✅ Calendrier interactif
- ✅ Mode examens

---

## 📋 Prérequis

- Android Studio (version récente)
- Projet Android avec Jetpack Compose
- Backend API fonctionnel avec les endpoints nécessaires
- Token d'authentification JWT

---

## 🗂️ Structure des fichiers à ajouter

### 1. Modèles (`models/`)

Ajoutez ces fichiers dans `app/src/main/java/sim2/app/talleb_5edma/models/` :

- ✅ `Evenement.kt` - Modèle pour les événements
- ✅ `Disponibilite.kt` - Modèle pour les disponibilités
- ✅ `RoutineAnalysis.kt` - Modèles pour l'analyse de routine
- ✅ `Schedule.kt` - Modèles pour l'import de planning
- ✅ `Matches.kt` - Modèles pour le matching IA

### 2. Repositories (`network/`)

Ajoutez ces fichiers dans `app/src/main/java/sim2/app/talleb_5edma/network/` :

- ✅ `EvenementRepository.kt` - CRUD événements
- ✅ `DisponibiliteRepository.kt` - CRUD disponibilités
- ✅ `RoutineRepository.kt` - Analyse de routine avec IA
- ✅ `ScheduleRepository.kt` - Import de planning PDF
- ✅ `MatchingRepository.kt` - Matching IA

### 3. Écrans (`screens/`)

Ajoutez ces fichiers dans `app/src/main/java/sim2/app/talleb_5edma/screens/` :

- ✅ `EvenementsScreen.kt` - Liste des événements
- ✅ `EvenementFormScreen.kt` - Formulaire création/édition événement
- ✅ `DisponibilitesScreen.kt` - Liste des disponibilités
- ✅ `DisponibiliteFormScreen.kt` - Formulaire création/édition disponibilité
- ✅ `RoutineAnalysisScreen.kt` - Écran d'analyse de routine
- ✅ `RoutineAnalysisComponents.kt` - Composants UI pour l'analyse
- ✅ `ScheduleUploadScreen.kt` - Import de planning PDF
- ✅ `MatchingScreen.kt` - Écran de matching IA

### 4. Interfaces (`interfaces/`)

Ajoutez ces fichiers dans `app/src/main/java/sim2/app/talleb_5edma/interfaces/` :

- ✅ `TimeScreen.kt` - Écran principal de gestion du temps
- ✅ `CalendarScreen.kt` - Calendrier interactif
- ✅ `AvailabilityScreen.kt` - Gestion des disponibilités
- ✅ `ExamModeScreen.kt` - Mode examens

### 5. Utilitaires (`util/`)

Vérifiez que ces fichiers existent dans `app/src/main/java/sim2/app/talleb_5edma/util/` :

- ✅ `LocalStorage.kt` - Gestion du cache (déjà existant, à mettre à jour)
- ✅ `DateConverter.kt` - Conversion de dates
- ✅ `FileUtils.kt` - Utilitaires pour fichiers

---

## 🔧 Modifications dans les fichiers existants

### 1. `Routes.kt`

Ajoutez ces routes dans votre fichier `Routes.kt` :

```kotlin
object Routes {
    // ... routes existantes ...
    
    // Evenements
    const val ScreenEvenements = "evenements"
    const val ScreenEvenementCreate = "evenements/create"
    const val ScreenEvenementEdit = "evenements/edit"
    
    // Disponibilites
    const val ScreenDisponibilites = "disponibilites"
    const val ScreenDisponibiliteCreate = "disponibilites/create"
    const val ScreenDisponibiliteEdit = "disponibilites/edit"
    
    // Routine Analysis
    const val ScreenRoutineAnalysis = "routine/analysis"
    
    // Schedule Import
    const val ScreenScheduleImport = "schedule-import"
    
    // AI Matching
    const val ScreenAiMatching = "ai-matching"
}
```

### 2. `MainActivity.kt`

#### A. Ajoutez les imports nécessaires :

```kotlin
import sim2.app.talleb_5edma.screens.*
import sim2.app.talleb_5edma.interfaces.*
```

#### B. Ajoutez les routes dans le NavHost :

```kotlin
NavHost(
    navController = navController,
    startDestination = startingRoute,
    modifier = modifier
) {
    // ... routes existantes ...
    
    // Evenements
    composable(Routes.ScreenEvenements) {
        EvenementsScreen(navController, currentToken)
    }
    composable(Routes.ScreenEvenementCreate) {
        EvenementFormScreen(navController, token = currentToken)
    }
    composable(Routes.ScreenEvenementEdit + "/{id}") {
        val id = it.arguments?.getString("id")
        EvenementFormScreen(navController, eventId = id, token = currentToken)
    }
    
    // Disponibilites
    composable(Routes.ScreenDisponibilites) {
        DisponibilitesScreen(navController, currentToken)
    }
    composable(Routes.ScreenDisponibiliteCreate + "/{jour}") { backStackEntry ->
        val jour = backStackEntry.arguments?.getString("jour")
        DisponibiliteFormScreen(navController, token = currentToken, jourParam = jour)
    }
    composable(Routes.ScreenDisponibiliteCreate) {
        DisponibiliteFormScreen(navController, token = currentToken)
    }
    composable(Routes.ScreenDisponibiliteEdit + "/{id}") {
        val id = it.arguments?.getString("id")
        DisponibiliteFormScreen(navController, disponibiliteId = id, token = currentToken)
    }
    
    // Routine Analysis
    composable(Routes.ScreenRoutineAnalysis) {
        RoutineAnalysisScreen(navController, currentToken)
    }
    
    // Schedule Import
    composable(Routes.ScreenScheduleImport) {
        ScheduleUploadScreen(navController, currentToken)
    }
    
    // AI Matching
    composable(Routes.ScreenAiMatching) {
        MatchingScreen(navController, currentToken)
    }
    
    // Calendar
    composable("calendar") {
        CalendarScreen(
            onBack = { navController.popBackStack() },
            onManageAvailability = {
                navController.navigate("availability")
            },
            navController = navController
        )
    }
    
    // Availability
    composable("availability") {
        AvailabilityScreen(
            onBack = { navController.popBackStack() },
            onOpenExamMode = { navController.navigate("exam_mode") },
            navController = navController
        )
    }
    
    // Exam Mode
    composable("exam_mode") {
        ExamModeScreen()
    }
}
```

#### C. Modifiez l'écran "Temps" pour utiliser TimeScreen :

```kotlin
composable(BottomDest.Time.route) {
    TimeScreen(
        userName = currentUser?.nom ?: "User",
        onOpenCalendar = { navController.navigate("calendar") },
        onOpenAvailability = { navController.navigate("availability") },
        onOpenRoutineAnalysis = { navController.navigate(Routes.ScreenRoutineAnalysis) },
        onOpenScheduleUpload = { navController.navigate(Routes.ScreenScheduleImport) },
        onOpenAiMatching = { navController.navigate(Routes.ScreenAiMatching) }
    )
}
```

### 3. `LocalStorage.kt`

Ajoutez ces fonctions pour le cache de l'analyse de routine :

```kotlin
// ==================== ROUTINE ANALYSIS CACHE ====================

const val ROUTINE_CACHE_PREFS = "routine_cache"
const val ROUTINE_CACHE_KEY = "routine_analysis_data"
const val ROUTINE_CACHE_TIMESTAMP_KEY = "routine_analysis_timestamp"
const val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes

fun getRoutineCachePrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(ROUTINE_CACHE_PREFS, Context.MODE_PRIVATE)
}

fun saveRoutineAnalysisCache(context: Context, data: String) {
    getRoutineCachePrefs(context).edit {
        putString(ROUTINE_CACHE_KEY, data)
        putLong(ROUTINE_CACHE_TIMESTAMP_KEY, System.currentTimeMillis())
        apply()
    }
    println("CatLog: Routine analysis cache saved")
}

fun getRoutineAnalysisCache(context: Context): String? {
    val prefs = getRoutineCachePrefs(context)
    val timestamp = prefs.getLong(ROUTINE_CACHE_TIMESTAMP_KEY, 0)
    val now = System.currentTimeMillis()
    
    if (now - timestamp > CACHE_DURATION_MS) {
        println("CatLog: Routine analysis cache expired")
        clearRoutineAnalysisCache(context)
        return null
    }
    
    val cached = prefs.getString(ROUTINE_CACHE_KEY, null)
    if (cached != null) {
        println("CatLog: Routine analysis cache found (age: ${(now - timestamp) / 1000}s)")
    }
    return cached
}

fun clearRoutineAnalysisCache(context: Context) {
    getRoutineCachePrefs(context).edit {
        clear()
        apply()
    }
    println("CatLog: Routine analysis cache cleared")
}

fun isRoutineCacheValid(context: Context): Boolean {
    val prefs = getRoutineCachePrefs(context)
    val timestamp = prefs.getLong(ROUTINE_CACHE_TIMESTAMP_KEY, 0)
    val now = System.currentTimeMillis()
    return (now - timestamp) <= CACHE_DURATION_MS && prefs.getString(ROUTINE_CACHE_KEY, null) != null
}
```

### 4. `KtorConfig.kt`

Vérifiez que votre configuration Ktor inclut les timeouts suivants :

```kotlin
install(HttpTimeout) {
    connectTimeoutMillis = 30_000
    requestTimeoutMillis = 300_000  // 5 minutes pour traitement PDF
    socketTimeoutMillis = 300_000
}
```

---

## 📦 Dépendances à ajouter

Vérifiez que votre fichier `app/build.gradle.kts` contient ces dépendances :

```kotlin
dependencies {
    // ... dépendances existantes ...
    
    // Ktor (déjà présent normalement)
    implementation("io.ktor:ktor-client-core:3.3.1")
    implementation("io.ktor:ktor-client-cio:3.3.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.3.1")
    implementation("io.ktor:ktor-serialization-gson:3.3.1")
    implementation("io.ktor:ktor-client-logging:3.3.1")
    
    // Gson pour la sérialisation JSON
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Pour la sélection de fichiers PDF
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Pour les permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
}
```

---

## 🚀 Commandes d'installation

### 1. Synchroniser Gradle

```bash
./gradlew clean
./gradlew build
```

Ou dans Android Studio :
- **File → Sync Project with Gradle Files**

### 2. Vérifier les imports

Assurez-vous que tous les fichiers sont bien importés et qu'il n'y a pas d'erreurs de compilation.

### 3. Tester la compilation

```bash
./gradlew assembleDebug
```

---

## ⚙️ Configuration Backend

Assurez-vous que votre backend expose ces endpoints :

### Événements
- `POST /evenements` - Créer un événement
- `GET /evenements` - Liste des événements
- `GET /evenements/{id}` - Détails d'un événement
- `PATCH /evenements/{id}` - Modifier un événement
- `DELETE /evenements/{id}` - Supprimer un événement
- `GET /evenements/date-range?startDate=...&endDate=...` - Événements par période

### Disponibilités
- `POST /disponibilites` - Créer une disponibilité
- `GET /disponibilites` - Liste des disponibilités
- `GET /disponibilites/{id}` - Détails d'une disponibilité
- `PATCH /disponibilites/{id}` - Modifier une disponibilité
- `DELETE /disponibilites/{id}` - Supprimer une disponibilité
- `GET /disponibilites/jour/{jour}` - Disponibilités par jour

### Analyse de routine
- `POST /ai/routine/analyze-enhanced` - Analyse enhanced (recommandé)
- `POST /ai/routine/analyze` - Analyse legacy (compatibilité)

### Import de planning
- `POST /schedule/process` - Traiter un PDF
- `POST /schedule/create-events` - Créer des événements depuis les cours

### Matching IA
- `POST /ai-matching/analyze` - Analyser les matches

---

## 📝 Checklist d'intégration

### Fichiers à copier
- [ ] Tous les fichiers `models/` listés ci-dessus
- [ ] Tous les fichiers `network/` listés ci-dessus
- [ ] Tous les fichiers `screens/` listés ci-dessus
- [ ] Tous les fichiers `interfaces/` listés ci-dessus
- [ ] Vérifier les fichiers `util/`

### Modifications à faire
- [ ] Ajouter les routes dans `Routes.kt`
- [ ] Ajouter les routes dans `MainActivity.kt`
- [ ] Modifier l'écran "Temps" dans `MainActivity.kt`
- [ ] Ajouter les fonctions de cache dans `LocalStorage.kt`
- [ ] Vérifier la configuration Ktor

### Configuration
- [ ] Vérifier les dépendances dans `build.gradle.kts`
- [ ] Synchroniser Gradle
- [ ] Vérifier la base URL dans `KtorConfig.kt`
- [ ] Tester la compilation

### Tests
- [ ] Tester la création d'un événement
- [ ] Tester la création d'une disponibilité
- [ ] Tester l'analyse de routine
- [ ] Tester l'import de planning PDF
- [ ] Tester le matching IA

---

## 🔍 Dépannage

### Erreur : "Unresolved reference"
- Vérifiez que tous les imports sont corrects
- Synchronisez Gradle : **File → Sync Project with Gradle Files**

### Erreur : "Cannot find symbol"
- Vérifiez que tous les fichiers sont bien copiés
- Vérifiez les noms de packages

### Erreur réseau : "Connection timeout"
- Vérifiez la base URL dans `KtorConfig.kt`
- Vérifiez que le backend est accessible
- Vérifiez les timeouts dans `KtorConfig.kt`

### Erreur : "Token not found"
- Vérifiez que l'utilisateur est bien connecté
- Vérifiez la fonction `getToken()` dans `LocalStorage.kt`

### Erreur lors de l'import PDF
- Vérifiez les permissions dans `AndroidManifest.xml`
- Vérifiez que le backend supporte le traitement PDF

---

## 📞 Support

Si vous rencontrez des problèmes :

1. Vérifiez les logs avec le tag `CatLog`
2. Vérifiez que tous les endpoints backend sont disponibles
3. Vérifiez la configuration de la base URL
4. Vérifiez les permissions dans `AndroidManifest.xml`

---

## 📄 Fichiers à copier (liste complète)

### Modèles
```
models/Evenement.kt
models/Disponibilite.kt
models/RoutineAnalysis.kt
models/Schedule.kt
models/Matches.kt
```

### Repositories
```
network/EvenementRepository.kt
network/DisponibiliteRepository.kt
network/RoutineRepository.kt
network/ScheduleRepository.kt
network/MatchingRepository.kt
```

### Écrans
```
screens/EvenementsScreen.kt
screens/EvenementFormScreen.kt
screens/DisponibilitesScreen.kt
screens/DisponibiliteFormScreen.kt
screens/RoutineAnalysisScreen.kt
screens/RoutineAnalysisComponents.kt
screens/ScheduleUploadScreen.kt
screens/MatchingScreen.kt
```

### Interfaces
```
interfaces/TimeScreen.kt
interfaces/CalendarScreen.kt
interfaces/AvailabilityScreen.kt
interfaces/ExamModeScreen.kt
```

### Utilitaires
```
util/DateConverter.kt
util/FileUtils.kt
```

---

## ✅ Validation finale

Une fois l'intégration terminée, vous devriez pouvoir :

1. ✅ Accéder à l'écran "Temps" depuis la bottom navigation
2. ✅ Voir le calendrier
3. ✅ Créer/modifier/supprimer des événements
4. ✅ Créer/modifier/supprimer des disponibilités
5. ✅ Analyser votre routine hebdomadaire
6. ✅ Importer un emploi du temps PDF
7. ✅ Utiliser le matching IA

---

**Bon développement ! 🚀**

