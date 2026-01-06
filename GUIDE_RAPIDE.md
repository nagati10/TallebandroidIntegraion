# 🚀 Guide Rapide - Intégration Gestion du Temps

## ⚡ Démarrage rapide (5 minutes)

### Étape 1 : Copier les fichiers

Copiez tous les fichiers listés dans `DOCUMENTATION_GESTION_TEMPS.md` dans votre projet.

### Étape 2 : Modifier Routes.kt

Ajoutez ces routes :

```kotlin
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
```

### Étape 3 : Modifier MainActivity.kt

#### A. Imports à ajouter :
```kotlin
import sim2.app.talleb_5edma.screens.*
import sim2.app.talleb_5edma.interfaces.*
```

#### B. Modifier l'écran "Temps" :
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

#### C. Ajouter les routes dans NavHost (voir documentation complète)

### Étape 4 : Ajouter le cache dans LocalStorage.kt

Copiez les fonctions de cache depuis `DOCUMENTATION_GESTION_TEMPS.md` section 3.

### Étape 5 : Synchroniser Gradle

```bash
./gradlew clean build
```

Ou dans Android Studio : **File → Sync Project with Gradle Files**

### Étape 6 : Vérifier la base URL

Dans `util/KtorConfig.kt`, vérifiez :
```kotlin
const val BASE_URL = "http://10.0.2.2:3005"  // Émulateur
// ou
const val BASE_URL = "https://votre-backend.com"  // Production
```

---

## ✅ Checklist rapide

- [ ] Fichiers copiés
- [ ] Routes ajoutées
- [ ] MainActivity modifié
- [ ] Cache ajouté dans LocalStorage
- [ ] Gradle synchronisé
- [ ] Base URL vérifiée
- [ ] Compilation réussie

---

## 🐛 Problèmes courants

| Problème | Solution |
|----------|----------|
| Erreur de compilation | Synchroniser Gradle |
| "Unresolved reference" | Vérifier les imports |
| Timeout réseau | Vérifier BASE_URL et backend |
| Token invalide | Vérifier la connexion utilisateur |

---

## 📞 Besoin d'aide ?

Consultez `DOCUMENTATION_GESTION_TEMPS.md` pour les détails complets.

