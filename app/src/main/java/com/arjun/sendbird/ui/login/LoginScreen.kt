package com.arjun.sendbird.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.arjun.sendbird.R
import com.arjun.sendbird.ui.SendbirdViewModel
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import kotlinx.coroutines.launch
import java.util.*

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalAnimatedInsets

@Composable
fun Login(
    sendbirdViewModel: SendbirdViewModel,
    navController: NavController,
    scaffoldState: ScaffoldState = rememberScaffoldState()
) {
    val state by sendbirdViewModel.loginState.collectAsState()

    val snackBarScope = rememberCoroutineScope()

    var userId by remember {
        mutableStateOf("")
    }

    if (state.isUserLoggedIn)
        navController.navigate("channel_list")

    if (state.error != null) {
        LaunchedEffect(scaffoldState) {
            scaffoldState.snackbarHostState.showSnackbar(state.error?.message?: "Something went wrong")
        }
    }

    Scaffold(
        scaffoldState = scaffoldState
    ) {
        val modifier = Modifier.padding(it)
        LoginScreen(
            modifier = modifier,
            value = userId,
            onValueChange = {
                userId = it
            },
            onConnectClick = {
                if (userId.isEmpty() || userId.length < 6) {
                    snackBarScope.launch {
                        scaffoldState.snackbarHostState.showSnackbar("Please enter a user id")
                    }
                    return@LoginScreen
                }
                sendbirdViewModel.login(userId = userId)
            },
            progressVisibility = state.isLoading
        )
    }
}

@ExperimentalAnimationApi
@Composable
private fun LoginScreen(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onConnectClick: () -> Unit,
    progressVisibility: Boolean,
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Image(
            painter = painterResource(id = R.drawable.logo_sendbird),
            contentDescription = null,
            modifier
                .size(150.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(
            modifier = modifier
                .height(32.dp)
                .fillMaxWidth()
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .padding(
                    horizontal = 16.dp,
                )
                .fillMaxWidth(),
            label = {
                Text(text = "Enter User ID")
            },
            textStyle = TextStyle(
                color = MaterialTheme.colors.onSurface,
                background = Color.Transparent,
            ),

            keyboardOptions = KeyboardOptions(
                autoCorrect = true,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
        )

        Spacer(
            modifier = modifier
                .height(16.dp)
                .fillMaxWidth()
        )

        Button(
            onClick = onConnectClick,
        ) {
            Text(text = "Connect")
        }

        AnimatedVisibility(visible = progressVisibility) {
            CircularProgressIndicator(
                modifier = modifier.padding(16.dp)
            )
        }
    }
}