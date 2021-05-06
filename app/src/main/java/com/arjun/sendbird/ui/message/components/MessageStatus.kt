package com.arjun.sendbird.ui.message.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.arjun.sendbird.R
import com.arjun.sendbird.util.isMe
import com.sendbird.android.BaseMessage
import com.sendbird.android.GroupChannel

@ExperimentalAnimationApi
@Composable
fun MessageStatus(
    message: BaseMessage,
    channel: GroupChannel?
) {
    AnimatedVisibility(visible = message.isMe()) {
        fun drawMessageStatus(): Int {
            return when (message.sendingStatus) {
                BaseMessage.SendingStatus.CANCELED, BaseMessage.SendingStatus.FAILED -> R.drawable.icon_error_filled
                BaseMessage.SendingStatus.SUCCEEDED -> {
                    val unreadMemberCount = channel?.getUnreadMemberCount(message)
                    val unDeliveredMemberCount =
                        channel?.getUndeliveredMemberCount(message)
                    when {
                        unreadMemberCount == 0 -> R.drawable.icon_read
                        unDeliveredMemberCount == 0 -> R.drawable.icon_delivered
                        else -> R.drawable.icon_sent
                    }
                }
                else -> 0
            }
        }


        val res = drawMessageStatus()

        if (res != 0)
            Icon(
                painter = painterResource(id = res),
                contentDescription = "Message Status",
                modifier = Modifier.size(15.dp)
            )
        else CircularProgressIndicator(modifier = Modifier.size(15.dp))
    }

}