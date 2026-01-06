# 🔧 Modifications Exactes à Faire

Ce document contient les modifications **exactes** à faire dans les fichiers existants.

---

## 1. Routes.kt

### Fichier : `app/src/main/java/sim2/app/talleb_5edma/Routes.kt`

### Modification : Ajouter ces constantes dans l'object Routes

```kotlin
object Routes {
    // ... routes existantes ...
    
    // ========== AJOUTER CES ROUTES ==========
    
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

---

## 2. MainActivity.kt

### Fichier : `app/src/main/java/sim2/app/talleb_5edma/MainActivity.kt`

### Modification A : Ajouter les imports

**À ajouter en haut du fichier avec les autres imports :**

```kotlin
import sim2.app.talleb_5edma.screens.*
import sim2.app.talleb_5edma.interfaces.*
```

### Modification B : Modifier l'écran "Temps"

**Trouver cette section :**
```kotlin
composable(BottomDest.Time.route) {
    // ... code existant ...
}
```

**Remplacer par :**
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

### Modification C : Ajouter les routes dans NavHost

**Dans le NavHost, après les routes existantes, ajouter :**

```kotlin
NavHost(
    navController = navController,
    startDestination = startingRoute,
    modifier = modifier
) {
    // ... routes existantes ...
    
    // ========== AJOUTER CES ROUTES ==========
    
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
    
    // Calendar (si pas déjà présent)
    composable("calendar") {
        CalendarScreen(
            onBack = { navController.popBackStack() },
            onManageAvailability = {
                navController.navigate("availability")
            },
            navController = navController
        )
    }
    
    // Availability (si pas déjà présent)
    composable("availability") {
        AvailabilityScreen(
            onBack = { navController.popBackStack() },
            onOpenExamMode = { navController.navigate("exam_mode") },
            navController = navController
        )
    }
    
    // Exam Mode (si pas déjà présent)
    composable("exam_mode") {
        ExamModeScreen()
    }
}
```

---

## 3. LocalStorage.kt

### Fichier : `app/src/main/java/sim2/app/talleb_5edma/util/LocalStorage.kt`

### Modification : Ajouter les fonctions de cache à la fin du fichier

**Ajouter ce code à la fin du fichier (après toutes les fonctions existantes) :**

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

---

## 4. KtorConfig.kt

### Fichier : `app/src/main/java/sim2/app/talleb_5edma/util/KtorConfig.kt`

### Modification : Vérifier les timeouts

**Vérifier que cette section existe dans la configuration du client :**

```kotlin
val client = HttpClient(CIO) {
    install(HttpTimeout) {
        connectTimeoutMillis = 30_000
        requestTimeoutMillis = 300_000  // 5 minutes pour traitement PDF
        socketTimeoutMillis = 300_000
    }
    // ... reste de la configuration ...
}
```

**Si cette section n'existe pas, l'ajouter dans la configuration du client.**

---

## 5. AndroidManifest.xml

### Fichier : `app/src/main/AndroidManifest.xml`

### Modification : Vérifier les permissions

**Vérifier que ces permissions sont présentes :**

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.CAMERA"/>
```

**Pour Android 13+ (API 33+), ajouter aussi :**
```xml
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>
```

---

## 6. build.gradle.kts

### Fichier : `app/build.gradle.kts`

### Modification : Vérifier les dépendances

**Vérifier que ces dépendances sont présentes dans la section `dependencies` :**

```kotlin
dependencies {
    // ... dépendances existantes ...
    
    // Ktor (normalement déjà présent)
    implementation("io.ktor:ktor-client-core:3.3.1")
    implementation("io.ktor:ktor-client-cio:3.3.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.3.1")
    implementation("io.ktor:ktor-serialization-gson:3.3.1")
    implementation("io.ktor:ktor-client-logging:3.3.1")
    
    // Gson (pour la sérialisation JSON)
    implementation("com.google.code.gson:gson:2.10.1")
}
```

**Si ces dépendances ne sont pas présentes, les ajouter.**

---

## 📝 Checklist des modifications

- [ ] **Routes.kt** : Routes ajoutées
- [ ] **MainActivity.kt** : Imports ajoutés
- [ ] **MainActivity.kt** : Écran "Temps" modifié
- [ ] **MainActivity.kt** : Routes ajoutées dans NavHost
- [ ] **LocalStorage.kt** : Fonctions de cache ajoutées
- [ ] **KtorConfig.kt** : Timeouts vérifiés
- [ ] **AndroidManifest.xml** : Permissions vérifiées
- [ ] **build.gradle.kts** : Dépendances vérifiées

---

## ⚠️ Notes importantes

1. **Ordre des modifications** :
   - Commencez par `Routes.kt`
   - Puis `MainActivity.kt`
   - Ensuite `LocalStorage.kt`
   - Enfin vérifiez `KtorConfig.kt` et `build.gradle.kts`

2. **Erreurs de compilation** :
   - Si vous avez des erreurs après avoir modifié `MainActivity.kt`, c'est normal si les fichiers screens/interfaces ne sont pas encore copiés
   - Copiez d'abord tous les fichiers, puis modifiez `MainActivity.kt`

3. **Synchronisation Gradle** :
   - Après chaque modification de `build.gradle.kts`, synchronisez Gradle
   - `File → Sync Project with Gradle Files`

---

## 🔍 Vérification après modifications

### 1. Compiler le projet
```bash
./gradlew build
```

### 2. Vérifier les erreurs
Dans Android Studio : `Build → Make Project`

### 3. Vérifier les imports
Tous les imports doivent être résolus (pas d'erreurs rouges)

---

## ✅ Validation finale

Une fois toutes les modifications faites :

1. ✅ Le projet compile sans erreur
2. ✅ Tous les imports sont résolus
3. ✅ Les routes sont accessibles depuis l'application
4. ✅ L'écran "Temps" affiche les nouveaux boutons

---

**Bon courage ! 🚀**

