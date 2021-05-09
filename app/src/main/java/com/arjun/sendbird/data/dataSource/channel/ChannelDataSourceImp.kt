package com.arjun.sendbird.data.dataSource.channel


import com.arjun.sendbird.model.ChannelState
import com.sendbird.android.*
import com.sendbird.android.SendBird.ChannelHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ChannelDataSourceImp @Inject constructor() : ChannelDataSource {

    override suspend fun loadChannels(): List<GroupChannel> {
        return suspendCancellableCoroutine { continuation ->

            val groupChannelListQueryResultHandler =
                GroupChannelListQuery.GroupChannelListQueryResultHandler { result, error ->
                    if (error != null) {
                        Timber.e(error)
                        continuation.resumeWithException(error)
                    } else {
                        continuation.resume(result)
                    }
                }

            GroupChannel.createMyGroupChannelListQuery().also {
                it.isIncludeEmpty = true
                it.limit = CHANNEL_LIST_LIMIT
                it.next(groupChannelListQueryResultHandler)
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun observeChannels(): Flow<ChannelState> {
        return callbackFlow {
            val channelHandler = object : ChannelHandler() {
                override fun onMessageReceived(baseChannel: BaseChannel, message: BaseMessage) {
                    offer(ChannelState.MessageAdded(message = message))
                }

                override fun onMessageUpdated(channel: BaseChannel?, message: BaseMessage?) {
                    offer(ChannelState.MessageUpdated(messageId = message?.messageId ?: 0L))
                }

                override fun onMessageDeleted(channel: BaseChannel?, msgId: Long) {
                    offer(ChannelState.MessageDeleted(messageId = msgId))
                }

                override fun onChannelChanged(channel: BaseChannel) {
                    offer(ChannelState.ChannelUpdated(channel))
                }

                override fun onReadReceiptUpdated(channel: GroupChannel) {
                    offer(ChannelState.ReadReceiptUpdated)
                }

                override fun onDeliveryReceiptUpdated(channel: GroupChannel) {
                    offer(ChannelState.DeliveryReceiptUpdated)
                }

                override fun onTypingStatusUpdated(channel: GroupChannel) {
                    val users = channel.typingUsers
                    offer(ChannelState.TypingStatusUpdated(users))
                }
            }

            SendBird.addChannelHandler(CHANNEL_HANDLER_ID, channelHandler)

            awaitClose { SendBird.removeChannelHandler(CHANNEL_HANDLER_ID) }
        }
    }

    companion object {
        private const val CHANNEL_LIST_LIMIT = 15
        private const val CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_DETAIL"
    }
}
