package com.project.connectCoder.daos

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.project.connectCoder.model.ChatMessage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*

class SendMessageDao {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    fun sendMessage(chatMessage: ChatMessage, fromRef : DocumentReference) {
        fromRef.set(chatMessage)
    }

    fun toMessage(chatMessage: ChatMessage, toRef : DocumentReference){

       toRef.set(chatMessage)
    }

    fun latestMessage(chatMessage: ChatMessage, latestFromRef : DocumentReference){
        latestFromRef.set(chatMessage)
    }

    fun latestToMessage(chatMessage: ChatMessage, latestToRef : DocumentReference){
        latestToRef.set(chatMessage)
    }
}