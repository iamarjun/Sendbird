//package com.arjun.sendbird.repository
//
//import com.arjun.sendbird.data.dataSource.channel.ChannelDataSourceImp
//import com.arjun.sendbird.data.dataSource.messages.MessageDataSourceImp
//import com.arjun.sendbird.data.dataSource.user.UserDataSourceImp
//import com.arjun.sendbird.data.model.ChannelState
//import com.sendbird.android.BaseMessage
//import com.sendbird.android.GroupChannel
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.Flow
//import java.util.*
//import javax.inject.Inject
//
//class ChatRepositoryImpl @Inject constructor(
//    private val channelDataSourceImp: ChannelDataSourceImp,
//    private val messageDataSourceImp: MessageDataSourceImp,
//    private val userDataSourceImp: UserDataSourceImp
//) : ChatRepository {
//
//    override suspend fun getChannel(channelUrl: String): GroupChannel =
//        channelDataSourceImp.getChannel(channelUrl)
//
//    override suspend fun loadChannels(): List<GroupChannel> =
//        channelDataSourceImp.loadChannels()
//
//    @ExperimentalCoroutinesApi
//    override fun observeChannels(): Flow<ChannelState> =
//        channelDataSourceImp.observeChannels()
//
//    override suspend fun sendMessage(channelUrl: String, message: String): BaseMessage =
//        messageDataSourceImp.sendMessage(channelUrl, message)
//
//    override suspend fun sendFileMessage(
//        channelUrl: String,
//        fileInfo: Hashtable<String, Any?>
//    ): BaseMessage =
//        messageDataSourceImp.sendFileMessage(channelUrl, fileInfo)
//
//    override suspend fun loadMessages(
//        channelUrl: String,
//        createdAt: Long?
//    ): List<BaseMessage> =
//        messageDataSourceImp.loadMessages(channelUrl, createdAt)
//
//    override suspend fun sendTypingStatus(channelUrl: String, isTyping: Flow<Boolean>) {
//        messageDataSourceImp.sendTypingStatus(channelUrl, isTyping)
//    }
//
//    override suspend fun markMessagesAsRead(channelUrl: String) {
//        messageDataSourceImp.markMessagesAsRead(channelUrl)
//    }
//
//    override fun observeUserOnlinePresence(userId: String?): Flow<Boolean> =
//        userDataSourceImp.observeUserOnlinePresence(userId)
//}
