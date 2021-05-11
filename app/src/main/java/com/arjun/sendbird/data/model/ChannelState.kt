package com.arjun.sendbird.data.model

import com.sendbird.android.BaseChannel
import com.sendbird.android.BaseMessage
import com.sendbird.android.User

sealed class ChannelState {
    object Init : ChannelState()
    data class MessageAdded(val message: BaseMessage) : ChannelState()
    data class MessageUpdated(val messageId: Long) : ChannelState()
    data class MessageDeleted(val messageId: Long) : ChannelState()
    object ReadReceiptUpdated : ChannelState()
    object DeliveryReceiptUpdated : ChannelState()
    data class TypingStatusUpdated(val typingUsers: List<User>) : ChannelState()
    data class ChannelUpdated(val channel: BaseChannel) : ChannelState()
}
