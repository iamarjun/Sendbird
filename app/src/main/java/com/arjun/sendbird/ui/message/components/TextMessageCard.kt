package com.arjun.sendbird.ui.message.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arjun.sendbird.R
import com.arjun.sendbird.util.isMe
import com.arjun.sendbird.util.timeStamp
import com.arjun.sendbird.util.widthDp
import com.sendbird.android.BaseMessage
import com.sendbird.android.GroupChannel

@ExperimentalAnimationApi
@Composable
fun TextMessageCard(
    message: BaseMessage,
    channel: GroupChannel?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = when (message.isMe()) {
            true -> Alignment.End
            false -> Alignment.Start
        }
    ) {

        val roundness = 15.dp

        Card(
            shape = RoundedCornerShape(
                topStart = roundness,
                topEnd = roundness,
                bottomStart = if (message.isMe()) roundness else 0.dp,
                bottomEnd = if (message.isMe()) 0.dp else roundness
            ),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .widthIn(
                    max = LocalContext.current
                        .widthDp()
                        .times(0.8).dp
                ),
            backgroundColor = when (message.isMe()) {
                true -> colorResource(id = R.color.purple_50)
                false -> colorResource(id = R.color.teal_50)
            }
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = message.timeStamp(),
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    MessageStatus(message = message, channel = channel)
                }
            }

        }
    }
}



