package com.arjun.sendbird.ui.message

import androidx.lifecycle.*
import com.arjun.sendbird.repository.ChatRepository
import com.arjun.sendbird.util.getHumanReadableDate
import com.sendbird.android.BaseMessage
import com.sendbird.android.GroupChannel
import com.sendbird.android.SendBird
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@ExperimentalCoroutinesApi
@HiltViewModel
class MessageViewModel @Inject constructor(
    private val repository: ChatRepository,
) : ViewModel() {

    val channelState = repository.observeChannels()

    private val _messages by lazy { MutableLiveData<List<BaseMessage>>() }
    val messages: LiveData<List<BaseMessage>>
        get() = _messages

    val groupedMessages = messages.map {
        it.groupBy { it.getHumanReadableDate() }
    }

    fun loadMessages(channelUrl: String) {
        viewModelScope.launch {
            val messages = repository.loadMessages(channelUrl = channelUrl)
            _messages.value = messages
        }
    }

    private val _channel by lazy { MutableLiveData<GroupChannel>() }
    val channel: LiveData<GroupChannel>
        get() = _channel

    fun getChannel(channelUrl: String) {
        viewModelScope.launch {
            val channel = repository.getChannel(channelUrl = channelUrl)
            _channel.value = channel
        }
    }

    val isOnline = _channel.asFlow().flatMapLatest { channel ->
        repository.observeUserOnlinePresence(channel?.members?.find { it.userId != SendBird.getCurrentUser().userId }?.userId)
    }

    fun addMessage(message: BaseMessage) {
        val m = messages.value?.toMutableList()
        m?.add(0, message)
        _messages.value = (m)
    }

    fun sendMessage(channelUrl: String, message: String) {
        viewModelScope.launch {
            try {
                val msg = repository.sendMessage(channelUrl, message)
                addMessage(msg)
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

    private val _page by lazy { MutableLiveData(1) }
    val page: LiveData<Int>
        get() = _page

    private var scrollPosition = 0

    private fun incrementPage() {
        _page.value = _page.value?.plus(1)
    }

    fun onChangeScrollPosition(position: Int) {
        scrollPosition = position
    }

    fun nextPage(channelUrl: String, createdAt: Long) {
        viewModelScope.launch {
            if (scrollPosition + 1 >= _page.value ?: 0 * PAGE_SIZE) {
                incrementPage()
            }

            if (_page.value ?: 0 > 1) {
                val messages = repository.loadMessages(channelUrl, createdAt)
                appendNewMessages(messages)
            }
        }
    }

    private fun appendNewMessages(messages: List<BaseMessage>) {
        val currentList = ArrayList(_messages.value)
        currentList.addAll(messages)
        _messages.value = currentList
    }

    companion object {
        const val PAGE_SIZE = 30
    }
}