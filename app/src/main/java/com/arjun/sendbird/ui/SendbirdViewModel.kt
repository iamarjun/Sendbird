package com.arjun.sendbird.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjun.sendbird.data.dataSource.channel.ChannelDataSource
import com.arjun.sendbird.data.dataSource.connection.ConnectionDataSource
import com.arjun.sendbird.data.dataSource.messages.MessageDataSource
import com.arjun.sendbird.data.dataSource.messages.MessageDataSourceImp
import com.arjun.sendbird.data.dataSource.user.UserDataSource
import com.arjun.sendbird.ui.channelList.ChannelListState
import com.arjun.sendbird.ui.login.LoginScreenState
import com.arjun.sendbird.ui.message.MessageScreenState
import com.arjun.sendbird.ui.message.ToolBarState
import com.arjun.sendbird.util.PAGE_SIZE
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
            try {
                combine(
                    //TODO: expose flow instead of this hack
                    flowOf(connectionDataSource.connect(userId)),
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
            } catch (e: Exception) {
                /**
                 *  TODO: Workaround for not exposing a flow in the first place,
                 *  to catch when the ConnectionDataSource::connect throws an exception
                 */
                _loginState.value = LoginScreenState(
                    isLoading = false,
                    isUserLoggedIn = false,
                    error = e
                )
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
            combine(
                loading,
                flowOf(channelDataSource.loadChannels())
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

    private val channelState = channelDataSource.observeChannels()

    fun getChannel(channelUrl: String) {
        viewModelScope.launch {
            val channel = channelDataSource.getChannel(channelUrl = channelUrl)
            val userIdToObserve =
                channel.members?.find { it.userId != SendBird.getCurrentUser().userId }?.userId
            combine(
                flowOf(channel),
                messageDataSource.loadMessages(channel = channel, scrollPosition, page),
                userDataSource.observeUserOnlinePresence(userIdToObserve),
                ) { groupChannel, messageList, isUserOnline ->
                MessageScreenState(
                    loading = false,
                    toolBarState = ToolBarState(
                        channel = groupChannel,
                        isOnline = isUserOnline,
                        showTypingStatus = false,
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

    /**
     * ---------------------------------------------------------------------------------------------
     * ---------------------------------------------------------------------------------------------
     * --------------------------------------- PAGINATION ------------------------------------------
     * ---------------------------------------------------------------------------------------------
     * ---------------------------------------------------------------------------------------------
     */

    private val scrollPosition by lazy { MutableStateFlow(0) }
    private val page by lazy { MutableStateFlow(1) }

    fun onChangeScrollPosition(position: Int) {
        scrollPosition.value = position
        //TODO: Improve pagination logic
        if (position + 1 >= page.value * PAGE_SIZE) {
            page.value = ++page.value
        }
    }

}
