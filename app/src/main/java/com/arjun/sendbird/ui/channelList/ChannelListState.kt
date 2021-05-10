package com.arjun.sendbird.ui.channelList

import com.sendbird.android.BaseChannel

data class ChannelListState(
    val loading: Boolean = false,
    val channelList: List<BaseChannel> = emptyList(),
)
