package com.arjun.sendbird.data.dataSource.messages

import com.sendbird.android.BaseMessage
import com.sendbird.android.GroupChannel
import kotlinx.coroutines.flow.Flow
import java.util.*

interface MessageDataSource {

    suspend fun sendMessage(channel: GroupChannel, message: String): BaseMessage

    suspend fun sendFileMessage(
        channel: GroupChannel,
        fileInfo: Hashtable<String, Any?>
    ): BaseMessage

    fun loadMessages(
        channel: GroupChannel,
        currentScrollPosition: Flow<Int>,
        pageNo: Flow<Int>,
    ): Flow<List<BaseMessage>>

    suspend fun sendTypingStatus(channel: GroupChannel, isTyping: Flow<Boolean>)

    suspend fun markMessagesAsRead(channel: GroupChannel)

    suspend fun getUnreadMemberCount(
        channel: GroupChannel,
        message: BaseMessage
    ): Int

    suspend fun getUndeliveredMemberCount(
        channel: GroupChannel,
        message: BaseMessage
    ): Int
}