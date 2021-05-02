package com.arjun.sendbird.ui.message

import androidx.lifecycle.*
import com.arjun.sendbird.repository.ChatRepository
import com.arjun.sendbird.util.getHumanReadableDate
import com.sendbird.android.BaseMessage
import com.sendbird.android.GroupChannel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val repository: ChatRepository,
) : ViewModel() {

    val channelState = repository.observeChannels()

    private val _messages by lazy { MutableLiveData<List<BaseMessage>>() }
    private val messages: LiveData<List<BaseMessage>>
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
}