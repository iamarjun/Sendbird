package com.arjun.sendbird.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.arjun.sendbird.R
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ViewWindowInsetObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope

@ExperimentalMaterialApi
@ExperimentalAnimatedInsets
@AndroidEntryPoint
abstract class BaseFragment : Fragment() {

    @Composable
    abstract fun MainContent(
        paddingValues: PaddingValues,
        bottomSheetScaffoldState: BottomSheetScaffoldState,
        coroutineScope: CoroutineScope,
    )

    @Composable
    open fun ToolBar() {
    }

    @Composable
    open fun BottomSheet() {
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return ComposeView(requireContext()).apply {
            setContent {
                val windowInsets = ViewWindowInsetObserver(this)
                    .start(windowInsetsAnimationsEnabled = true)

                val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
                    bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
                )

                val coroutineScope = rememberCoroutineScope()

                CompositionLocalProvider(
                    LocalWindowInsets provides windowInsets,
                ) {

                    BottomSheetScaffold(
                        scaffoldState = bottomSheetScaffoldState,
                        topBar = { ToolBar() },
                        backgroundColor = colorResource(id = R.color.light_blue_gray),
                        sheetContent = { BottomSheet() },
                        sheetPeekHeight = 0.dp,
                        sheetElevation = 8.dp,
                    ) {
                        MainContent(
                            paddingValues = it,
                            bottomSheetScaffoldState = bottomSheetScaffoldState,
                            coroutineScope
                        )
                    }
                }
            }
        }
    }
}