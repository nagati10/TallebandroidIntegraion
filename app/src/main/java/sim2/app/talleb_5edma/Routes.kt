package sim2.app.talleb_5edma

object Routes {

    // --- Standard Auth & Base (From New Version) ---
    const val Screen0 = "SplashScreen"
    const val Splash = "splash"
    const val Screen1 = "ScreenLogIn"
    const val Screen2 = "ScreenSignIn"

    const val ScreenForgot = "ForgotPasswordScreen"
    const val ScreenOTP = "OTPValidationScreen"
    const val ScreenResetP = "ResetPasswordScreen"

    // --- Main Flow (From New Version) ---
    const val ScreenHome = "news"
    const val ScreenHomeEntreprise = "EntrepriseHomeScreen"
    const val ScreenStore = "Store"
    const val ScreenProfile = "Profile"
    const val ScreenEditProfile = "EditProfile"
    
    // --- Offers (From New Version) ---
    const val ScreenOffre = "ScreenOffre"
    const val ScreenCreateOffrePro = "ScreenCreateOffrePro"
    const val ScreenCreateOffreCasual = "ScreenCreateOffreCasual"
    const val ScreenCreateOffre = "ScreenCreateOffre"
    const val ScreenUpdateOffre = "ScreenUpdateOffre"
    const val OfferComparisonScreen = "offerComparisonScreen"

    // --- Communication (From New Version) ---
    const val ScreenChating = "screenChating"
    const val ScreenCall = "screenCall" 

    // --- Events (Shared) ---
    const val ScreenEvenements = "evenements"
    const val ScreenEvenementCreate = "evenements/create"
    const val ScreenEvenementEdit = "evenements/edit"

    // --- Disponibilites (Shared) ---
    const val ScreenDisponibilites = "disponibilites"
    const val ScreenDisponibiliteCreate = "disponibilites/create"
    const val ScreenDisponibiliteEdit = "disponibilites/edit"
    
    // --- Tools (From New Version) ---
    const val LocationPicker = "location_picker"
    const val AiInterviewTraining = "aiInterviewTraining"
    const val TesT = "TesT"

    // =================================================
    // MERGED FEATURES FROM FERIEL (Old Version Work)
    // =================================================
    
    // Routine Analysis
    const val ScreenRoutineAnalysis = "routine/analysis"

    // Schedule Import
    const val ScreenScheduleImport = "schedule-import"

    // AI Matching
    const val ScreenAiMatching = "ai-matching"
}