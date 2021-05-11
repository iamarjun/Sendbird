package com.arjun.sendbird.data.dataSource.channel

import com.arjun.sendbird.model.ChannelState
import com.sendbird.android.GroupChannel
import kotlinx.coroutines.flow.Flow

interface ChannelDataSource {

    val channel: Flow<GroupChannel?>

    val channels: Flow<List<GroupChannel>>

    suspend fun loadChannels()

    suspend fun getChannel(channelUrl: String)

    val channelState: Flow<ChannelState>
}