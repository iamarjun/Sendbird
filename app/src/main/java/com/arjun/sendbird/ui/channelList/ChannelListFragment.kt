package com.arjun.sendbird.ui.channelList

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.arjun.sendbird.MainViewModel
import com.arjun.sendbird.ui.base.BaseFragment
import com.arjun.sendbird.util.getHumanReadableDate
import com.google.accompanist.glide.rememberGlidePainter
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import kotlinx.coroutines.CoroutineScope

@ExperimentalMaterialApi
@ExperimentalAnimatedInsets
class ChannelListFragment : BaseFragment() {

    private val viewModel by viewModels<ChannelListViewModel>()
    private val mainViewModel by viewModels<MainViewModel>()

    @Composable
    override fun ToolBar() = TopAppBar(
        title = { Text(text = "My Chats") },
        actions = {
            IconButton(onClick = {
                mainViewModel.logout {
                    findNavController().navigate(ChannelListFragmentDirections.actionChannelListFragmentToSplashFragment())
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                )
            }
        }
    )

    @Composable
    override fun MainContent(
        paddingValues: PaddingValues,
        bottomSheetScaffoldState: BottomSheetScaffoldState,
        coroutineScope: CoroutineScope,
    ) {
        val channels = viewModel.channels.value

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {

            channels?.let {
                items(it) { channel ->
                    Card(
                        Modifier
                            .padding(paddingValues = paddingValues)
                            .clickable {
                                findNavController().navigate(
                                    ChannelListFragmentDirections.actionChannelListFragmentToChatDetailFragment(
                                        channelUrl = channel.url
                                    )
                                )
                            },
                        elevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Image(
                                painter = rememberGlidePainter(request = channel.coverUrl),
                                contentDescription = "Channel",
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(60.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(
                                verticalArrangement = Arrangement.SpaceAround,
                                modifier = Modifier.fillMaxWidth(0.8f),
                            ) {
                                Text(
                                    text = channel.name,
                                    style = MaterialTheme.typography.h6,
                                    modifier = Modifier
                                        .padding(top = 16.dp)
                                )

                                Text(
                                    text = channel.lastMessage.message.toString(),
                                    modifier = Modifier
                                        .padding(bottom = 16.dp),
                                    fontSize = 16.sp,
                                    fontStyle = FontStyle.Italic,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = channel.getHumanReadableDate(),
                                modifier = Modifier
                                    .align(Alignment.Bottom)
                                    .padding(16.dp),
                                color = Color.LightGray
                            )
                        }

                    }
                }
            }

        }

    }

}