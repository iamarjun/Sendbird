package com.arjun.sendbird.ui.message

import com.sendbird.android.BaseMessage
import com.sendbird.android.GroupChannel

data class MessageScreenState(
    val channel: GroupChannel? = null,
    val messages: List<BaseMessage> = emptyList(),
)

data class ToolBarState(
    val loading: Boolean = false,
    val channel: GroupChannel? = null,
    val isOnline: Boolean = false,
    val showTypingStatus: Boolean = false
)