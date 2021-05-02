package com.arjun.sendbird.util

import android.content.Context
import com.sendbird.android.BaseChannel
import com.sendbird.android.BaseMessage
import com.sendbird.android.SendBird
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun BaseMessage.isMe() = sender?.userId == SendBird.getCurrentUser().userId
fun BaseMessage.getHumanReadableDate(): String {
    val date: LocalDate =
        Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault())
            .toLocalDate()

    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM, dd")
    return formatter.format(date)
}

fun BaseChannel.getHumanReadableDate(): String {
    val date: LocalDate =
        Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault())
            .toLocalDate()

    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM, dd")
    return formatter.format(date)
}

const val ACCESS_TOKEN = "ee6f6cd6410c96a4bdb6343073de5cbf82dbf233"

fun Context.widthDp() = resources.displayMetrics.run { widthPixels / density }
fun Context.heightDp() = resources.displayMetrics.run { heightPixels / density }