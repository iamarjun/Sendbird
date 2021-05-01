package com.arjun.sendbird.ui.channelList

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.arjun.sendbird.R
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                findNavController().navigate(
                                    ChannelListFragmentDirections.actionChannelListFragmentToChatDetailFragment(
                                        channelUrl = channel.url
                                    )
                                )
                            },
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_sendbird),
                            contentDescription = "Channel",
                            Modifier.padding(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = channel.name,
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier
                                .align(Alignment.Top)
                                .padding(top = 16.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = channel.createdAt.toString(),
                            modifier = Modifier
                                .align(Alignment.Bottom)
                                .padding(bottom = 16.dp)
                        )
                    }

                }
            }

        }

    }

}