package com.arjun.sendbird.ui.channelList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.arjun.sendbird.ui.base.BaseFragment
import com.google.accompanist.insets.ExperimentalAnimatedInsets

@ExperimentalAnimatedInsets
class ChannelListFragment : BaseFragment() {

    private val viewModel by viewModels<ChannelListViewModel>()

    @Composable
    override fun ToolBar() = TopAppBar(
        title = { Text(text = "My Chats") }
    )

    @Composable
    override fun MainContent(paddingValues: PaddingValues, scaffoldState: ScaffoldState) {
        val channels = viewModel.channels.value

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {

            channels?.let {
                items(it) { channel ->
                    Text(
                        text = channel.name,
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier.clickable {
                            findNavController().navigate(
                                ChannelListFragmentDirections.actionChannelListFragmentToChatDetailFragment(
                                    channelUrl = channel.url
                                )
                            )
                        }
                    )
                }
            }

        }

    }

}