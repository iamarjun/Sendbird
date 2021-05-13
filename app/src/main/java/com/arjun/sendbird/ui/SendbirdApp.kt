package com.arjun.sendbird.ui

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.arjun.sendbird.theme.SendbirdTheme
import com.arjun.sendbird.ui.channelList.ChannelList
import com.arjun.sendbird.ui.login.Login
import com.arjun.sendbird.ui.message.Message
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalAnimatedInsets
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun SendbirdApp(
    sendbirdViewModel: SendbirdViewModel,
    navController: NavHostController,
    actionRequestPermission: ActivityResultLauncher<Array<String>>,
    actionTakeImage: ActivityResultLauncher<Uri>,
    actionTakeVideo: ActivityResultLauncher<Uri>
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

            composable(route = "messages/{channelUrl}") { backStackEntry ->

                val channelUrl =
                    backStackEntry.arguments?.getString("channelUrl") ?: return@composable

                Message(
                    channelUrl = channelUrl,
                    sendbirdViewModel = sendbirdViewModel,
                    navController = navController,
                    actionRequestPermission = actionRequestPermission,
                    actionTakeImage = actionTakeImage,
                    actionTakeVideo = actionTakeVideo
                )
            }
        }
    }
}