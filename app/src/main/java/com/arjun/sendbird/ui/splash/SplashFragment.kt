package com.arjun.sendbird.ui.splash

import android.os.Bundle
import android.view.View
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.arjun.sendbird.MainViewModel
import com.arjun.sendbird.R
import com.arjun.sendbird.cache.UserManager
import com.arjun.sendbird.model.Resource
import com.arjun.sendbird.ui.base.BaseFragment
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalAnimatedInsets
@AndroidEntryPoint
class SplashFragment : BaseFragment() {

    private val viewModel by viewModels<MainViewModel>()

    @Inject
    internal lateinit var userManager: UserManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userManager.getUserId().flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
                if (it.isNotEmpty())
                    viewModel.login(it)
                else {
                    delay(3000)
                    findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToLoginFragment())
                }
            }.launchIn(lifecycleScope)

    }

    @Composable
    override fun MainContent(
        paddingValues: PaddingValues,
        bottomSheetScaffoldState: BottomSheetScaffoldState,
        coroutineScope: CoroutineScope,
    ) {

        val userExist by viewModel.userExist.observeAsState()

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
            is Resource.Success -> findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToChannelListFragment())
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_sendbird_full),
                contentDescription = "Sendbird Logo",
                alignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )

            CircularProgressIndicator(
                modifier = Modifier
                    .size(64.dp)
                    .padding(16.dp),
            )
        }
    }
}