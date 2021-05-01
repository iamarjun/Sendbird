package com.arjun.sendbird.repository

import com.arjun.sendbird.model.ChannelState
import com.sendbird.android.BaseMessage
import com.sendbird.android.GroupChannel
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    suspend fun getChannel(channelUrl: String): GroupChannel

    suspend fun loadChannels(): List<GroupChannel>

    fun observeChannels(handlerId: String): Flow<ChannelState>

    suspend fun loadMessages(channelUrl: String, createdAt: Long? = null): List<BaseMessage>

    suspend fun sendMessage(channelUrl: String, message: String): BaseMessage

    suspend fun sendTypingStatus(channelUrl: String, isTyping: Boolean)

    suspend fun markMessagesAsRead(channelUrl: String)

    fun observeUserOnlinePresence(userId: String): Flow<Boolean>
}
