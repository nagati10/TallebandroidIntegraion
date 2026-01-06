# 📖 Guide d'Intégration - Module Gestion du Temps

Bienvenue ! Ce guide vous aidera à intégrer le module de **gestion du temps** dans votre projet Android existant.

---

## 🎯 Vue d'ensemble

Ce module ajoute les fonctionnalités suivantes à votre application :

- 📅 **Gestion des événements** : Créer, modifier, supprimer des événements (travail, études, activités)
- ⏰ **Gestion des disponibilités** : Définir vos créneaux disponibles par jour
- 📊 **Analyse de routine** : Analyse IA de votre planning hebdomadaire avec score d'équilibre
- 📄 **Import de planning** : Importer automatiquement votre emploi du temps depuis un PDF
- 🤖 **Matching IA** : Trouver des opportunités qui correspondent à vos disponibilités
- 📆 **Calendrier interactif** : Visualiser votre planning de manière intuitive

---

## 📚 Documentation disponible

Ce projet contient plusieurs documents pour vous guider :

### 1. 📋 **LISTE_FICHIERS_A_COPIER.md**
   - Liste complète de tous les fichiers à copier
   - Chemins exacts de chaque fichier
   - Checklist de copie

### 2. 📖 **DOCUMENTATION_GESTION_TEMPS.md**
   - Documentation complète et détaillée
   - Toutes les modifications à faire
   - Configuration backend
   - Dépannage

### 3. ⚡ **GUIDE_RAPIDE.md**
   - Guide de démarrage rapide (5 minutes)
   - Étapes essentielles
   - Checklist rapide

### 4. 💻 **COMMANDES.md**
   - Toutes les commandes Gradle nécessaires
   - Commandes de test et débogage
   - Ordre d'exécution recommandé

---

## 🚀 Démarrage rapide

### Option 1 : Démarrage rapide (5 minutes)
👉 Consultez **GUIDE_RAPIDE.md**

### Option 2 : Guide complet
👉 Consultez **DOCUMENTATION_GESTION_TEMPS.md**

---

## 📝 Étapes principales

### 1. Copier les fichiers
   - Consultez **LISTE_FICHIERS_A_COPIER.md**
   - Copiez les 22 fichiers nécessaires
   - Vérifiez les 2 fichiers utilitaires

### 2. Modifier les fichiers existants
   - `Routes.kt` : Ajouter les nouvelles routes
   - `MainActivity.kt` : Ajouter les routes dans NavHost
   - `LocalStorage.kt` : Ajouter les fonctions de cache
   - `KtorConfig.kt` : Vérifier les timeouts

### 3. Synchroniser Gradle
   ```bash
   ./gradlew clean build
   ```

### 4. Vérifier la configuration
   - Base URL dans `KtorConfig.kt`
   - Endpoints backend disponibles
   - Permissions dans `AndroidManifest.xml`

### 5. Tester
   - Compiler le projet
   - Tester chaque fonctionnalité
   - Vérifier les logs

---

## 📦 Structure du module

```
app/src/main/java/sim2/app/talleb_5edma/
├── models/
│   ├── Evenement.kt
│   ├── Disponibilite.kt
│   ├── RoutineAnalysis.kt
│   ├── Schedule.kt
│   └── Matches.kt
├── network/
│   ├── EvenementRepository.kt
│   ├── DisponibiliteRepository.kt
│   ├── RoutineRepository.kt
│   ├── ScheduleRepository.kt
│   └── MatchingRepository.kt
├── screens/
│   ├── EvenementsScreen.kt
│   ├── EvenementFormScreen.kt
│   ├── DisponibilitesScreen.kt
│   ├── DisponibiliteFormScreen.kt
│   ├── RoutineAnalysisScreen.kt
│   ├── RoutineAnalysisComponents.kt
│   ├── ScheduleUploadScreen.kt
│   └── MatchingScreen.kt
├── interfaces/
│   ├── TimeScreen.kt
│   ├── CalendarScreen.kt
│   ├── AvailabilityScreen.kt
│   └── ExamModeScreen.kt
└── util/
    ├── DateConverter.kt
    └── FileUtils.kt
```

---

## ⚙️ Configuration requise

### Backend
Votre backend doit exposer ces endpoints :

- `POST /evenements` - Créer un événement
- `GET /evenements` - Liste des événements
- `PATCH /evenements/{id}` - Modifier un événement
- `DELETE /evenements/{id}` - Supprimer un événement
- `POST /disponibilites` - Créer une disponibilité
- `GET /disponibilites` - Liste des disponibilités
- `PATCH /disponibilites/{id}` - Modifier une disponibilité
- `DELETE /disponibilites/{id}` - Supprimer une disponibilité
- `POST /ai/routine/analyze-enhanced` - Analyse de routine
- `POST /schedule/process` - Traiter un PDF
- `POST /schedule/create-events` - Créer des événements depuis les cours
- `POST /ai-matching/analyze` - Matching IA

### Android
- Android Studio (version récente)
- Min SDK : 24
- Target SDK : 36
- Jetpack Compose
- Ktor pour les appels réseau

---

## ✅ Checklist d'intégration

### Fichiers
- [ ] 22 fichiers copiés (voir LISTE_FICHIERS_A_COPIER.md)
- [ ] 2 fichiers utilitaires vérifiés/créés
- [ ] 4 fichiers modifiés (Routes, MainActivity, LocalStorage, KtorConfig)

### Configuration
- [ ] Routes ajoutées dans Routes.kt
- [ ] Routes ajoutées dans MainActivity.kt
- [ ] Cache ajouté dans LocalStorage.kt
- [ ] Timeouts vérifiés dans KtorConfig.kt
- [ ] Base URL configurée

### Gradle
- [ ] Dépendances vérifiées
- [ ] Gradle synchronisé
- [ ] Projet compilé sans erreur

### Tests
- [ ] Création d'événement fonctionne
- [ ] Création de disponibilité fonctionne
- [ ] Analyse de routine fonctionne
- [ ] Import PDF fonctionne
- [ ] Matching IA fonctionne

---

## 🐛 Dépannage

### Erreur de compilation
1. Synchroniser Gradle : `File → Sync Project with Gradle Files`
2. Nettoyer : `./gradlew clean`
3. Reconstruire : `Build → Rebuild Project`

### Erreur réseau
1. Vérifier la base URL dans `KtorConfig.kt`
2. Vérifier que le backend est accessible
3. Vérifier le token d'authentification

### Erreur "Unresolved reference"
1. Vérifier que tous les fichiers sont copiés
2. Vérifier les imports dans chaque fichier
3. Synchroniser Gradle

### Plus d'aide
Consultez la section "Dépannage" dans **DOCUMENTATION_GESTION_TEMPS.md**

---

## 📞 Support

Si vous rencontrez des problèmes :

1. ✅ Vérifiez les logs avec le tag `CatLog`
2. ✅ Consultez la documentation complète
3. ✅ Vérifiez que tous les endpoints backend sont disponibles
4. ✅ Vérifiez la configuration de la base URL

---

## 📄 Fichiers de documentation

- **README_INTEGRATION.md** (ce fichier) - Vue d'ensemble
- **LISTE_FICHIERS_A_COPIER.md** - Liste complète des fichiers
- **DOCUMENTATION_GESTION_TEMPS.md** - Documentation détaillée
- **GUIDE_RAPIDE.md** - Guide de démarrage rapide
- **COMMANDES.md** - Commandes nécessaires

---

## 🎯 Prochaines étapes

1. **Lire** ce README
2. **Consulter** LISTE_FICHIERS_A_COPIER.md pour voir les fichiers à copier
3. **Suivre** GUIDE_RAPIDE.md ou DOCUMENTATION_GESTION_TEMPS.md
4. **Exécuter** les commandes dans COMMANDES.md
5. **Tester** toutes les fonctionnalités

---

## ✨ Fonctionnalités ajoutées

Une fois l'intégration terminée, vous pourrez :

- ✅ Accéder à l'écran "Temps" depuis la bottom navigation
- ✅ Voir et gérer votre calendrier
- ✅ Créer/modifier/supprimer des événements
- ✅ Créer/modifier/supprimer des disponibilités
- ✅ Analyser votre routine hebdomadaire avec IA
- ✅ Importer votre emploi du temps depuis un PDF
- ✅ Trouver des opportunités avec le matching IA

---

**Bon développement ! 🚀**

*Pour toute question, consultez la documentation complète dans DOCUMENTATION_GESTION_TEMPS.md*

