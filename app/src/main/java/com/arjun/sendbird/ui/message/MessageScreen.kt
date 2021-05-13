package com.arjun.sendbird.ui.message

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.arjun.sendbird.R
import com.arjun.sendbird.ui.SendbirdViewModel
import com.arjun.sendbird.ui.message.components.FileMessageCard
import com.arjun.sendbird.ui.message.components.MessageInput
import com.arjun.sendbird.ui.message.components.TextMessageCard
import com.google.accompanist.glide.rememberGlidePainter
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import com.sendbird.android.BaseMessage
import com.sendbird.android.FileMessage
import com.sendbird.android.GroupChannel
import com.sendbird.android.SendBird
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber

@ExperimentalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalAnimatedInsets
@ExperimentalFoundationApi

@Composable
fun Message(
    channelUrl: String,
    sendbirdViewModel: SendbirdViewModel,
    navController: NavController,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState()
) {

    sendbirdViewModel.getChannel(channelUrl = channelUrl)
    sendbirdViewModel.observeChannelState()

    val coroutineScope = rememberCoroutineScope()

    val messages by sendbirdViewModel.messages.collectAsState(initial = emptyList())
    val toolbarState by sendbirdViewModel.toolbarState.collectAsState()

    var message by remember {
        mutableStateOf("")
    }

    if (toolbarState.loading)
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    else
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            topBar = {
                ToolBar(
                    channel = toolbarState.channel!!,
                    isOnline = toolbarState.isOnline,
                    showTypingStatus = toolbarState.showTypingStatus,
                ) {
                    navController.navigateUp()
                }
            },
            backgroundColor = colorResource(id = R.color.light_blue_gray),
            sheetContent = {
                BottomSheet {
                    when (it) {
                        Attachments.Camera -> TODO()
                        Attachments.Gallery -> TODO()
                    }
                }
            },
            sheetPeekHeight = 0.dp,
            sheetElevation = 8.dp,
        ) {

            val modifier = Modifier.padding(it)

            MessageScreen(
                modifier = modifier,
                channel = toolbarState.channel!!,
                messages = messages,
                message = message,
                onChangeScrollPosition = sendbirdViewModel::onChangeScrollPosition,
                onMessageChange = {
                    message = it
                    sendbirdViewModel.sendTypingStatus(toolbarState.channel!!, it.isNotEmpty())
                },
                onSendClick = {
                    sendbirdViewModel.sendMessage(message = message)
                    message = ""
                },
                onAttachmentClick = {
                    coroutineScope.launch {
                        if (scaffoldState.bottomSheetState.isCollapsed) {
                            scaffoldState.bottomSheetState.expand()
                        } else {
                            scaffoldState.bottomSheetState.collapse()
                        }
                    }
                }
            )
        }
}

@ExperimentalAnimationApi
@Composable
private fun ToolBar(
    channel: GroupChannel,
    isOnline: Boolean,
    showTypingStatus: Boolean,
    onBackClick: () -> Unit,
) {

    TopAppBar {
        IconButton(
            onClick = onBackClick,
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back Button"
            )
        }

        Image(
            painter = rememberGlidePainter(request = channel.members?.find { it.userId != SendBird.getCurrentUser().userId }?.profileUrl),
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
                text = channel.members?.find { it.userId != SendBird.getCurrentUser().userId }?.nickname
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

@ExperimentalCoroutinesApi
@ExperimentalAnimationApi
@Composable
private fun MessageScreen(
    modifier: Modifier,
    channel: GroupChannel,
    messages: List<BaseMessage>,
    message: String,
    onChangeScrollPosition: (Int) -> Unit,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachmentClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        val state = rememberLazyListState()

        Timber.d("First visible item index: ${state.firstVisibleItemIndex}")

        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .weight(1f),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            state = state
        ) {

            itemsIndexed(messages) { index, message ->
                when (message is FileMessage) {
                    true -> FileMessageCard(message = message, channel = channel)
                    false -> TextMessageCard(message = message, channel = channel)
                }

                onChangeScrollPosition(index)
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

@ExperimentalMaterialApi
@Composable
private fun BottomSheet(
    onAttachmentClick: (Attachments) -> Unit
) {
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
                onAttachmentClick(Attachments.Camera)
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
                onAttachmentClick(Attachments.Gallery)
            }
        )

    }
}


//class MessageFragment : BaseFragment() {
//
//    private val args by navArgs<MessageFragmentArgs>()
//    private val channelUrl by lazy { args.channelUrl }
//    private val viewModel by viewModels<MessageViewModel>()
//    private val isTyping = MutableStateFlow(false)
//
//    private val attachmentHelper by lazy {
//        AttachmentHelper(
//            lifecycle,
//            requireContext(),
//            requireActivity().activityResultRegistry
//        ) {
//            Timber.d("Uri : $it")
//            val fileInfo = FileUtils.getFileInfo(requireContext(), it) ?: return@AttachmentHelper
//            viewModel.sendFileMessage(channelUrl, fileInfo)
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        viewModel.apply {
//            loadMessages(channelUrl)
//            getChannel(channelUrl)
//            viewModel.typingStatus(channelUrl, isTyping)
//        }
//        attachmentHelper
//    }
//
//
//    override fun onPause() {
//        super.onPause()
//        isTyping.value = false
//    }
//
//
//}
//
//override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//    super.onViewCreated(view, savedInstanceState)
//
//
//    viewModel.channelState.flowWithLifecycle(lifecycle).onEach { state ->
//        when (state) {
//            is ChannelState.ChannelUpdated -> {
//
//            }
//            ChannelState.DeliveryReceiptUpdated -> {
//
//            }
//            is ChannelState.MessageAdded -> {
//                viewModel.addMessage(state.message)
//            }
//            is ChannelState.MessageDeleted -> {
//
//            }
//            is ChannelState.MessageUpdated -> {
//
//            }
//            ChannelState.ReadReceiptUpdated -> {
//
//            }
//            is ChannelState.TypingStatusUpdated -> {
//                viewModel.showTypingIndicator(state.typingUsers.isNotEmpty())
//            }
//        }
//    }.launchIn(lifecycleScope)
//
//}
//}
//
//fun LazyListState.isScrolledToTheEnd() =
//    layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1