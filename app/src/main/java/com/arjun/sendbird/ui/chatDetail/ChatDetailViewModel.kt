package com.arjun.sendbird.ui.chatDetail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjun.sendbird.repository.ChatRepository
import com.sendbird.android.BaseMessage
import com.sendbird.android.GroupChannel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val repository: ChatRepository,
) : ViewModel() {

    private val _messages by lazy { mutableStateOf<List<BaseMessage>?>(null) }
    val messages: State<List<BaseMessage>?>
        get() = _messages

    fun loadMessages(channelUrl: String) {
        viewModelScope.launch {
            val messages = repository.loadMessages(channelUrl = channelUrl)
            _messages.value = messages
        }
    }

    fun add(value: String) {
//       _messages.value = messages.value?.toMutableList()?.apply {
//            add(value)
//        }
    }


}