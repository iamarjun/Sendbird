package com.arjun.sendbird.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.arjun.sendbird.LocalBackPressedDispatcher
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ViewWindowInsetObserver
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalAnimatedInsets
@AndroidEntryPoint
abstract class BaseFragment : Fragment() {

    @Composable
    abstract fun MainContent(paddingValues: PaddingValues, scaffoldState: ScaffoldState)

    @Composable
    abstract fun ToolBar()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return ComposeView(requireContext()).apply {
            setContent {
                val windowInsets = ViewWindowInsetObserver(this)
                    .start(windowInsetsAnimationsEnabled = true)

                CompositionLocalProvider(
                    LocalBackPressedDispatcher provides requireActivity().onBackPressedDispatcher,
                    LocalWindowInsets provides windowInsets,
                ) {
                    val scaffoldState = rememberScaffoldState()
                    Scaffold(
                        scaffoldState = scaffoldState,
                        topBar = { ToolBar() }
                    ) {
                        MainContent(paddingValues = it, scaffoldState = scaffoldState)
                    }
                }
            }
        }
    }
}