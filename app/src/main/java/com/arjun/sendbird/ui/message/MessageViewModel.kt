package com.arjun.sendbird.ui.message

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjun.sendbird.repository.ChatRepository
import com.sendbird.android.BaseMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
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