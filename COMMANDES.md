# 💻 Commandes Nécessaires

## Commandes Gradle

### 1. Nettoyer le projet
```bash
./gradlew clean
```

### 2. Compiler le projet
```bash
./gradlew build
```

### 3. Compiler en mode debug
```bash
./gradlew assembleDebug
```

### 4. Compiler en mode release
```bash
./gradlew assembleRelease
```

### 5. Installer sur l'appareil/émulateur
```bash
./gradlew installDebug
```

### 6. Synchroniser les dépendances
```bash
./gradlew --refresh-dependencies
```

### 7. Vérifier les dépendances
```bash
./gradlew dependencies
```

---

## Commandes Git (optionnel)

### Si vous utilisez Git pour partager le code :

```bash
# Créer une branche pour la gestion du temps
git checkout -b feature/gestion-temps

# Ajouter tous les nouveaux fichiers
git add app/src/main/java/sim2/app/talleb_5edma/models/
git add app/src/main/java/sim2/app/talleb_5edma/network/
git add app/src/main/java/sim2/app/talleb_5edma/screens/
git add app/src/main/java/sim2/app/talleb_5edma/interfaces/

# Commiter les changements
git commit -m "feat: Ajout du module gestion du temps"

# Pousser vers le dépôt
git push origin feature/gestion-temps
```

---

## Commandes Android Studio

### Via l'interface graphique :

1. **Synchroniser Gradle** :
   - `File → Sync Project with Gradle Files`
   - Ou clic droit sur `build.gradle.kts` → `Sync Gradle Files`

2. **Nettoyer le projet** :
   - `Build → Clean Project`

3. **Reconstruire le projet** :
   - `Build → Rebuild Project`

4. **Exécuter l'application** :
   - `Run → Run 'app'`
   - Ou clic sur le bouton ▶️

---

## Vérification après intégration

### 1. Vérifier la compilation
```bash
./gradlew assembleDebug
```

### 2. Vérifier les erreurs
Dans Android Studio : `Build → Make Project` (Ctrl+F9 / Cmd+F9)

### 3. Vérifier les warnings
Dans Android Studio : `Analyze → Inspect Code`

---

## Commandes de test (optionnel)

### Lancer les tests unitaires
```bash
./gradlew test
```

### Lancer les tests instrumentés
```bash
./gradlew connectedAndroidTest
```

---

## Commandes de débogage

### Voir les logs
```bash
adb logcat | grep "CatLog"
```

### Filtrer les logs de l'application
```bash
adb logcat | grep "sim2.app.talleb_5edma"
```

### Nettoyer les logs
```bash
adb logcat -c
```

---

## Commandes pour vérifier les permissions

### Vérifier les permissions de l'app
```bash
adb shell dumpsys package sim2.app.talleb_5edma | grep permission
```

---

## Ordre d'exécution recommandé

1. **Nettoyer** :
   ```bash
   ./gradlew clean
   ```

2. **Synchroniser** :
   - Dans Android Studio : `File → Sync Project with Gradle Files`

3. **Compiler** :
   ```bash
   ./gradlew build
   ```

4. **Vérifier** :
   - Dans Android Studio : `Build → Make Project`

5. **Exécuter** :
   - Dans Android Studio : `Run → Run 'app'`

---

## Notes importantes

- ⚠️ **Toujours synchroniser Gradle** après avoir modifié `build.gradle.kts`
- ⚠️ **Nettoyer le projet** si vous rencontrez des erreurs bizarres
- ⚠️ **Vérifier la base URL** avant de tester les fonctionnalités réseau
- ⚠️ **Vérifier le token** si les appels API échouent

---

## En cas d'erreur

1. Nettoyer : `./gradlew clean`
2. Synchroniser : `File → Sync Project with Gradle Files`
3. Reconstruire : `Build → Rebuild Project`
4. Vérifier les logs : `adb logcat | grep "CatLog"`

