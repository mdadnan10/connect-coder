package com.project.connectCoder.activity.messages

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.*
import com.project.connectCoder.R
import com.project.connectCoder.daos.SendMessageDao
import com.project.connectCoder.fragment.ProfileFragment
import com.project.connectCoder.model.ChatMessage
import com.project.connectCoder.model.ConnectCoderUser
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.item_from_row.view.*
import kotlinx.android.synthetic.main.item_to_row.view.*
import java.util.*
import java.util.TimeZone.*

class ChatLogActivity : AppCompatActivity() {

    private val sendMessageDao = SendMessageDao()
    private val currentUserId = sendMessageDao.auth.currentUser.uid
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private var toUser: ConnectCoderUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        val toUserFromProfile = intent.getParcelableExtra<ConnectCoderUser>(ProfileFragment.USER_KEY)

        toUser = intent.getParcelableExtra(SelectUsers.USER_KEY) ?: toUserFromProfile

        tv_chat_log_user_name.setColors(R.color.color_primary, R.color.color_secondary)
        tv_chat_log_user_name.text = toUser?.userName
        Glide.with(this).load(toUser?.profile).into(chatUserImage)

        btn_back.setOnClickListener {
            onBackPressed()
            finish()
        }

        btnSendMessage.setOnClickListener {
            performSendMessage()
        }

        setUpRecyclerView()
        listenForMessages()
    }

    @SuppressLint("SimpleDateFormat")
    private fun listenForMessages() {
        val toId = toUser?.uid
        val ref = sendMessageDao.db.collection("user-messages/$currentUserId/$toId")

        ref.orderBy("timeStamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    Toast.makeText(
                        this,
                        "Some problem occurred while sending message.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {

                    if (dc.type == DocumentChange.Type.ADDED) {
                        val chatMessages = dc.document.toObject(ChatMessage::class.java)

                        val message = chatMessages.text
                        val millis = chatMessages.timeStamp

                        val formatTime = SimpleDateFormat("hh.mm aa")
                        val time = formatTime.format(millis)

                        if (chatMessages.fromUid == currentUserId) {
                            val currentUser = MessageActivity.currentUser
                            if (currentUser != null)
                            adapter.add(ChatFromItem(message, currentUser, time))
                            else Toast.makeText(this, "Please go to message section.", Toast.LENGTH_LONG).show()
                        } else adapter.add(ChatToItem(message, toUser!!, time))
                    }

                }

            }

    }

    private fun performSendMessage() {

        val message = etTextMessage.text.toString().trim()

        if (message.isEmpty()) {
            Toast.makeText(this, "Enter a message to send.", Toast.LENGTH_LONG).show()
            return
        }

        val user = intent.getParcelableExtra<ConnectCoderUser>(SelectUsers.USER_KEY)
        val toId = user?.uid


        val fromRem = sendMessageDao.db.collection("user-messages/$currentUserId/$toId").document()
        val toRef = sendMessageDao.db.collection("user-messages/$toId/$currentUserId").document()

        val timeStamp = System.currentTimeMillis()
        val chatMessage = ChatMessage(toId!!, currentUserId, fromRem.id, message, timeStamp)

        sendMessageDao.sendMessage(chatMessage, toRef)
        etTextMessage.text.clear()
        chatLogRecyclerView.scrollToPosition(adapter.itemCount - 1)

        sendMessageDao.toMessage(chatMessage, fromRem)

        val latestFromRef = sendMessageDao.db.collection("latest-messages/")
            .document("messages/$currentUserId/$toId")
        val latestToRef = sendMessageDao.db.collection("latest-messages/")
            .document("messages/$toId/$currentUserId")

        sendMessageDao.latestMessage(chatMessage, latestFromRef)
        sendMessageDao.latestToMessage(chatMessage, latestToRef)
    }

    private fun setUpRecyclerView() {
        chatLogRecyclerView.adapter = adapter
    }
}

class ChatFromItem(
    private val fromText: String,
    private val user: ConnectCoderUser,
    private val fromTime: String
) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.item_from_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.txtMessageFrom.text = fromText
        viewHolder.itemView.messageFromTime.text = fromTime
        Glide.with(viewHolder.itemView.context).load(user.profile)
            .into(viewHolder.itemView.imgFromUser)
    }
}

class ChatToItem(private val toText: String, private val user: ConnectCoderUser, private val toTime: String) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.item_to_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.txtMessageTo.text = toText
        viewHolder.itemView.messageToTime.text = toTime
        Glide.with(viewHolder.itemView.context).load(user.profile).into(viewHolder.itemView.imgToUser)
    }
}