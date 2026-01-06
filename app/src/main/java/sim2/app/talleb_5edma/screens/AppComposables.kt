package sim2.app.talleb_5edma.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import sim2.app.talleb_5edma.R
import sim2.app.talleb_5edma.ui.theme.Talleb_5edmaTheme


@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    labelText: String,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = { Text(labelText) },
        leadingIcon = {
            if (leadingIcon != null) Icon(imageVector = leadingIcon, contentDescription = null)
        },
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        isError = isError
    )
}

@Composable
fun HeaderText(
    text : String,
    modifier: Modifier
    ){
    Text(
        text,
        fontWeight = FontWeight.Bold, fontSize = 30.sp, modifier = modifier
    )
}

@Composable
fun SocialLoginButton(
    iconResId: Int,
    buttonText: String,
    buttonTextColor: Color,
    backgroundColor: Color,
    onIconClick: () -> Unit
) {
    Button(
        onClick = onIconClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        modifier = Modifier.height(50.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(iconResId),
                contentDescription = "$buttonText icon",
                modifier = Modifier.size(27.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(30.dp))

            )
            Text(
                text = buttonText,
                color = buttonTextColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AlternativeLogIn(
    onIconClick: (index: Int) -> Unit,
    onSingUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconList = listOf(
        R.drawable.facebook,
        R.drawable.google
    )

    val nameList = listOf(
        "Facebook",
        "Google"
    )

    val colorList = listOf(
        Color.Blue,
        Color(0xFFF8F2F2)
    )
    val colorTextList = listOf(
        Color.White,
        Color.Black
    )

    Column(
        modifier.fillMaxWidth(),
        Arrangement.Center,
        Alignment.CenterHorizontally
    ) {
        Text("OR", color = Color.Red, fontSize = 20.sp)
        Spacer(Modifier.height(60.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(27.dp)
        ) {
            iconList.forEachIndexed { index, iconResId ->
                SocialLoginButton(
                    iconResId = iconResId,
                    buttonText = nameList[index],
                    buttonTextColor = colorTextList[index],
                    backgroundColor = colorList[index],
                    onIconClick = {
                        onIconClick(index) // Simply call the index-based handler
                    }
                )
            }
        }
        Spacer(Modifier.height(25.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Don't Have an Account?", color = Color.Red)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onSingUpClick) {
                Text(
                    "Sing Up", color = Color.Red, style = TextStyle(
                        textDecoration = TextDecoration.Underline
                    )
                )
            }
        }
    }
}


@Composable
fun TermsAndPrivacyText(snackBarHostState: SnackbarHostState?=null) {
    val scope = rememberCoroutineScope()

    val annotatedText = buildAnnotatedString {
        append("By registering you agree to our ")

        pushStringAnnotation("TERMS", "terms")
        val withStyle = withStyle(
            style = SpanStyle(
                color = Color.Red,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("Terms & Conditions")
        }
        pop()

        append(" and ")

        pushStringAnnotation("PRIVACY", "privacy")
        withStyle(
            style = SpanStyle(
                color = Color.Red,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("Privacy Policy")
        }
        pop()
    }

    ClickableText(
        text = annotatedText,
        style = TextStyle(
            color = Color.Red,
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        ),
        modifier = Modifier.fillMaxWidth().padding(start = 60.dp).padding(end = 60.dp)
    ) { offset ->
        annotatedText.getStringAnnotations(offset, offset).firstOrNull()?.let {
            when (it.tag) {
                "TERMS" -> {
                    scope.launch {
                        snackBarHostState?.showSnackbar(
                            message = "Coming Soon :)",
                        )
                    }
                }

                "PRIVACY" -> {
                    scope.launch {
                        snackBarHostState?.showSnackbar(
                            message = "Coming Soon :)",
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun OTPInputField(
    modifier: Modifier = Modifier,
    onOtpChange: (String) -> Unit
) {
    var otpText by remember { mutableStateOf("") }
    val maxLength = 4

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.spacedBy(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(maxLength) { index ->
                val char = otpText.getOrNull(index)

                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.onSurface)
                            .align(Alignment.BottomCenter)
                    )

                    Text(
                        text = char?.toString() ?: "",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (char != null)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        }

        BasicTextField(
            value = otpText,
            onValueChange = { newValue ->
                if (newValue.length <= maxLength && newValue.all { it.isDigit() }) {
                    otpText = newValue
                    onOtpChange(newValue)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .alpha(0f)
        )
    }
}


data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@Preview(showBackground = true)
@Composable
fun PreviewAll(){
    Talleb_5edmaTheme {
        Column {
            HeaderText("test", Modifier)
            LoginTextField(
                value = "",
                onValueChange = {},
                labelText = "text"
            )
        }
    }
}





