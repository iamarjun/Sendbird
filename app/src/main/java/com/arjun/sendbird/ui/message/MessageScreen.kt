package com.arjun.sendbird.ui.message

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.arjun.media.*
import com.arjun.sendbird.R
import com.arjun.sendbird.data.model.Attachments
import com.arjun.sendbird.ui.message.MessageViewModel.MediaType.IMAGE
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
    navController: NavController,
    viewModel: MessageViewModel = hiltViewModel(),
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState()
) {

    val actionRequestPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            viewModel.hasNecessaryPermission(it.values.all { it == true })
        }

    val actionTakeImage =
        rememberLauncherForActivityResult(CustomTakePicture()) { success: Boolean ->

            if (!success) {
                Timber.e("Image taken FAIL")
                return@rememberLauncherForActivityResult
            }

            Timber.d("Image taken SUCCESS")

            if (viewModel.temporaryCameraImageUri == null) {
                Timber.e("Can't find previously saved temporary Camera Image URI")
            } else {
                viewModel.setCurrentMedia(viewModel.temporaryCameraImageUri!!)
                viewModel.clearTemporaryCameraImageUri()
            }


        }

    val actionTakeVideo = rememberLauncherForActivityResult(CustomTakeVideo()) { uri: Uri? ->

        if (uri == null) {
            Timber.e("Video taken FAIL")
            return@rememberLauncherForActivityResult
        }

        Timber.d("Video taken SUCCESS")
//        viewModel.setCurrentMedia(uri)
    }

    viewModel.getChannel(channelUrl = channelUrl)
    viewModel.observeChannelState()

    val coroutineScope = rememberCoroutineScope()

    val messages by viewModel.messages.collectAsState(initial = emptyList())
    val toolbarState by viewModel.toolbarState.collectAsState()
    val hasNecessaryPermission by viewModel.hasNecessaryPermission.collectAsState(initial = false)

    val handleAttachmentClick = {
        coroutineScope.launch {
            if (scaffoldState.bottomSheetState.isCollapsed) {
                scaffoldState.bottomSheetState.expand()
            } else {
                scaffoldState.bottomSheetState.collapse()
            }
        }
    }

    if (hasNecessaryPermission)
        handleAttachmentClick()

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
                        Attachments.Camera -> viewModel.createMediaUriForCamera(IMAGE) { uri ->
                            viewModel.saveTemporaryCameraImageUri(uri)
                            actionTakeImage.launch(uri)
                        }
                        Attachments.Gallery -> TODO()
                    }
                }
            },
            sheetPeekHeight = 0.dp,
            sheetElevation = 8.dp,
        ) {

            val modifier = Modifier.padding(it)
            val context = LocalContext.current

            MessageScreen(
                modifier = modifier,
                channel = toolbarState.channel!!,
                messages = messages,
                message = message,
                onChangeScrollPosition = viewModel::onChangeScrollPosition,
                onMessageChange = {
                    message = it
                    viewModel.sendTypingStatus(toolbarState.channel!!, it.isNotEmpty())
                },
                onSendClick = {
                    viewModel.sendMessage(message = message)
                    message = ""
                },
                onAttachmentClick = {
                    if (
                        canReadOwnEntriesInMediaStore(context) &&
                        canWriteOwnEntriesInMediaStore(context) &&
                        canReadSharedEntriesInMediaStore(context) &&
                        canWriteSharedEntriesInMediaStore(context)
                    ) {
                        handleAttachmentClick()
                    } else {
                        val permission = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                            arrayOf(
                                WRITE_EXTERNAL_STORAGE,
                                READ_EXTERNAL_STORAGE
                            ) else
                            arrayOf(
                                READ_EXTERNAL_STORAGE
                            )

                        actionRequestPermission.launch(permission)
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

        Spacer(modifier = Modifier.height(8.dp))

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