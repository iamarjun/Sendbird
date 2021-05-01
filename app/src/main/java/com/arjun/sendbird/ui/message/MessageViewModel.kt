package com.arjun.sendbird.ui.message

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjun.sendbird.repository.ChatRepository
import com.sendbird.android.BaseChannel
import com.sendbird.android.BaseMessage
import com.sendbird.android.GroupChannel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val repository: ChatRepository,
) : ViewModel() {

    val channelState = repository.observeChannels()

    private val _messages by lazy { MutableLiveData<List<BaseMessage>>() }
    val messages: LiveData<List<BaseMessage>>
        get() = _messages

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

            }

        }
    }
}