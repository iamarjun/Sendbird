package com.arjun.sendbird.dataSource

import android.content.ContentResolver
import android.net.Uri
import com.sendbird.android.BaseChannel
import com.sendbird.android.BaseChannel.GetMessagesHandler
import com.sendbird.android.BaseChannel.SendUserMessageHandler
import com.sendbird.android.BaseMessage
import com.sendbird.android.GroupChannel
import com.sendbird.android.GroupChannel.GroupChannelGetHandler
import com.sendbird.android.UserMessageParams
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MessageDataSource @Inject constructor(
    private val contentResolver: ContentResolver,
) {

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
        val params = UserMessageParams()
            .setMessage(message)
            .setCustomType("text")


        return suspendCancellableCoroutine { continuation ->
            val messageHandler = SendUserMessageHandler { message, error ->
                if (error != null) {
                    Timber.e(error)
                }
                continuation.resume(message)
            }
            channel.sendUserMessage(params, messageHandler)
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

    companion object {
        private const val CHANNEL_MESSAGE_LIMIT = 30
    }
}
