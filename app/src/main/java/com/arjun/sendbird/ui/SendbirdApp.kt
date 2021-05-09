package com.arjun.sendbird.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.arjun.sendbird.theme.SendbirdTheme
import com.arjun.sendbird.ui.channelList.ChannelList
import com.arjun.sendbird.ui.login.Login
import com.google.accompanist.insets.ExperimentalAnimatedInsets

@ExperimentalAnimatedInsets
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun SendbirdApp(
    sendbirdViewModel: SendbirdViewModel,
    navController: NavHostController
) {
    SendbirdTheme {
        NavHost(
            navController = navController,
            startDestination = "login"
        ) {
            composable(route = "login") {
                Login(
                    sendbirdViewModel = sendbirdViewModel,
                    navController = navController
                )
            }

            composable(route = "channel_list") {
                ChannelList(
                    sendbirdViewModel = sendbirdViewModel,
                    navController = navController
                )
            }
        }
    }
}