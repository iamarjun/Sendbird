package com.arjun.sendbird.data.dataSource.messages

import com.sendbird.android.BaseChannel
import com.sendbird.android.BaseMessage
import com.sendbird.android.FileMessage.ThumbnailSize
import com.sendbird.android.FileMessageParams
import com.sendbird.android.GroupChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MessageDataSourceImp @Inject constructor() : MessageDataSource {

    private val _messages = MutableStateFlow(emptyList<BaseMessage>())
    override val messages: Flow<List<BaseMessage>> = _messages.asStateFlow()

    override val lastMessage: BaseMessage
        get() = localMessages.last()

    private val localMessages = mutableListOf<BaseMessage>()

    override suspend fun sendMessage(channel: GroupChannel, message: String): BaseMessage {
        return suspendCancellableCoroutine { continuation ->
            val messageHandler = BaseChannel.SendUserMessageHandler { message, error ->
                if (error != null) {
                    Timber.e(error)
                }
                continuation.resume(message)
            }
            channel.sendUserMessage(message, messageHandler)
        }
    }

    override suspend fun sendFileMessage(
        channel: GroupChannel,
        fileInfo: Hashtable<String, Any?>
    ): BaseMessage {

        // Specify two dimensions of thumbnails to generate
        val thumbnailSizes: MutableList<ThumbnailSize> = ArrayList()
        thumbnailSizes.add(ThumbnailSize(240, 240))
        thumbnailSizes.add(ThumbnailSize(320, 320))

        val name: String = if (fileInfo.containsKey("name"))
            fileInfo["name"] as String else {
            "Sendbird File"
        }
        val path = fileInfo["path"] as String
        val file = File(path)
        val mime = fileInfo["mime"] as String
        val size = fileInfo["size"] as Int

        val params = FileMessageParams().setFile(file)
            .setFileName(name)
            .setFileSize(size)
            .setMimeType(mime)
            .setThumbnailSizes(thumbnailSizes)

        return suspendCancellableCoroutine { continuation ->
            val messageHandler = BaseChannel.SendFileMessageHandler { fileMessage, error ->
                if (error != null) {
                    Timber.e(error)
                }
                continuation.resume(fileMessage)
            }
            channel.sendFileMessage(params, messageHandler)
        }
    }


    override suspend fun loadMessages(channel: GroupChannel, createdAt: Long?) {

        //Case when activity is recreated and data was already fetched
        if (localMessages.isNotEmpty() && createdAt == null)
            return

        val messages: List<BaseMessage> = suspendCancellableCoroutine { continuation ->

            val messageHandler = BaseChannel.GetMessagesHandler { messages, error ->
                if (error != null) {
                    Timber.e(error)
                    continuation.resumeWithException(error)
                } else {
                    continuation.resume(messages)
                }
            }

            channel.getPreviousMessagesByTimestamp(
                createdAt ?: Long.MAX_VALUE,
                false,
                CHANNEL_MESSAGE_LIMIT,
                true,
                BaseChannel.MessageTypeFilter.ALL,
                null,
                null,
                true,
                messageHandler
            )
        }

        localMessages.addAll(messages)

        _messages.emit(localMessages)
    }


    override suspend fun sendTypingStatus(channel: GroupChannel, isTyping: Flow<Boolean>) {
        isTyping.collect {
            if (it) {
                channel.startTyping()
            } else {
                channel.endTyping()
            }
        }
    }

    override suspend fun markMessagesAsRead(channel: GroupChannel) = channel.markAsRead()

    override suspend fun getUnreadMemberCount(
        channel: GroupChannel,
        message: BaseMessage
    ) = channel.getUnreadMemberCount(message)

    override suspend fun getUndeliveredMemberCount(
        channel: GroupChannel,
        message: BaseMessage
    ) = channel.getUndeliveredMemberCount(message)

    companion object {
        private const val CHANNEL_MESSAGE_LIMIT = 30
    }
}
