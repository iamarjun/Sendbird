package com.arjun.sendbird.data.model

sealed class Attachments {
    object Camera : Attachments()
    object Gallery : Attachments()
}