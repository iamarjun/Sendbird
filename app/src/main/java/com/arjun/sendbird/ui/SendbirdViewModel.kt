package com.arjun.sendbird.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjun.sendbird.data.dataSource.channel.ChannelDataSource
import com.arjun.sendbird.data.dataSource.connection.ConnectionDataSource
import com.arjun.sendbird.data.dataSource.messages.MessageDataSource
import com.arjun.sendbird.data.dataSource.user.UserDataSource
import com.arjun.sendbird.model.ChannelState
import com.arjun.sendbird.ui.channelList.ChannelListState
import com.arjun.sendbird.ui.login.LoginScreenState
import com.arjun.sendbird.ui.message.MessageScreenState
import com.arjun.sendbird.ui.message.ToolBarState
import com.arjun.sendbird.util.PAGE_SIZE
import com.sendbird.android.GroupChannel
import com.sendbird.android.SendBird
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SendbirdViewModel @Inject constructor(
    private val connectionDataSource: ConnectionDataSource,
    private val channelDataSource: ChannelDataSource,
    private val messageDataSource: MessageDataSource,
    private val userDataSource: UserDataSource,
) : ViewModel() {

    /**
     * ---------------------------------------------------------------------------------------------
     * ---------------------------------------------------------------------------------------------
     * -------------------------------------- LOGIN SCREEN -----------------------------------------
     * ---------------------------------------------------------------------------------------------
     * ---------------------------------------------------------------------------------------------
     */

    private val loading = MutableStateFlow(false)

    private val _loginState = MutableStateFlow(LoginScreenState(isLoading = false))
    val loginState = _loginState.asStateFlow()

    fun login(userId: String) {
        viewModelScope.launch {
            combine(
                connectionDataSource.connect(userId),
                loading,
            ) { isUserLoggedIn, loading ->
                LoginScreenState(
                    isLoading = loading,
                    isUserLoggedIn = isUserLoggedIn
                )
            }.catch {
                LoginScreenState(
                    isLoading = false,
                    isUserLoggedIn = false,
                    error = it
                )
            }.collect {
                _loginState.value = it
            }
        }
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * ---------------------------------------------------------------------------------------------
     * ----------------------------------- CHANNEL LIST SCREEN -------------------------------------
     * ---------------------------------------------------------------------------------------------
     * ---------------------------------------------------------------------------------------------
     */

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            connectionDataSource.disconnect {
                onLogout()
            }
        }
    }

    private val _channelListState = MutableStateFlow(ChannelListState(loading = true))
    val channelListState = _channelListState.asStateFlow()


    fun getChannels() {
        viewModelScope.launch {
            channelDataSource.loadChannels()

            combine(
                loading,
                channelDataSource.channels
            ) { loading, channels ->
                ChannelListState(
                    loading = loading,
                    channelList = channels
                )
            }.catch {
                throw it
            }.collect {
                _channelListState.value = it
            }
        }
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * ---------------------------------------------------------------------------------------------
     * ------------------------------------- MESSAGES SCREEN ---------------------------------------
     * ---------------------------------------------------------------------------------------------
     * ---------------------------------------------------------------------------------------------
     */

    private val _messageScreenState = MutableStateFlow(MessageScreenState(loading = true))
    val messageScreenState = _messageScreenState.asStateFlow()

    private lateinit var groupChannel: GroupChannel

    fun getChannel(channelUrl: String) {
        viewModelScope.launch {
            channelDataSource.getChannel(channelUrl = channelUrl)
            channelDataSource.channel
                .filterNotNull()
                .collect { channel ->
                    groupChannel = channel
                    val userIdToObserve =
                        channel.members?.find { it.userId != SendBird.getCurrentUser().userId }?.userId
                    messageDataSource.loadMessages(channel = channel)
                    combine(
                        channelDataSource.channel,
                        messageDataSource.messages,
                        userDataSource.observeUserOnlinePresence(userIdToObserve),
                        channelDataSource.channelState
                    ) { groupChannel, messageList, isUserOnline, channelState ->
                        MessageScreenState(
                            loading = false,
                            toolBarState = ToolBarState(
                                channel = groupChannel,
                                isOnline = isUserOnline,
                                showTypingStatus = channelState is ChannelState.TypingStatusUpdated && channelState.typingUsers.isNotEmpty(),
                            ),
                            messages = messageList,
                        )
                    }.catch {
                        throw it
                    }.collect {
                        _messageScreenState.value = it
                    }
                }
        }
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * ---------------------------------------------------------------------------------------------
     * --------------------------------------- PAGINATION ------------------------------------------
     * ---------------------------------------------------------------------------------------------
     * ---------------------------------------------------------------------------------------------
     */

    private var scrollPosition = 0
    private var page = 1

    fun onChangeScrollPosition(position: Int) {
        scrollPosition = position
        if (position + 1 >= page * PAGE_SIZE) {
            ++page
            if (::groupChannel.isInitialized)
                viewModelScope.launch {
                    val createdAt = messageDataSource.lastMessage.createdAt
                    messageDataSource.loadMessages(groupChannel, createdAt)
                }
        }
    }

    fun sendTypingStatus(channel: GroupChannel, isTyping: Boolean) {
        viewModelScope.launch {
            messageDataSource.sendTypingStatus(channel, isTyping)
        }
    }


}
