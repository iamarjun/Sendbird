package com.arjun.sendbird.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.navigation.compose.rememberNavController
import com.arjun.media.CustomTakePicture
import com.arjun.media.CustomTakeVideo
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalAnimatedInsets
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<SendbirdViewModel>()

    private val actionRequestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            viewModel.hasNecessaryPermission(it.values.all { it == true })
        }

    private val actionTakeImage = registerForActivityResult(CustomTakePicture()) { success ->

        if (!success) {
            Timber.e("Image taken FAIL")
            return@registerForActivityResult
        }

        Timber.d("Image taken SUCCESS")

        if (viewModel.temporaryCameraImageUri == null) {
            Timber.e("Can't find previously saved temporary Camera Image URI")
        } else {
            viewModel.setCurrentMedia(viewModel.temporaryCameraImageUri!!)
            viewModel.clearTemporaryCameraImageUri()
        }

    }

    private val actionTakeVideo = registerForActivityResult(CustomTakeVideo()) { uri ->

        if (uri == null) {
            Timber.e("Video taken FAIL")
            return@registerForActivityResult
        }

        Timber.d("Video taken SUCCESS")
        viewModel.setCurrentMedia(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            SendbirdApp(
                sendbirdViewModel = viewModel,
                navController = navController,
                actionRequestPermission = actionRequestPermission,
                actionTakeImage = actionTakeImage,
                actionTakeVideo = actionTakeVideo
            )
        }
    }
}
