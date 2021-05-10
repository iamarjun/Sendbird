package com.arjun.sendbird.data.dataSource.messages

import com.arjun.sendbird.util.PAGE_SIZE
import com.sendbird.android.BaseChannel
import com.sendbird.android.BaseMessage
import com.sendbird.android.FileMessage.ThumbnailSize
import com.sendbird.android.FileMessageParams
import com.sendbird.android.GroupChannel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MessageDataSourceImp @Inject constructor() : MessageDataSource {
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

    @ExperimentalCoroutinesApi
    override fun loadMessages(
        channel: GroupChannel,
        currentScrollPosition: Flow<Int>,
        pageNo: Flow<Int>,
    ): Flow<List<BaseMessage>> = combine(
        currentScrollPosition,
        pageNo
    ) { position, page ->

        if (position == 0 && page == 1) {
            val messages = loadMessages(channel = channel, createdAt = Long.MAX_VALUE)
            localMessages.addAll(messages)
        }
        //TODO: Improve pagination logic
        if (position + 1 >= page * PAGE_SIZE) {
            val createdAt = localMessages.last().createdAt
            val messages = loadMessages(channel = channel, createdAt = createdAt)
            localMessages.addAll(messages)
        }

        localMessages
    }

    private suspend fun loadMessages(channel: GroupChannel, createdAt: Long): List<BaseMessage> =
        suspendCancellableCoroutine { continuation ->

            val messageHandler = BaseChannel.GetMessagesHandler { messages, error ->
                if (error != null) {
                    Timber.e(error)
                    continuation.resumeWithException(error)
                } else {
                    continuation.resume(messages)
                }
            }

            channel.getPreviousMessagesByTimestamp(
                createdAt,
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
