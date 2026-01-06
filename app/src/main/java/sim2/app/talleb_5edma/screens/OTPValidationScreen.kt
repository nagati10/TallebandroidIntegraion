package sim2.app.talleb_5edma.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import sim2.app.talleb_5edma.Routes
import sim2.app.talleb_5edma.ui.theme.Talleb_5edmaTheme

@Composable
fun OTPValidationScreen(
    navController: NavController? = null,
    modifier: Modifier,
    codeOfVerification: String,
    snackBarHostState: SnackbarHostState?=null
){
    var code by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
            Row {
                IconButton(
                    onClick = { navController?.navigate(route = Routes.ScreenForgot) },
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
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(15.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Enter the code send to you by Email or Mobile number",
                    modifier = Modifier.align(Alignment.Start),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.SemiBold
                )

                OTPInputField(
                    modifier = Modifier.fillMaxWidth(),
                    onOtpChange = { otp ->
                        code=otp
                    }
                )

                Spacer(Modifier.fillMaxHeight(0.02f))

                Button(
                    {
                        if(code==codeOfVerification)navController?.navigate(Routes.ScreenResetP)
                        else
                        scope.launch { snackBarHostState?.showSnackbar(
                            message = "Wrong code!",
                        ) }
                    },
                    Modifier.fillMaxWidth(0.9f),
                    true,
                    colors = ButtonDefaults.buttonColors(
                        Color.Red,
                        Color.White
                    )
                ) {
                    Text("Verify", fontSize = 20.sp)
                }
                Text("Didn't receive a verification code ?", fontSize = 15.sp)
                TextButton(onClick = {
                    scope.launch {
                        snackBarHostState?.showSnackbar(
                            message = "Coming Soon :)",
                        )
                    }
                }) {
                    Text(
                        "Resend code", color = Color.Red, style = TextStyle(
                            textDecoration = TextDecoration.Underline,
                            fontSize = 16.sp
                        )
                    )
                }
                Spacer(Modifier.fillMaxHeight(0.8f))
            }
        }
    }


@Preview(showBackground = true)
@Composable
fun OTPValidationScreenPreview(){
    Talleb_5edmaTheme {
        OTPValidationScreen(modifier = Modifier, codeOfVerification = "0000")
    }
}