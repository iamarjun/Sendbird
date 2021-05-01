package com.arjun.sendbird.ui.login

import android.os.Bundle
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.arjun.sendbird.MainViewModel
import com.arjun.sendbird.ui.base.BaseFragment
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@ExperimentalAnimatedInsets
class LoginFragment : BaseFragment() {

    private val viewModel by activityViewModels<MainViewModel>()

    @Composable
    override fun ToolBar() {

    }

    @Composable
    override fun MainContent(paddingValues: PaddingValues, scaffoldState: ScaffoldState) {
        val snackBarScope = rememberCoroutineScope()

        val textField = remember {
            mutableStateOf("")
        }
        val textFieldValue = textField.value

        val progressVisibility = remember {
            mutableStateOf(false)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            TextField(
                value = textFieldValue,
                onValueChange = {
                    textField.value = it
                },
                modifier = Modifier
                    .padding(8.dp)
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
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    backgroundColor = Color.LightGray,
                )
            )

            Button(
                onClick = {
                    if (textFieldValue.isEmpty()) {
                        snackBarScope.launch {
                            scaffoldState.snackbarHostState.showSnackbar("Please enter a user id")
                        }
                        return@Button
                    }

                    viewModel.login(textFieldValue)
                    progressVisibility.value = true
                },
            ) {
                Text(text = "Login")
            }

            AnimatedVisibility(visible = progressVisibility.value) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userExist.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
                if (it) {
                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToChannelListFragment())
                }

            }.launchIn(lifecycleScope)

    }
}