package com.arjun.sendbird.data.dataSource.messages

import android.content.ContentResolver
import android.net.Uri
import com.google.modernstorage.mediastore.MediaResource
import com.sendbird.android.BaseChannel
import com.sendbird.android.BaseMessage
import com.sendbird.android.FileMessage.ThumbnailSize
import com.sendbird.android.FileMessageParams
import com.sendbird.android.GroupChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MessageDataSourceImp @Inject constructor(
    private val contentResolver: ContentResolver
) : MessageDataSource {

    private val _messages = MutableStateFlow(emptyList<BaseMessage>())
    override val messages: Flow<List<BaseMessage>> = _messages.asStateFlow()

    override val lastMessage: BaseMessage
        get() = localMessages.last()

    private val localMessages = mutableListOf<BaseMessage>()

    /**
     * Compose doesn't recompose on same reference of the list
     * see: https://stackoverflow.com/questions/66448722/jetpack-compose-lazycolumn-not-recomposing
     */
    override suspend fun addMessage(message: BaseMessage) {
        val msg = localMessages.toMutableList()
        msg.add(0, message)
        _messages.emit(msg)
    }

    override suspend fun sendMessage(channel: GroupChannel, message: String) {
        val textMessage: BaseMessage = suspendCancellableCoroutine { continuation ->
            val messageHandler = BaseChannel.SendUserMessageHandler { message, error ->
                if (error != null) {
                    Timber.e(error)
                }
                continuation.resume(message)
            }
            channel.sendUserMessage(message, messageHandler)
        }
        addMessage(message = textMessage)
    }

    override suspend fun sendFileMessage(
        channel: GroupChannel,
        mediaResource: MediaResource
    ) {

        // Specify two dimensions of thumbnails to generate
        val thumbnailSizes: MutableList<ThumbnailSize> = ArrayList()
        thumbnailSizes.add(ThumbnailSize(240, 240))
        thumbnailSizes.add(ThumbnailSize(320, 320))

        val name = mediaResource.filename
        val file = copyStreamToFile(mediaResource.uri)
        val mime = mediaResource.mimeType
        val size = mediaResource.size

        val params = FileMessageParams().setFile(file)
            .setFileName(name)
            .setFileSize(size.toInt())
            .setMimeType(mime)
            .setThumbnailSizes(thumbnailSizes)

        val message: BaseMessage = suspendCancellableCoroutine { continuation ->
            val messageHandler = BaseChannel.SendFileMessageHandler { fileMessage, error ->
                if (error != null) {
                    Timber.e(error)
                }
                continuation.resume(fileMessage)
            }
            channel.sendFileMessage(params, messageHandler)
        }

        addMessage(message = message)
    }

    /**
     * Creates a temporary file from a Uri, preparing it for upload.
     */
    private fun copyStreamToFile(uri: Uri): File {
        val outputFile = File.createTempFile("temp", null)

        contentResolver.openInputStream(uri)?.use { input ->
            val outputStream = FileOutputStream(outputFile)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // buffer size
                while (true) {
                    val byteCount = input.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
            }
        }
        return outputFile
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
        /**
         * Compose doesn't recompose on same reference of the list
         * see: https://stackoverflow.com/questions/66448722/jetpack-compose-lazycolumn-not-recomposing
         */
        _messages.emit(localMessages.toMutableList())
    }


    override suspend fun sendTypingStatus(channel: GroupChannel, isTyping: Boolean) {
        if (isTyping) {
            channel.startTyping()
        } else {
            channel.endTyping()
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
