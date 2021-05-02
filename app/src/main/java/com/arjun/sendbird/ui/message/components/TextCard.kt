package com.arjun.sendbird.ui.message.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.arjun.sendbird.R.color
import com.arjun.sendbird.util.isMe
import com.arjun.sendbird.util.widthDp
import com.sendbird.android.BaseMessage

@Composable
fun TextCard(
    message: BaseMessage,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = when (message.isMe()) {
            true -> Alignment.End
            false -> Alignment.Start
        }
    ) {
        Card(
            shape = RoundedCornerShape(
                topStartPercent = 25,
                topEndPercent = 25,
                bottomStartPercent = if (message.isMe()) 25 else 0,
                bottomEndPercent = if (message.isMe()) 0 else 25
            ),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .widthIn(
                    max = LocalContext.current
                        .widthDp()
                        .times(0.8).dp
                ),
            backgroundColor = when (message.isMe()) {
                true -> colorResource(id = color.purple_50)
                false -> colorResource(id = color.teal_50)
            }
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

        }
    }
}


