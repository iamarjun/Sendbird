package com.arjun.sendbird.ui.message.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.arjun.sendbird.R
import com.arjun.sendbird.util.isMe
import com.arjun.sendbird.util.widthDp
import com.google.accompanist.glide.rememberGlidePainter
import com.sendbird.android.FileMessage

@Composable
fun FileMessageCard(
    message: FileMessage
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
                .widthIn(max = 200.dp),
            backgroundColor = when (message.isMe()) {
                true -> colorResource(id = R.color.purple_50)
                false -> colorResource(id = R.color.teal_50)
            }
        ) {
            Column(
                modifier = Modifier.padding(6.dp)
            ) {

                Image(
                    painter = rememberGlidePainter(
                        request = message.url
                    ),
                    contentDescription = "Image Message",
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = roundness,
                                topEnd = roundness,
                                bottomStart = if (message.isMe()) roundness else 0.dp,
                                bottomEnd = if (message.isMe()) 0.dp else roundness
                            )
                        )
                        .size(
                            LocalContext.current
                                .widthDp()
                                .times(0.5).dp
                        ),
                    contentScale = ContentScale.Crop,
                )

                Text(
                    text = message.message,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        )
                )
            }

        }
    }
}