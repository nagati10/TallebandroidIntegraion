package sim2.app.talleb_5edma.screens

import android.graphics.Bitmap
import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sim2.app.talleb_5edma.R
import sim2.app.talleb_5edma.Routes
import sim2.app.talleb_5edma.network.UserRepository
import sim2.app.talleb_5edma.ui.theme.Talleb_5edmaTheme
import sim2.app.talleb_5edma.util.FileUtils
import androidx.compose.runtime.saveable.rememberSaveable
import sim2.app.talleb_5edma.models.User
import sim2.app.talleb_5edma.network.CreateUserRequest

@Composable
fun SignUpScreen(
    navController: NavController? = null,
    modifier: Modifier,
    snackBarHostState: SnackbarHostState? = null
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var isFormValid by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isResponseError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }

    // State for selected image
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Image Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { imageUri ->
                try {
                    // Get bitmap for preview
                    val previewBitmap = FileUtils.uriToBitmap(context, imageUri)
                    bitmap = previewBitmap
                    selectedImageUri = imageUri

                    scope.launch {
                        snackBarHostState?.showSnackbar(
                            message = "Profile image selected",
                            duration = SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    scope.launch {
                        snackBarHostState?.showSnackbar(
                            message = "Failed to load image: ${e.message}",
                            duration = SnackbarDuration.Long
                        )
                    }
                }
            }
        }
    )

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        content = {
            // Back button
            Row {
                IconButton(
                    onClick = { navController?.navigate(route = Routes.Screen1) },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back arrow",
                        tint = Color.Red,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // Main content
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))

                // Clickable Profile Image
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            galleryLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Profile image",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.outline_person_24),
                            contentDescription = "Add profile image",
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    // Add camera icon overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(36.dp)
                            .background(Color.Red, CircleShape)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_photo_camera_24),
                            contentDescription = "Add photo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = if (bitmap != null) "Change profile photo" else "Add profile photo",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                SignUpForm(
                    onFullNameChange = { fullName = it },
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onConfirmPasswordChange = { confirmPassword = it },
                    onPhoneChange = { contact = it },
                    onValidationChange = { isValid ->
                        isFormValid = isValid
                    }
                )

                Button(
                    onClick = {
                        if (isFormValid) {
                            isLoading = true
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val repository = UserRepository()
                                    val userRequest = CreateUserRequest(
                                        nom = fullName,
                                        email = email,
                                        password = password,
                                        contact = contact,
                                        role = "user"
                                    )

                                    // Convert image to ByteArray if available
                                    var imageBytes: ByteArray? = null
                                    var imageFileName: String? = null

                                    selectedImageUri?.let { uri ->
                                        imageBytes = FileUtils.uriToByteArray(context, uri)
                                        imageFileName = FileUtils.getFileName(context, uri)
                                    }

                                    // Call the updated signup function with image data
                                    val response: User = repository.signup(
                                        userRequest = userRequest,
                                        imageBytes = imageBytes,
                                        imageFileName = imageFileName
                                    )

                                    withContext(Dispatchers.Main) {
                                        isLoading = false

                                        if (response._id != null) {
                                            scope.launch {
                                                snackBarHostState?.showSnackbar(
                                                    message = "Account created successfully!",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                            // Navigate to login screen
                                            navController?.navigate(Routes.Screen1) {
                                                popUpTo(Routes.Screen1) {
                                                    inclusive = true
                                                }
                                            }
                                        } else {
                                            isResponseError = true
                                            errorMessage = "Signup failed. Please try again."
                                            scope.launch {
                                                snackBarHostState?.showSnackbar(
                                                    message = errorMessage,
                                                    duration = SnackbarDuration.Long
                                                )
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        isResponseError = true
                                        errorMessage = "Error: ${e.message ?: "Unknown error occurred"}"
                                        scope.launch {
                                            snackBarHostState?.showSnackbar(
                                                message = errorMessage,
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            scope.launch {
                                snackBarHostState?.showSnackbar(
                                    message = "Please fix all errors before submitting",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(50.dp),
                    enabled = !isLoading && isFormValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "Sign Up",
                            fontSize = 18.sp
                        )
                    }
                }

                // Error message display
                if (isResponseError) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(10.dp))
                TermsAndPrivacyText(snackBarHostState)
                Spacer(Modifier.height(20.dp))
            }
        }
    )
}

@Composable
fun SignUpForm(
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onValidationChange: (Boolean) -> Unit
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val isFullNameValid = fullName.isNotEmpty() && fullName.length >= 4
    val isEmailValid = remember(email) {
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    val isPasswordValid = password.length >= 6
    val isConfirmPasswordValid = password == confirmPassword && confirmPassword.isNotEmpty()
    val isFormValid = isFullNameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid


    var contact by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedCountryCode by remember { mutableStateOf("+216") }
    var isPhoneValid = remember(contact) {
        ( Patterns.PHONE.matcher(contact).matches() && contact.length == 8 &&
                contact.all { it.isDigit() }
        )
    }

    val countryCodes = listOf(
        "+216" to "🇹🇳 Tunisia",
        "+213" to "🇩🇿 Algeria",
        "+212" to "🇲🇦 Morocco",
        "+33" to "🇫🇷 France",
        "+1" to "🇺🇸 USA",
        "+44" to "🇬🇧 UK",
        "+49" to "🇩🇪 Germany",
        "+39" to "🇮🇹 Italy",
        "+34" to "🇪🇸 Spain"
    )

    LaunchedEffect(isFormValid) {
        onValidationChange(isFormValid)
    }

    LaunchedEffect(fullName) {
        onFullNameChange(fullName)
    }

    LaunchedEffect(email) {
        onEmailChange(email)
    }

    LaunchedEffect(password) {
        onPasswordChange(password)
    }

    LaunchedEffect(confirmPassword) {
        onConfirmPasswordChange(confirmPassword)
    }
    LaunchedEffect(contact) {
        onPhoneChange(contact)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = fullName,
            onValueChange = {
                fullName = it
            },
            modifier = Modifier.fillMaxWidth(0.9f),
            label = { Text("Full Name") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = "Full name icon")
            },
            trailingIcon = {
                if (fullName.isNotEmpty()) {
                    if (!isFullNameValid) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_error_24),
                            contentDescription = "Error",
                            tint = Color(0xFFBA1A1A)
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.outline_check_circle_24),
                            contentDescription = "Valid",
                            tint = Color(0xFF4CAF50)
                        )
                    }
                }
            },
            isError = fullName.isNotEmpty() && !isFullNameValid,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        if (fullName.isEmpty()) {
            Text(
                text = "Must not be empty!",
                color = Color(0xFFBA1A1A),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        } else if (!isFullNameValid) {
            Text(
                text = "Full name is required",
                color = Color(0xFFBA1A1A),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            modifier = Modifier.fillMaxWidth(0.9f),
            label = { Text("Email") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = "Email icon")
            },
            trailingIcon = {
                Row(Modifier.padding(15.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (email.isNotEmpty()) {
                        if (!isEmailValid) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_error_24),
                                contentDescription = "Error",
                                tint = Color(0xFFBA1A1A)
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.outline_check_circle_24),
                                contentDescription = "Valid",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            },
            isError = email.isNotEmpty() && !isEmailValid,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        if (email.isEmpty()) {
            Text(
                text = "Must not be empty!",
                color = Color(0xFFBA1A1A),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        } else if (!isEmailValid) {
            Text(
                text = "Please enter a valid email address",
                color = Color(0xFFBA1A1A),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            modifier = Modifier.fillMaxWidth(0.9f),
            label = { Text("Password") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Password icon")
            },
            trailingIcon = {
                if (password.isNotEmpty() && !isPasswordValid) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_error_24),
                        contentDescription = "Error",
                        tint = Color(0xFFBA1A1A)
                    )
                } else {
                    IconButton(
                        onClick = { isPasswordVisible = !isPasswordVisible }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_remove_red_eye_24),
                            tint = if (isPasswordVisible) Color.Gray else Color.Red,
                            modifier = Modifier.size(30.dp),
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            },
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = password.isNotEmpty() && !isPasswordValid,
            singleLine = true
        )

        if (password.isEmpty()) {
            Text(
                text = "Must not be empty!",
                color = Color(0xFFBA1A1A),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        } else if (!isPasswordValid) {
            Text(
                text = "Password must be at least 6 characters",
                color = Color(0xFFBA1A1A),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
            },
            modifier = Modifier.fillMaxWidth(0.9f),
            label = { Text("Confirm Password") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Confirm password icon")
            },
            trailingIcon = {
                if (confirmPassword.isNotEmpty() && !isConfirmPasswordValid) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_error_24),
                        contentDescription = "Error",
                        tint = Color(0xFFBA1A1A)
                    )
                } else {
                    IconButton(
                        onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_remove_red_eye_24),
                            tint = if (isConfirmPasswordVisible) Color.Gray else Color.Red,
                            modifier = Modifier.size(30.dp),
                            contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            },
            visualTransformation = if (isConfirmPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = confirmPassword.isNotEmpty() && !isConfirmPasswordValid,
            singleLine = true
        )

        if (confirmPassword.isEmpty()) {
            Text(
                text = "Must not be empty!",
                color = Color(0xFFBA1A1A),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        } else if (!isConfirmPasswordValid) {
            Text(
                text = "Passwords do not match",
                color = Color(0xFFBA1A1A),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Country Code Dropdown
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .wrapContentHeight()
            ) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = selectedCountryCode,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.ArrowDropDown,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.width(200.dp)
                ) {
                    countryCodes.forEach { (code, country) ->
                        DropdownMenuItem(
                            text = {
                                Text("$code $country")
                            },
                            onClick = {
                                selectedCountryCode = code
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Phone Number Input
            OutlinedTextField(
                value = contact,
                onValueChange = {
                    // Only allow numbers
                    contact = it.filter { char -> char.isDigit() }
                },
                modifier = Modifier.weight(1f),
                label = { Text("Phone number") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = "Phone icon")
                },
                trailingIcon = {
                    if (contact.isNotEmpty()) {
                        if (!isPhoneValid) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_error_24),
                                contentDescription = "Error",
                                tint = Color(0xFFBA1A1A)
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.outline_check_circle_24),
                                contentDescription = "Valid",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                    }
                },
                isError = contact.isNotEmpty() && !isPhoneValid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = PhoneNumberTransformation(selectedCountryCode)
            )
        }


// Error messages
        if (contact.isEmpty()) {
            Text(
                text = "Phone number is required!",
                color = Color(0xFFBA1A1A),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        } else if (!isPhoneValid) {
            Text(
                text = "Phone number must be 8-15 digits",
                color = Color(0xFFBA1A1A),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }
    }
}

class PhoneNumberTransformation(private val countryCode: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // You can add formatting logic here if needed
        // For example: format as +216 12 345 678
        return TransformedText(text, OffsetMapping.Identity)
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview(){
    val navController= rememberNavController()
    Talleb_5edmaTheme {
        SignUpScreen(navController, Modifier.fillMaxSize())
    }
}