package com.arjun.sendbird.data.dataSource.channel


import com.arjun.sendbird.data.model.ChannelState
import com.sendbird.android.*
import com.sendbird.android.SendBird.ChannelHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ChannelDataSourceImp @Inject constructor() : ChannelDataSource {

    private val _channel = MutableStateFlow<GroupChannel?>(null)
    override val channel: Flow<GroupChannel?> = _channel.asStateFlow()

    override suspend fun getChannel(channelUrl: String) {
        val channel: GroupChannel = suspendCancellableCoroutine { continuation ->

            val groupChannelGetHandler =
                GroupChannel.GroupChannelGetHandler { groupChannel, error ->

                    if (error != null) {
                        Timber.e(error)
                        continuation.resumeWithException(error)
                    } else {
                        continuation.resume(groupChannel)
                    }
                }

            GroupChannel.getChannel(channelUrl, groupChannelGetHandler)
        }

        _channel.emit(channel)
    }

    override fun loadChannels(): Flow<List<GroupChannel>> = flow {
        val channels: List<GroupChannel> = suspendCancellableCoroutine { continuation ->

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

        emit(channels)
    }

    @ExperimentalCoroutinesApi
    override val channelState: Flow<ChannelState>
        get() = callbackFlow {
            this.trySend(ChannelState.Init).isSuccess

            val channelHandler = object : ChannelHandler() {
                override fun onMessageReceived(baseChannel: BaseChannel, message: BaseMessage) {
                    this@callbackFlow.trySend(ChannelState.MessageAdded(message = message)).isSuccess
                }

                override fun onMessageUpdated(channel: BaseChannel?, message: BaseMessage?) {
                    this@callbackFlow.trySend(
                        ChannelState.MessageUpdated(
                            messageId = message?.messageId ?: 0L
                        )
                    ).isSuccess
                }

                override fun onMessageDeleted(channel: BaseChannel?, msgId: Long) {
                    this@callbackFlow.trySend(ChannelState.MessageDeleted(messageId = msgId)).isSuccess
                }

                override fun onChannelChanged(channel: BaseChannel) {
                    this@callbackFlow.trySend(ChannelState.ChannelUpdated(channel)).isSuccess
                }

                override fun onReadReceiptUpdated(channel: GroupChannel) {
                    this@callbackFlow.trySend(ChannelState.ReadReceiptUpdated).isSuccess
                }

                override fun onDeliveryReceiptUpdated(channel: GroupChannel) {
                    this@callbackFlow.trySend(ChannelState.DeliveryReceiptUpdated).isSuccess
                }

                override fun onTypingStatusUpdated(channel: GroupChannel) {
                    val users = channel.typingUsers
                    this@callbackFlow.trySend(ChannelState.TypingStatusUpdated(users)).isSuccess
                }
            }

            SendBird.addChannelHandler(CHANNEL_HANDLER_ID, channelHandler)

            awaitClose { SendBird.removeChannelHandler(CHANNEL_HANDLER_ID) }
        }


    companion object {
        private const val CHANNEL_LIST_LIMIT = 15
        private const val CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_DETAIL"
    }
}
