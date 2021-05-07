package com.arjun.sendbird.dataSource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bumptech.glide.load.HttpException
import com.sendbird.android.BaseChannel
import com.sendbird.android.BaseMessage
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MessagePagingSource(
    private val channel: BaseChannel
) : PagingSource<Long, BaseMessage>() {

    override fun getRefreshKey(state: PagingState<Long, BaseMessage>): Long? {
        return SENDBIRD_INITIAL_CREATED_AT
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, BaseMessage> {
        val createdAt = params.key ?: SENDBIRD_INITIAL_CREATED_AT

        return try {

            val messages = loadMessages(channel, createdAt, params.loadSize)
            val nextKey = if (messages.isEmpty()) null else messages.last().createdAt

            LoadResult.Page(
                data = messages,
                nextKey = nextKey,
                prevKey = null
            )

        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }


    private suspend fun loadMessages(
        channel: BaseChannel,
        createdAt: Long,
        loadSize: Int
    ): List<BaseMessage> {

        return suspendCancellableCoroutine { continuation ->

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
                loadSize,
                true,
                BaseChannel.MessageTypeFilter.ALL,
                null,
                null,
                true,
                messageHandler
            )
        }
    }

    companion object {
        private const val SENDBIRD_INITIAL_CREATED_AT = Long.MAX_VALUE
    }

}