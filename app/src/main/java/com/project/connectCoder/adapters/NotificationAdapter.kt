package com.project.connectCoder.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.connectCoder.R
import com.project.connectCoder.daos.CodeHubDao
import com.project.connectCoder.daos.CommentDao
import com.project.connectCoder.daos.FeedPostDao
import com.project.connectCoder.daos.UserDao
import com.project.connectCoder.model.CodeHubPosts
import com.project.connectCoder.model.ConnectCoderUser
import com.project.connectCoder.model.FeedPost
import com.project.connectCoder.model.Notification

class NotificationAdapter(private val list: ArrayList<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        context = parent.context
        return NotificationViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {

        val notification = list[position]

        holder.text.text = notification.commentText
        getUserInfo(holder.likedPersonProfile, holder.likedPersonUserName, notification.uid)

//        getFeedImage(holder.likedImage, notification.postId)
//        getCodeHubImage(holder.likedImage, notification.postId)

        holder.itemView.setOnClickListener {
            Toast.makeText(context, "Clicked ${notification.uid}", Toast.LENGTH_SHORT).show()
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val likedPersonProfile: ImageView = itemView.findViewById(R.id.notificationUserImage)
        val likedPersonUserName: TextView = itemView.findViewById(R.id.notificationUserName)
        val text: TextView = itemView.findViewById(R.id.notification_text)
        val likedImage: ImageView = itemView.findViewById(R.id.likedImage)

    }

    private fun getUserInfo(profile: ImageView, userName: TextView, userId: String) {
        val ref = UserDao().getUserById(userId)

        ref.addOnSuccessListener {

            val user = it.toObject(ConnectCoderUser::class.java)

            Glide.with(context).load(user!!.profile).into(profile)
            userName.text = user.name

        }
    }

//    private fun getFeedImage(postImage: ImageView, postId: String) {
//        val feedRef = FeedPostDao().getFeedPostById(postId)
//
//            feedRef.addOnSuccessListener {
//                val post = it.toObject(FeedPost::class.java)
//
//                Glide.with(context).load(post?.postImg).into(postImage)
//
//        }
//    }

//    private fun getCodeHubImage(postImage: ImageView, postId: String) {
//        val codeHubRef = CodeHubDao().getCodeHubPostById(postId)
//
//            codeHubRef.addOnSuccessListener {
//                val post = it.toObject(CodeHubPosts::class.java)
//
//                Glide.with(context).load(post?.postImg).into(postImage)
//
//        }
//    }

//    private fun getCommentImage(postImage: ImageView, postId: String) {
//        val codeHubRef = CommentDao().db.collection()
//
//        if (codeHubRef != null) {
//
//            codeHubRef.addOnSuccessListener {
//                val post = it.toObject(CodeHubPosts::class.java)
//
//                Glide.with(context).load(post?.postImg).into(postImage)
//            }
//        }
//    }
}