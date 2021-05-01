package com.arjun.sendbird.ui.chatDetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.arjun.sendbird.MainViewModel
import com.arjun.sendbird.R
import com.arjun.sendbird.ui.base.BaseFragment
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import com.google.accompanist.insets.navigationBarsWithImePadding

@ExperimentalAnimatedInsets
class ChatDetailFragment : BaseFragment() {

    private val args by navArgs<ChatDetailFragmentArgs>()

    private val viewModel by viewModels<ChatDetailViewModel>()
    private val mainViewModel by activityViewModels<MainViewModel>()

    @Composable
    override fun ToolBar() = TopAppBar {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "Sendbird Chat App")
    }

    @Composable
    override fun MainContent(paddingValues: PaddingValues, scaffoldState: ScaffoldState) {

        viewModel.loadMessages(args.channelUrl)

        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
        ) {
            val (lazyColumn, textField) = createRefs()

            val messages = viewModel.messages.value

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(lazyColumn) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(textField.top)
                        height = Dimension.fillToConstraints
                    },
                reverseLayout = true
            ) {

                messages?.let {
                    items(it) { message ->
                        Text(text = message.message)
                    }
                }


            }

            val query = remember { mutableStateOf("") }
            val onClick = {
                viewModel.add(query.value)
                query.value = ""
            }
            OutlinedTextField(
                value = query.value,
                onValueChange = { newValue ->
                    query.value = newValue
                },
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .navigationBarsWithImePadding()
                    .constrainAs(textField) {
                        top.linkTo(lazyColumn.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    },
                label = {
                    Text(text = "Enter Message")
                },
                trailingIcon = {
                    IconButton(
                        onClick = onClick
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Search Icon",
                        )
                    }
                },
                textStyle = TextStyle(
                    color = MaterialTheme.colors.onSurface,
                    background = Color.Transparent,
                ),

                keyboardOptions = KeyboardOptions(
                    autoCorrect = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onClick() }
                )
            )

        }
    }

}