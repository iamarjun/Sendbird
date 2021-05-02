package com.arjun.sendbird.dataSource

import com.sendbird.android.BaseChannel
import com.sendbird.android.BaseChannel.*
import com.sendbird.android.BaseMessage
import com.sendbird.android.FileMessage.ThumbnailSize
import com.sendbird.android.FileMessageParams
import com.sendbird.android.GroupChannel
import com.sendbird.android.GroupChannel.GroupChannelGetHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MessageDataSource @Inject constructor() {

    suspend fun getChannel(channelUrl: String): GroupChannel {
        return suspendCancellableCoroutine { continuation ->

            val groupChannelGetHandler = GroupChannelGetHandler { groupChannel, error ->

                if (error != null) {
                    Timber.e(error)
                    continuation.resumeWithException(error)
                } else {
                    continuation.resume(groupChannel)
                }
            }

            GroupChannel.getChannel(channelUrl, groupChannelGetHandler)
        }
    }

    suspend fun sendMessage(channelUrl: String, message: String): BaseMessage {
        val channel = getChannel(channelUrl)

        return suspendCancellableCoroutine { continuation ->
            val messageHandler = SendUserMessageHandler { message, error ->
                if (error != null) {
                    Timber.e(error)
                }
                continuation.resume(message)
            }
            channel.sendUserMessage(message, messageHandler)
        }
    }

    suspend fun sendFileMessage(
        channelUrl: String,
        fileInfo: Hashtable<String, Any?>
    ): BaseMessage {
        val channel = getChannel(channelUrl)

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
            .setFileUrl(path)
            .setMimeType(mime)
            .setThumbnailSizes(thumbnailSizes)

        return suspendCancellableCoroutine { continuation ->
            val messageHandler = SendFileMessageHandler { fileMessage, error ->
                if (error != null) {
                    Timber.e(error)
                }
                continuation.resume(fileMessage)
            }
            channel.sendFileMessage(params, messageHandler)
        }
    }

    suspend fun loadMessages(channelUrl: String, createdAt: Long?): List<BaseMessage> {

        val channel = getChannel(channelUrl)

        return suspendCancellableCoroutine { continuation ->

            val messageHandler = GetMessagesHandler { messages, error ->
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
    }

    suspend fun sendTypingStatus(channelUrl: String, isTyping: Boolean) {
        val channel = getChannel(channelUrl)

        if (isTyping) {
            channel.startTyping()
        } else {
            channel.endTyping()
        }
    }

    suspend fun markMessagesAsRead(channelUrl: String) = getChannel(channelUrl).markAsRead()

    suspend fun getUnreadMemberCount(
        channelUrl: String,
        message: BaseMessage
    ) = getChannel(channelUrl).getUnreadMemberCount(message)

    suspend fun getUndeliveredMemberCount(
        channelUrl: String,
        message: BaseMessage
    ) = getChannel(channelUrl).getUndeliveredMemberCount(message)

    companion object {
        private const val CHANNEL_MESSAGE_LIMIT = 30
    }
}
