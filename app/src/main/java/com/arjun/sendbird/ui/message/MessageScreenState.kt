package com.arjun.sendbird.ui.message

import com.sendbird.android.BaseMessage
import com.sendbird.android.GroupChannel

data class MessageScreenState(
    val loading: Boolean = false,
    val toolBarState: ToolBarState = ToolBarState(),
    val messages: List<BaseMessage> = emptyList(),
)

data class ToolBarState(
    val channel: GroupChannel? = null,
    val isOnline: Boolean = false,
    val showTypingStatus: Boolean = false
)