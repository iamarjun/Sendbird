package com.arjun.sendbird.util

import com.sendbird.android.BaseMessage
import com.sendbird.android.SendBird

fun BaseMessage.isMe() = sender?.userId == SendBird.getCurrentUser().userId
const val ACCESS_TOKEN = "ee6f6cd6410c96a4bdb6343073de5cbf82dbf233"