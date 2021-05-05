package com.arjun.sendbird.util

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Log
import com.sendbird.android.BaseChannel
import com.sendbird.android.BaseMessage
import com.sendbird.android.SendBird
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

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

const val ACCESS_TOKEN = "615e13d4bb6f5e0d2815e6182e11c1c619affb14"

fun Context.widthDp() = resources.displayMetrics.run { widthPixels / density }
fun Context.heightDp() = resources.displayMetrics.run { heightPixels / density }