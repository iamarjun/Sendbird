package com.arjun.sendbird.ui.message

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.arjun.sendbird.R
import com.arjun.sendbird.model.ChannelState
import com.arjun.sendbird.ui.base.BaseFragment
import com.arjun.sendbird.ui.message.components.DateCard
import com.arjun.sendbird.ui.message.components.ListTile
import com.arjun.sendbird.ui.message.components.MessageInput
import com.arjun.sendbird.ui.message.components.TextCard
import com.google.accompanist.glide.rememberGlidePainter
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import com.sendbird.android.BaseMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

@ExperimentalMaterialApi
@ExperimentalAnimatedInsets
@ExperimentalFoundationApi
class MessageFragment : BaseFragment() {

    private val args by navArgs<MessageFragmentArgs>()
    private val channelUrl by lazy { args.channelUrl }
    private val viewModel by viewModels<MessageViewModel>()
    private val attachmentHelper by lazy {
        AttachmentHelper(
            lifecycle,
            requireContext(),
            requireActivity().activityResultRegistry
        ) {
            Timber.d("Uri : $it")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.apply {
            loadMessages(channelUrl)
            getChannel(channelUrl)
        }
        attachmentHelper
    }

    @Composable
    override fun ToolBar() {

        val channel by viewModel.channel.observeAsState()

        TopAppBar {
            IconButton(
                onClick = {
                    findNavController().popBackStack()
                },
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back Button"
                )
            }

            Image(
                painter = rememberGlidePainter(request = channel?.members?.find { it.userId != channel?.creator?.userId }?.profileUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(56.dp)
                    .padding(6.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = channel?.members?.find { it.userId != channel?.creator?.userId }?.nickname
                    ?: "", fontSize = 16.sp
            )
        }
    }

    @Composable
    override fun MainContent(
        paddingValues: PaddingValues,
        bottomSheetScaffoldState: BottomSheetScaffoldState,
        coroutineScope: CoroutineScope,
    ) {
        val messages by viewModel.groupedMessages.observeAsState(mapOf())

        val messageToSend = remember {
            mutableStateOf("")
        }

        fun onMessageChange(newMessage: String) {
            messageToSend.value = newMessage
        }

        ChatScreen(
            paddingValues = paddingValues,
            groupedMessages = messages,
            message = messageToSend.value,
            onMessageChange = ::onMessageChange,
            onSendClick = {
                viewModel.sendMessage(channelUrl, messageToSend.value)
                onMessageChange("")
            },
            onAttachmentClick = {
                coroutineScope.launch {
                    if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                        bottomSheetScaffoldState.bottomSheetState.expand()
                    } else {
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                }
            }
        )
    }

    @Composable
    private fun ChatScreen(
        paddingValues: PaddingValues,
        groupedMessages: Map<String, List<BaseMessage>>,
        message: String,
        onMessageChange: (String) -> Unit,
        onSendClick: () -> Unit,
        onAttachmentClick: () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                groupedMessages.forEach { (date, messages) ->

                    stickyHeader {
                        DateCard(date = date)
                    }

                    items(messages) { message ->
                        TextCard(message = message)
                    }
                }
            }

            Divider()

            MessageInput(
                value = message,
                onValueChange = onMessageChange,
                onSendClick = onSendClick,
                onAttachmentClick = onAttachmentClick
            )
        }
    }

    @Composable
    override fun BottomSheet() {
        Column(
            Modifier
                .fillMaxWidth()
        ) {
            ListTile(
                title = "Open Camera",
                icon = Icons.Filled.CameraAlt,
                iconTint = colorResource(
                    id = R.color.purple_500
                ),
                onClick = {
                    attachmentHelper.openCamera()
                }
            )

            Divider()

            ListTile(
                title = "Open Gallery",
                icon = Icons.Filled.PhotoLibrary,
                iconTint = colorResource(
                    id = R.color.purple_500
                ),
                onClick = {
                    attachmentHelper.selectImage()
                }
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.channelState.flowWithLifecycle(lifecycle).onEach { state ->
            when (state) {
                is ChannelState.ChannelUpdated -> {

                }
                ChannelState.DeliveryReceiptUpdated -> {

                }
                is ChannelState.MessageAdded -> {
                    viewModel.addMessage(state.message)
                }
                is ChannelState.MessageDeleted -> {

                }
                is ChannelState.MessageUpdated -> {

                }
                ChannelState.ReadReceiptUpdated -> {

                }
                is ChannelState.TypingStatusUpdated -> {

                }
            }
        }.launchIn(lifecycleScope)

    }
}