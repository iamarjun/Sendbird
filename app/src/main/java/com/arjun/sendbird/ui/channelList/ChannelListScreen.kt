package com.arjun.sendbird.ui.channelList

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.arjun.sendbird.ui.SendbirdViewModel
import com.arjun.sendbird.util.getHumanReadableDate
import com.google.accompanist.glide.rememberGlidePainter
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import com.sendbird.android.BaseChannel
import com.sendbird.android.GroupChannel

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalAnimatedInsets
@Composable
fun ChannelList(
    sendbirdViewModel: SendbirdViewModel,
    navController: NavController,
    scaffoldState: ScaffoldState = rememberScaffoldState()
) {

    sendbirdViewModel.getChannels()

    val state by sendbirdViewModel.channelListState.collectAsState()

    Scaffold(
        topBar = {
            ToolBar(
                logout = {
                    sendbirdViewModel.logout {
                        navController.navigate("messages")
                    }
                }
            )
        },
        scaffoldState = scaffoldState,
    ) {
        val modifier = Modifier.padding(it)

        AnimatedVisibility(visible = state.loading) {
            Box(modifier = modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = modifier.align(Alignment.Center)
                )
            }
        }

        AnimatedVisibility(visible = state.loading.not()) {
            ChannelList(
                modifier = modifier,
                channels = state.channelList
            ) { channelUrl ->
                navController.navigate("messages/$channelUrl")
            }
        }

    }

}


@Composable
private fun ToolBar(
    logout: () -> Unit,
) = TopAppBar(
    title = { Text(text = "My Chats") },
    actions = {
        IconButton(onClick = logout) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Logout",
            )
        }
    }
)

@Composable
private fun ChannelList(
    modifier: Modifier,
    channels: List<BaseChannel>,
    navigateTo: (String) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        items(channels) { channel ->
            ChannelCard(
                modifier = modifier,
                channel = (channel as GroupChannel)
            ) { channelUrl ->
                navigateTo(channelUrl)
            }
        }
    }
}

@Composable
private fun ChannelCard(
    modifier: Modifier,
    channel: GroupChannel,
    onChannelClick: (String) -> Unit,
) {
    Card(
        modifier = modifier
            .clickable {
                onChannelClick(channel.url)
            },
        elevation = 8.dp
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
        ) {
            Image(
                painter = rememberGlidePainter(request = channel.coverUrl),
                contentDescription = "Channel",
                modifier = modifier
                    .padding(16.dp)
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = modifier.width(8.dp))

            Column(
                verticalArrangement = Arrangement.SpaceAround,
                modifier = modifier.fillMaxWidth(0.8f),
            ) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.h6,
                    modifier = modifier
                        .padding(top = 16.dp)
                )

                Text(
                    text = channel.lastMessage.message.toString(),
                    modifier = modifier
                        .padding(bottom = 16.dp),
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = modifier.weight(1f))

            Text(
                text = channel.getHumanReadableDate(),
                modifier = modifier
                    .align(Alignment.Bottom)
                    .padding(16.dp),
                color = Color.Gray
            )
        }

    }
}