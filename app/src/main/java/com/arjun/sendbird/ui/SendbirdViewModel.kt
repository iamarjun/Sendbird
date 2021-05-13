package com.arjun.sendbird.ui

import android.net.Uri
import androidx.lifecycle.*
import com.arjun.media.*
import com.arjun.sendbird.data.dataSource.channel.ChannelDataSource
import com.arjun.sendbird.data.dataSource.connection.ConnectionDataSource
import com.arjun.sendbird.data.dataSource.messages.MessageDataSource
import com.arjun.sendbird.data.dataSource.user.UserDataSource
import com.arjun.sendbird.data.model.Attachments
import com.arjun.sendbird.data.model.ChannelState
import com.arjun.sendbird.ui.channelList.ChannelListState
import com.arjun.sendbird.ui.login.LoginScreenState
import com.arjun.sendbird.ui.message.ToolBarState
import com.arjun.sendbird.util.PAGE_SIZE
import com.sendbird.android.GroupChannel
import com.sendbird.android.SendBird
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SendbirdViewModel @Inject constructor(
    private val connectionDataSource: ConnectionDataSource,
    private val channelDataSource: ChannelDataSource,
    private val messageDataSource: MessageDataSource,
    private val userDataSource: UserDataSource,
    private val mediaStoreClient: MediaStoreClient,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /**
     * ---------------------------------------------------------------------------------------------
     * ---------------------------------------------------------------------------------------------
     * -------------------------------------- LOGIN SCREEN -----------------------------------------
     * ---------------------------------------------------------------------------------------------
     * ---------------------------------------------------------------------------------------------
     */


    private val _loginState = MutableStateFlow(LoginScreenState(isLoading = false))
    val loginState = _loginState.asStateFlow()

    fun login(userId: String) {
        viewModelScope.launch {
            connectionDataSource.connect(userId).map { isUserLoggedIn ->
                LoginScreenState(
                    isLoading = true,
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
            channelDataSource.loadChannels().map { channels ->
                ChannelListState(
                    loading = false,
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

//    private val _messageScreenState = MutableStateFlow(MessageScreenState(loading = true))
//    val messageScreenState = _messageScreenState.asStateFlow()

    private val _toolbarState = MutableStateFlow(ToolBarState(loading = true))
    val toolbarState = _toolbarState.asStateFlow()

    val messages = messageDataSource.messages

    private val typingStatus = MutableStateFlow(false)

    private lateinit var groupChannel: GroupChannel

    fun sendMessage(message: String) {
        viewModelScope.launch {
            if (::groupChannel.isInitialized.not())
                return@launch

            messageDataSource.sendMessage(groupChannel, message)
        }
    }

    fun sendFileMessage(mediaResource: MediaResource) {
        viewModelScope.launch {
            if (::groupChannel.isInitialized.not())
                return@launch

            messageDataSource.sendFileMessage(groupChannel, mediaResource)
        }
    }

    fun observeChannelState() {
        viewModelScope.launch {
            channelDataSource.channelState.collect {
                when (it) {
                    is ChannelState.ChannelUpdated -> {
                    }
                    ChannelState.DeliveryReceiptUpdated -> {
                    }
                    ChannelState.Init -> {
                    }
                    is ChannelState.MessageAdded -> {
                        messageDataSource.addMessage(it.message)
                    }
                    is ChannelState.MessageDeleted -> {
                    }
                    is ChannelState.MessageUpdated -> {
                    }
                    ChannelState.ReadReceiptUpdated -> {
                    }
                    is ChannelState.TypingStatusUpdated -> {
                        typingStatus.emit(it.typingUsers.isNotEmpty())
                    }
                }
            }
        }
    }

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
                        userDataSource.observeUserOnlinePresence(userIdToObserve)
                            .stateIn(viewModelScope),
                        typingStatus.asStateFlow()
                    ) { isUserOnline, typingStatus ->
                        ToolBarState(
                            loading = false,
                            channel = channel,
                            isOnline = false,
                            showTypingStatus = false
                        )
                    }.catch {
                        throw it
                    }.collect {
                        _toolbarState.value = it
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

    /**
     * ---------------------------------------------------------------------------------------------
     * ---------------------------------------------------------------------------------------------
     * --------------------------------- PERMISSIONS & MEDIA ---------------------------------------
     * ---------------------------------------------------------------------------------------------
     * ---------------------------------------------------------------------------------------------
     */

    private val _hasNecessaryPermission = MutableStateFlow(false)
    val hasNecessaryPermission = _hasNecessaryPermission.asStateFlow()

    private val currentMedia: MutableStateFlow<MediaResource?> = MutableStateFlow(null)

//    init {
//        savedStateHandle.get<Uri>("currentMediaUri")?.let { uri ->
//            if (hasNecessaryPermission) {
//                viewModelScope.launch {
//                    currentMedia.value = mediaStoreClient.getResourceByUri(uri)
//                }
//            }
//        }
//    }

    init {
        viewModelScope.launch {
            currentMedia.filterNotNull().collect {
                sendFileMessage(it)
            }
        }
    }

    fun hasNecessaryPermission(allMatch: Boolean) {
        viewModelScope.launch {
            _hasNecessaryPermission.emit(allMatch)
        }
    }

    fun setCurrentMedia(uri: Uri) {
        viewModelScope.launch {
            mediaStoreClient.getResourceByUri(uri)?.let {
                savedStateHandle.set("currentMediaUri", uri)
                currentMedia.value = mediaStoreClient.getResourceByUri(uri)
            }
        }
    }

    val temporaryCameraImageUri: Uri?
        get() = savedStateHandle.get("temporaryCameraImageUri")

    fun saveTemporaryCameraImageUri(uri: Uri) {
        savedStateHandle.set("temporaryCameraImageUri", uri)
    }

    fun clearTemporaryCameraImageUri() {
        savedStateHandle.remove<Uri>("temporaryCameraImageUri")
    }

    fun createMediaUriForCamera(type: MediaType, callback: (uri: Uri) -> Unit) {
        viewModelScope.launch {
            val uri = when (type) {
                MediaType.IMAGE -> mediaStoreClient.createImageUri(
                    generateFilename(Attachments.Camera, "jpg"),
                    SharedPrimary
                )
                MediaType.VIDEO -> mediaStoreClient.createVideoUri(
                    generateFilename(Attachments.Camera, "mp4"),
                    SharedPrimary
                )
            }

            withContext(Dispatchers.Main) {
                if (uri != null) callback(uri)
            }
        }
    }

    enum class MediaType {
        IMAGE, VIDEO
    }

    private fun generateFilename(source: Attachments, extension: String): String {
        return when (source) {
            Attachments.Camera -> "camera-${System.currentTimeMillis()}.$extension"
            Attachments.Gallery -> "gallery-${System.currentTimeMillis()}.$extension"
        }
    }

}
