package com.arjun.sendbird.ui.channelList

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.arjun.sendbird.ui.base.BaseFragment
import com.google.accompanist.glide.rememberGlidePainter
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

                    val date: LocalDate =
                        Instant.ofEpochMilli(channel.createdAt).atZone(ZoneId.systemDefault())
                            .toLocalDate()

                    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM, dd")

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
                                    fontStyle = FontStyle.Italic
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = formatter.format(date),
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