package com.project.connectCoder.model

data class ChatMessage(
    val toUid: String = "",
    val fromUid: String = "",
    val messageId: String = "",
    val text: String = "",
    val timeStamp: Long = 0L
)
