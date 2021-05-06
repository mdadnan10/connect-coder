package com.project.connectCoder.activity.messages

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.project.connectCoder.R
import com.project.connectCoder.activity.main.MainActivity
import com.project.connectCoder.activity.messages.SelectUsers.Companion.USER_KEY
import com.project.connectCoder.daos.SendMessageDao
import com.project.connectCoder.daos.UserDao
import com.project.connectCoder.model.ChatMessage
import com.project.connectCoder.model.ConnectCoderUser
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.item_search_user.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MessageActivity : AppCompatActivity() {

    companion object{
        var currentUser : ConnectCoderUser? = null
    }

    var auth = FirebaseAuth.getInstance()
    private val userDao = UserDao()
    private val messageDao = SendMessageDao()
    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        tv_message_user_name.setColors(R.color.color_primary, R.color.color_secondary)

        btn_back.setOnClickListener {
            onBackPressed()
            finish()
        }
        btn_search_message_users.setOnClickListener {
            startActivity(Intent(this, SelectUsers::class.java))
        }

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatLogActivity::class.java)

            val clickedUser = item as LatestMessage
            intent.putExtra(USER_KEY, clickedUser.chatPartnerUser)

            startActivity(intent)
        }

        displayUserName()
        listenForLatestMessages()
        setUpRecyclerView()
    }

    private val latestMessagesMap = HashMap<String, ChatMessage>()

    private fun listenForLatestMessages() {
        val fromId = auth.currentUser.uid
        val ref = messageDao.db.collection("latest-messages/messages/$fromId")

        ref.addSnapshotListener { snapshot, error ->

            if (error != null){
                Toast.makeText(this, "Some problem occurred while retrieving latest message.", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            for (value in snapshot!!.documentChanges){

                if (value.type == DocumentChange.Type.ADDED){
                    val chatMessage = value.document.toObject(ChatMessage::class.java)

                    latestMessagesMap[value.document.reference.id] = chatMessage
                    refreshRecyclerViewMessages()
                }

                if (value.type == DocumentChange.Type.MODIFIED){
                    val chatMessage = value.document.toObject(ChatMessage::class.java)

                    latestMessagesMap[value.document.reference.id] = chatMessage
                    refreshRecyclerViewMessages()
                }

            }

        }
    }

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessage(it))
        }
    }

    private fun setUpRecyclerView() {
        recentMessageRecyclerView.adapter = adapter
    }

    class LatestMessage(private val chatMessage: ChatMessage) : Item<GroupieViewHolder>(){
        var chatPartnerUser : ConnectCoderUser? = null

        override fun getLayout(): Int {
            return R.layout.item_search_user
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.searchUserProfession.text = chatMessage.text

            val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
            val chatPartnerId = if (chatMessage.fromUid == currentUser) chatMessage.toUid
            else chatMessage.fromUid

            val userDao = UserDao()
            val ref = userDao.usersCollection.document(chatPartnerId)
            ref.addSnapshotListener { value, error ->
                if (error != null){
                    Toast.makeText(viewHolder.itemView.context, "Something Wrong Happen.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                chatPartnerUser = value?.toObject(ConnectCoderUser::class.java)
                viewHolder.itemView.searchUserName.text = chatPartnerUser!!.name
                Glide.with(viewHolder.itemView.context).load(chatPartnerUser!!.profile).into(viewHolder.itemView.searchUserImage)
            }
        }

    }

    private fun displayUserName() {
        GlobalScope.launch(Dispatchers.IO) {
            val uId = auth.currentUser!!.uid
            val user = userDao.getUserById(uId).await().toObject(ConnectCoderUser::class.java)!!
            currentUser = user
            withContext(Dispatchers.Main) {
                tv_message_user_name.text = user.userName
            }
        }
    }
}