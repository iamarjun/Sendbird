package com.arjun.sendbird.data.dataSource.channel

import com.arjun.sendbird.model.ChannelState
import com.sendbird.android.GroupChannel
import kotlinx.coroutines.flow.Flow

interface ChannelDataSource {

    suspend fun loadChannels(): List<GroupChannel>

    fun observeChannels(): Flow<ChannelState>
}