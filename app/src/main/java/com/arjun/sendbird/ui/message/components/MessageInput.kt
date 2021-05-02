package com.arjun.sendbird.ui.message.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.arjun.sendbird.R

@Composable
fun MessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachmentClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
        ) {
            IconButton(
                onClick = onAttachmentClick,
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "Camera Icon",
                    tint = colorResource(id = R.color.purple_500)
                )
            }

            TextField(
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = KeyboardOptions(
                    autoCorrect = true,
                ),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    backgroundColor = colorResource(id = R.color.light_blue_gray2),
                ),
                placeholder = {
                    Text(text = "Type a message")
                },
                shape = CircleShape
            )

            IconButton(
                onClick = onSendClick,
                enabled = value.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Search Icon",
                    tint = colorResource(id = if (value.isNotEmpty()) R.color.purple_500 else R.color.purple_200)
                )
            }
        }
    }
}