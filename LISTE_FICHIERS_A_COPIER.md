# 📋 Liste Complète des Fichiers à Copier

## Structure des dossiers

Tous les fichiers doivent être copiés dans : `app/src/main/java/sim2/app/talleb_5edma/`

---

## 📁 MODELS (5 fichiers)

### Chemin : `models/`

1. ✅ **Evenement.kt**
   - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/models/Evenement.kt`
   - Contenu : Modèles pour les événements (Evenement, CreateEvenementRequest, UpdateEvenementRequest)

2. ✅ **Disponibilite.kt**
   - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/models/Disponibilite.kt`
   - Contenu : Modèles pour les disponibilités (Disponibilite, CreateDisponibiliteRequest, UpdateDisponibiliteRequest)

3. ✅ **RoutineAnalysis.kt**
   - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/models/RoutineAnalysis.kt`
   - Contenu : Tous les modèles pour l'analyse de routine (enhanced + legacy)

4. ✅ **Schedule.kt**
   - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/models/Schedule.kt`
   - Contenu : Modèles pour l'import de planning PDF (Course, ProcessedScheduleResponse, etc.)

5. ✅ **Matches.kt**
   - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/models/Matches.kt`
   - Contenu : Modèles pour le matching IA (Match, MatchingRequest, MatchingResponse, etc.)

---

## 📁 NETWORK (5 fichiers)

### Chemin : `network/`

6. ✅ **EvenementRepository.kt**
   - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/network/EvenementRepository.kt`
   - Contenu : CRUD complet pour les événements

7. ✅ **DisponibiliteRepository.kt**
   - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/network/DisponibiliteRepository.kt`
   - Contenu : CRUD complet pour les disponibilités

8. ✅ **RoutineRepository.kt**
   - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/network/RoutineRepository.kt`
   - Contenu : Analyse de routine avec IA (enhanced + legacy)

9. ✅ **ScheduleRepository.kt**
   - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/network/ScheduleRepository.kt`
   - Contenu : Import et traitement de planning PDF

10. ✅ **MatchingRepository.kt**
    - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/network/MatchingRepository.kt`
    - Contenu : Matching IA pour trouver des opportunités

---

## 📁 SCREENS (8 fichiers)

### Chemin : `screens/`

11. ✅ **EvenementsScreen.kt**
    - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/screens/EvenementsScreen.kt`
    - Contenu : Écran de liste des événements

12. ✅ **EvenementFormScreen.kt**
    - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/screens/EvenementFormScreen.kt`
    - Contenu : Formulaire de création/édition d'événement

13. ✅ **DisponibilitesScreen.kt**
    - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/screens/DisponibilitesScreen.kt`
    - Contenu : Écran de liste des disponibilités

14. ✅ **DisponibiliteFormScreen.kt**
    - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/screens/DisponibiliteFormScreen.kt`
    - Contenu : Formulaire de création/édition de disponibilité

15. ✅ **RoutineAnalysisScreen.kt**
    - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/screens/RoutineAnalysisScreen.kt`
    - Contenu : Écran principal d'analyse de routine

16. ✅ **RoutineAnalysisComponents.kt**
    - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/screens/RoutineAnalysisComponents.kt`
    - Contenu : Composants UI pour l'affichage de l'analyse (cartes, graphiques, etc.)

17. ✅ **ScheduleUploadScreen.kt**
    - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/screens/ScheduleUploadScreen.kt`
    - Contenu : Écran d'import de planning PDF

18. ✅ **MatchingScreen.kt**
    - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/screens/MatchingScreen.kt`
    - Contenu : Écran de matching IA

---

## 📁 INTERFACES (4 fichiers)

### Chemin : `interfaces/`

19. ✅ **TimeScreen.kt**
    - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/interfaces/TimeScreen.kt`
    - Contenu : Écran principal de gestion du temps (menu avec boutons)

20. ✅ **CalendarScreen.kt**
    - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/interfaces/CalendarScreen.kt`
    - Contenu : Calendrier interactif

21. ✅ **AvailabilityScreen.kt**
    - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/interfaces/AvailabilityScreen.kt`
    - Contenu : Gestion des disponibilités

22. ✅ **ExamModeScreen.kt**
    - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/interfaces/ExamModeScreen.kt`
    - Contenu : Mode examens

---

## 📁 UTIL (2 fichiers à vérifier/ajouter)

### Chemin : `util/`

23. ✅ **DateConverter.kt**
    - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/util/DateConverter.kt`
    - Contenu : Utilitaires de conversion de dates
    - ⚠️ Vérifier si existe déjà, sinon créer

24. ✅ **FileUtils.kt**
    - Chemin complet : `app/src/main/java/sim2/app/talleb_5edma/util/FileUtils.kt`
    - Contenu : Utilitaires pour la gestion de fichiers (Uri, Bitmap, etc.)
    - ⚠️ Vérifier si existe déjà, sinon créer

---

## 📝 FICHIERS À MODIFIER (4 fichiers)

### ⚠️ Ne pas copier, mais modifier ces fichiers existants :

25. 🔧 **Routes.kt**
    - Chemin : `app/src/main/java/sim2/app/talleb_5edma/Routes.kt`
    - Action : Ajouter les nouvelles routes (voir documentation)

26. 🔧 **MainActivity.kt**
    - Chemin : `app/src/main/java/sim2/app/talleb_5edma/MainActivity.kt`
    - Action : Ajouter les routes dans NavHost et modifier l'écran "Temps"

27. 🔧 **LocalStorage.kt**
    - Chemin : `app/src/main/java/sim2/app/talleb_5edma/util/LocalStorage.kt`
    - Action : Ajouter les fonctions de cache pour l'analyse de routine

28. 🔧 **KtorConfig.kt**
    - Chemin : `app/src/main/java/sim2/app/talleb_5edma/util/KtorConfig.kt`
    - Action : Vérifier les timeouts (déjà configurés normalement)

---

## 📊 Résumé

- **Fichiers à copier** : 22 fichiers
  - Models : 5 fichiers
  - Network : 5 fichiers
  - Screens : 8 fichiers
  - Interfaces : 4 fichiers

- **Fichiers à vérifier/créer** : 2 fichiers
  - Util : 2 fichiers

- **Fichiers à modifier** : 4 fichiers
  - Routes.kt
  - MainActivity.kt
  - LocalStorage.kt
  - KtorConfig.kt (vérification)

**Total : 24 fichiers à gérer**

---

## ✅ Checklist de copie

### Models
- [ ] Evenement.kt
- [ ] Disponibilite.kt
- [ ] RoutineAnalysis.kt
- [ ] Schedule.kt
- [ ] Matches.kt

### Network
- [ ] EvenementRepository.kt
- [ ] DisponibiliteRepository.kt
- [ ] RoutineRepository.kt
- [ ] ScheduleRepository.kt
- [ ] MatchingRepository.kt

### Screens
- [ ] EvenementsScreen.kt
- [ ] EvenementFormScreen.kt
- [ ] DisponibilitesScreen.kt
- [ ] DisponibiliteFormScreen.kt
- [ ] RoutineAnalysisScreen.kt
- [ ] RoutineAnalysisComponents.kt
- [ ] ScheduleUploadScreen.kt
- [ ] MatchingScreen.kt

### Interfaces
- [ ] TimeScreen.kt
- [ ] CalendarScreen.kt
- [ ] AvailabilityScreen.kt
- [ ] ExamModeScreen.kt

### Util
- [ ] DateConverter.kt (vérifier/créer)
- [ ] FileUtils.kt (vérifier/créer)

### Modifications
- [ ] Routes.kt (modifier)
- [ ] MainActivity.kt (modifier)
- [ ] LocalStorage.kt (modifier)
- [ ] KtorConfig.kt (vérifier)

---

## 🚀 Ordre recommandé de copie

1. **Models** (5 fichiers) - Base de données
2. **Network** (5 fichiers) - Communication API
3. **Util** (2 fichiers) - Utilitaires
4. **Interfaces** (4 fichiers) - UI de base
5. **Screens** (8 fichiers) - Écrans complets
6. **Modifications** (4 fichiers) - Intégration

---

## 💡 Astuce

Pour copier rapidement tous les fichiers :

1. Créez d'abord les dossiers s'ils n'existent pas :
   ```bash
   mkdir -p app/src/main/java/sim2/app/talleb_5edma/models
   mkdir -p app/src/main/java/sim2/app/talleb_5edma/network
   mkdir -p app/src/main/java/sim2/app/talleb_5edma/screens
   mkdir -p app/src/main/java/sim2/app/talleb_5edma/interfaces
   mkdir -p app/src/main/java/sim2/app/talleb_5edma/util
   ```

2. Copiez ensuite tous les fichiers dans leurs dossiers respectifs.

3. Vérifiez que les packages sont corrects dans chaque fichier.

---

**Bon courage ! 🎯**

