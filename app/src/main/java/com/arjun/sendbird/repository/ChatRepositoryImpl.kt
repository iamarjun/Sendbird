package com.arjun.sendbird.repository

import com.arjun.sendbird.dataSource.ChannelDataSource
import com.arjun.sendbird.dataSource.MessageDataSource
import com.arjun.sendbird.dataSource.UserDataSource
import com.arjun.sendbird.model.ChannelState
import com.sendbird.android.BaseMessage
import com.sendbird.android.GroupChannel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val channelDataSource: ChannelDataSource,
    private val messageDataSource: MessageDataSource,
    private val userDataSource: UserDataSource
) : ChatRepository {

    override suspend fun getChannel(channelUrl: String): GroupChannel =
        messageDataSource.getChannel(channelUrl)

    override suspend fun loadChannels(): List<GroupChannel> =
        channelDataSource.loadChannels()

    @ExperimentalCoroutinesApi
    override fun observeChannels(): Flow<ChannelState> =
        channelDataSource.observeChannels()

    override suspend fun sendMessage(channelUrl: String, message: String): BaseMessage =
        messageDataSource.sendMessage(channelUrl, message)

    override suspend fun loadMessages(
        channelUrl: String,
        createdAt: Long?
    ): List<BaseMessage> =
        messageDataSource.loadMessages(channelUrl, createdAt)

    override suspend fun sendTypingStatus(channelUrl: String, isTyping: Boolean) {
        messageDataSource.sendTypingStatus(channelUrl, isTyping)
    }

    override suspend fun markMessagesAsRead(channelUrl: String) {
        messageDataSource.markMessagesAsRead(channelUrl)
    }

    override fun observeUserOnlinePresence(userId: String): Flow<Boolean> =
        userDataSource.observeUserOnlinePresence(userId)
}
