package com.arjun.sendbird.data.dataSource.channel

import com.arjun.sendbird.data.model.ChannelState
import com.sendbird.android.GroupChannel
import kotlinx.coroutines.flow.Flow

interface ChannelDataSource {

    val channel: Flow<GroupChannel?>

    fun loadChannels(): Flow<List<GroupChannel>>

    suspend fun getChannel(channelUrl: String)

    val channelState: Flow<ChannelState>
}