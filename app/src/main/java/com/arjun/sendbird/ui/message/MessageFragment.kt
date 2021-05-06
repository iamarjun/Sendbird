package com.arjun.sendbird.ui.message

import android.os.Bundle
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
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
import com.arjun.sendbird.ui.message.components.*
import com.arjun.sendbird.util.FileUtils
import com.google.accompanist.glide.rememberGlidePainter
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import com.sendbird.android.BaseMessage
import com.sendbird.android.FileMessage
import com.sendbird.android.SendBird
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

@ExperimentalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalAnimatedInsets
@ExperimentalFoundationApi
class MessageFragment : BaseFragment() {

    private val args by navArgs<MessageFragmentArgs>()
    private val channelUrl by lazy { args.channelUrl }
    private val viewModel by viewModels<MessageViewModel>()
    private val isTyping = MutableStateFlow(false)

    private val attachmentHelper by lazy {
        AttachmentHelper(
            lifecycle,
            requireContext(),
            requireActivity().activityResultRegistry
        ) {
            Timber.d("Uri : $it")
            val fileInfo = FileUtils.getFileInfo(requireContext(), it) ?: return@AttachmentHelper
            viewModel.sendFileMessage(channelUrl, fileInfo)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.apply {
            loadMessages(channelUrl)
            getChannel(channelUrl)
            viewModel.typingStatus(channelUrl, isTyping)
        }
        attachmentHelper
    }

    @Composable
    override fun ToolBar() {

        val channel by viewModel.channel.observeAsState()
        val isOnline by viewModel.isOnline.collectAsState(initial = false)
        val showTypingStatus by viewModel.showTypingStatus.observeAsState(initial = false)

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
                painter = rememberGlidePainter(request = channel?.members?.find { it.userId != SendBird.getCurrentUser().userId }?.profileUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(56.dp)
                    .padding(6.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = channel?.members?.find { it.userId != SendBird.getCurrentUser().userId }?.nickname
                        ?: "", fontSize = 16.sp
                )
                AnimatedVisibility(visible = isOnline) {
                    Text(
                        text = if (showTypingStatus) "typing..." else "online",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color.LightGray,
                        )
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        isTyping.value = false
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
            lifecycleScope.launch {
                isTyping.emit(newMessage.isNotEmpty())
            }
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
        val channel by viewModel.channel.observeAsState()

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
                        when (message is FileMessage) {
                            true -> FileMessageCard(message = message, channel = channel)
                            false -> TextMessageCard(message = message, channel = channel)
                        }

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

            ListItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Search Icon",
                        tint = colorResource(
                            id = R.color.purple_500
                        ),
                        modifier = Modifier.padding(16.dp)
                    )
                },
                text = {
                    Text(text = "Open Camera")
                },
                modifier = Modifier.clickable {
                    attachmentHelper.openCamera()
                }
            )

            Divider()

            ListItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = "Search Icon",
                        tint = colorResource(
                            id = R.color.purple_500
                        ),
                        modifier = Modifier.padding(16.dp)
                    )
                },
                text = {
                    Text(text = "Open Gallery")
                },
                modifier = Modifier.clickable {
                    attachmentHelper.openGallery()
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
                    viewModel.showTypingIndicator(state.typingUsers.isNotEmpty())
                }
            }
        }.launchIn(lifecycleScope)

    }
}