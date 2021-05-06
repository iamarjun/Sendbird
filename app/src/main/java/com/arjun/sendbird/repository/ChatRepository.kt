package com.arjun.sendbird.repository

import com.arjun.sendbird.model.ChannelState
import com.sendbird.android.BaseMessage
import com.sendbird.android.GroupChannel
import kotlinx.coroutines.flow.Flow
import java.util.*

interface ChatRepository {

    suspend fun getChannel(channelUrl: String): GroupChannel

    suspend fun loadChannels(): List<GroupChannel>

    fun observeChannels(): Flow<ChannelState>

    suspend fun loadMessages(channelUrl: String, createdAt: Long? = null): List<BaseMessage>

    suspend fun sendMessage(channelUrl: String, message: String): BaseMessage

    suspend fun sendFileMessage(channelUrl: String, fileInfo: Hashtable<String, Any?>): BaseMessage

    suspend fun sendTypingStatus(channelUrl: String, isTyping: Flow<Boolean>)

    suspend fun markMessagesAsRead(channelUrl: String)

    fun observeUserOnlinePresence(userId: String?): Flow<Boolean>
}
