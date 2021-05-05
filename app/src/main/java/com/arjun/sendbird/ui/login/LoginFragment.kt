package com.arjun.sendbird.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.arjun.sendbird.MainViewModel
import com.arjun.sendbird.R
import com.arjun.sendbird.model.Resource
import com.arjun.sendbird.ui.base.BaseFragment
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalAnimatedInsets
class LoginFragment : BaseFragment() {

    private val viewModel by activityViewModels<MainViewModel>()
    private val progressVisibility = mutableStateOf(false)


    @Composable
    override fun MainContent(
        paddingValues: PaddingValues,
        bottomSheetScaffoldState: BottomSheetScaffoldState,
        coroutineScope: CoroutineScope,
    ) {
        val snackBarScope = rememberCoroutineScope()

        val userId by viewModel.userId.observeAsState("")
        val userExist by viewModel.userExist.observeAsState()

        progressVisibility.value = userExist is Resource.Loading

        when (userExist) {
            is Resource.Error -> {
                LaunchedEffect(userExist) {
                    bottomSheetScaffoldState.snackbarHostState.showSnackbar(
                        userExist?.e?.message ?: "Something went wrong"
                    )
                }
            }
            is Resource.Loading -> {
            }
            is Resource.Success -> findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToChannelListFragment())
        }

        LoginScreen(
            paddingValues = paddingValues,
            textFieldValue = userId,
            onTextFieldValueChange = viewModel::onUserIdChange,
            onConnectClick = {
                if (userId.isEmpty() || userId.length < 6) {
                    snackBarScope.launch {
                        bottomSheetScaffoldState.snackbarHostState.showSnackbar("Please enter a user id")
                    }
                    return@LoginScreen
                }
                viewModel.login(userId = userId)
            },
            progressVisibility = progressVisibility.value
        )
    }

    @Composable
    private fun LoginScreen(
        paddingValues: PaddingValues,
        textFieldValue: String,
        onTextFieldValueChange: (String) -> Unit,
        onConnectClick: () -> Unit,
        progressVisibility: Boolean,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Image(
                painter = painterResource(id = R.drawable.logo_sendbird),
                contentDescription = null,
                Modifier
                    .size(150.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(
                modifier = Modifier
                    .height(32.dp)
                    .fillMaxWidth()
            )

            OutlinedTextField(
                value = textFieldValue,
                onValueChange = onTextFieldValueChange,
                modifier = Modifier
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
                modifier = Modifier
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
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}