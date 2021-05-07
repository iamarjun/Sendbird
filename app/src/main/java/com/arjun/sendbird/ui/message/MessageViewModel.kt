package com.arjun.sendbird.ui.message

import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.arjun.sendbird.dataSource.MessagePagingSource
import com.arjun.sendbird.repository.ChatRepository
import com.sendbird.android.BaseMessage
import com.sendbird.android.GroupChannel
import com.sendbird.android.SendBird
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MessageViewModel @Inject constructor(
    private val repository: ChatRepository,
) : ViewModel() {

    private val _messages = MutableStateFlow<PagingData<BaseMessage>>(PagingData.empty())
    val messages = _messages.asStateFlow()

    fun getMessages(channel: GroupChannel): Flow<PagingData<BaseMessage>> {
        return Pager(
            config = PagingConfig(
                pageSize = CHANNEL_MESSAGE_LIMIT,
                enablePlaceholders = true
            ),
            pagingSourceFactory = { MessagePagingSource(channel) }
        ).flow
    }

    val channelState = repository.observeChannels()

//    private val _messages by lazy { MutableLiveData<List<BaseMessage>>() }
//    private val messages: LiveData<List<BaseMessage>>
//        get() = _messages
//
//    val groupedMessages = messages.map {
//        it.groupBy { it.getHumanReadableDate() }
//    }
//
//    fun loadMessages(channelUrl: String) {
//        viewModelScope.launch {
//            val messages = repository.loadMessages(channelUrl = channelUrl)
//            _messages.value = messages
//        }
//    }

    private val _channel by lazy { MutableLiveData<GroupChannel>() }
    val channel: LiveData<GroupChannel>
        get() = _channel

    fun getChannel(channelUrl: String) {
        viewModelScope.launch {
            val channel = repository.getChannel(channelUrl = channelUrl)
            _channel.value = channel
            _messages.emitAll(Pager(
                config = PagingConfig(
                    pageSize = CHANNEL_MESSAGE_LIMIT,
                    enablePlaceholders = true
                ),
                pagingSourceFactory = { MessagePagingSource(channel) }
            ).flow)
        }
    }

    val isOnline = _channel.asFlow().flatMapLatest { channel ->
        repository.observeUserOnlinePresence(channel?.members?.find { it.userId != SendBird.getCurrentUser().userId }?.userId)
    }

    fun addMessage(message: BaseMessage) {
//        val m = messages.value?.toMutableList()
//        m?.add(0, message)
//        _messages.value = (m)
    }

    fun sendMessage(channelUrl: String, message: String, onMessageSend: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.sendMessage(channelUrl, message)
                onMessageSend()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun sendFileMessage(channelUrl: String, fileInfo: Hashtable<String, Any?>) {
        viewModelScope.launch {
            try {
                val msg = repository.sendFileMessage(channelUrl, fileInfo)
                addMessage(msg)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private val _showTypingStatus by lazy { MutableLiveData(false) }
    val showTypingStatus: LiveData<Boolean>
        get() = _showTypingStatus

    fun showTypingIndicator(showTypingStatus: Boolean) {
        _showTypingStatus.value = showTypingStatus
    }

    fun typingStatus(channelUrl: String, isTyping: Flow<Boolean>) {
        viewModelScope.launch {
            repository.sendTypingStatus(
                channelUrl = channelUrl,
                isTyping = isTyping
            )
        }
    }

    companion object {
        private const val CHANNEL_MESSAGE_LIMIT = 30
    }
}