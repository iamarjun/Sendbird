package com.arjun.sendbird.ui.message

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.arjun.sendbird.model.ChannelState
import com.arjun.sendbird.ui.base.BaseFragment
import com.arjun.sendbird.util.isMe
import com.google.accompanist.glide.rememberGlidePainter
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.sendbird.android.BaseMessage
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalAnimatedInsets
class MessageFragment : BaseFragment() {

    private val args by navArgs<MessageFragmentArgs>()
    private val channelUrl by lazy { args.channelUrl }
    private val viewModel by viewModels<MessageViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.apply {
            loadMessages(channelUrl)
            getChannel(channelUrl)
        }
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
    override fun MainContent(paddingValues: PaddingValues, scaffoldState: ScaffoldState) {
        val messages by viewModel.messages.observeAsState(emptyList())

        val messageToSend = remember {
            mutableStateOf("")
        }

        fun onMessageChange(newMessage: String) {
            messageToSend.value = newMessage
        }

        ChatScreen(
            paddingValues = paddingValues,
            messages = messages,
            message = messageToSend.value,
            onMessageChange = ::onMessageChange,
            onSendClick = {
                viewModel.sendMessage(channelUrl, messageToSend.value)
                onMessageChange("")
            }
        )
    }

    @Composable
    private fun ChatScreen(
        paddingValues: PaddingValues,
        messages: List<BaseMessage>,
        message: String,
        onMessageChange: (String) -> Unit,
        onSendClick: () -> Unit,
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
        ) {
            val (lazyColumn, textField) = createRefs()

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(lazyColumn) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(textField.top)
                        height = Dimension.fillToConstraints
                    },
                reverseLayout = true
            ) {

                items(messages) { message ->
                    Text(
                        text = message.message,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        textAlign = if (message.isMe()) TextAlign.End else TextAlign.Start,
                        fontSize = 16.sp
                    )
                }
            }

            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .navigationBarsWithImePadding()
                    .constrainAs(textField) {
                        top.linkTo(lazyColumn.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    },
                label = {
                    Text(text = "Enter Message")
                },
                trailingIcon = {
                    IconButton(
                        onClick = onSendClick,
                        enabled = message.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Search Icon",
                        )
                    }
                },
                textStyle = TextStyle(
                    color = MaterialTheme.colors.onSurface,
                    background = Color.Transparent,
                ),

                keyboardOptions = KeyboardOptions(
                    autoCorrect = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
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