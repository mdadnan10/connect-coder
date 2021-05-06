package com.project.connectCoder.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.connectCoder.R
import com.project.connectCoder.activity.insideMain.CommentActivity
import com.project.connectCoder.daos.FeedPostDao
import com.project.connectCoder.model.FeedPost
import com.project.connectCoder.util.Utils

class ProfileFeedAdapter (private val list : ArrayList<FeedPost>): RecyclerView.Adapter<ProfileFeedAdapter.ProfileFeedPostViewHolder>() {

    private lateinit var context : Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileFeedPostViewHolder {
        context = parent.context
        return ProfileFeedPostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_feed_post, parent, false))
    }

    override fun onBindViewHolder(holder: ProfileFeedPostViewHolder, position: Int) {
        val post = list[position]

        holder.userName.text = post.name
        holder.createdAt.text = Utils.getTimeAgo(post.createdAt)
        holder.title.text = post.postTitle
        holder.feedLikeCount.text = post.likedBy.size.toString()
        Glide.with(holder.userImage.context).load(post.profile).into(holder.userImage)
        Glide.with(holder.feedPostImage.context).load(post.postImg).into(holder.feedPostImage)

        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser!!.uid
        val isLiked =  post.likedBy.contains(currentUserId)
        val postId = post.postId
        val editAble = post.uid

        holder.feedLikeBtn.setOnClickListener {
            FeedPostDao().updateFeedPostLikes(postId)
            addNotifications(post.uid, postId)
        }

        if (isLiked) {
            holder.feedLikeBtn.setImageDrawable(ContextCompat.getDrawable(holder.feedLikeBtn.context, R.drawable.ic_liked))
        }
        else{
            holder.feedLikeBtn.setImageDrawable(ContextCompat.getDrawable(holder.feedLikeBtn.context, R.drawable.ic_unliked))
        }

        holder.commentBtn.setOnClickListener {

            val intent = Intent(context, CommentActivity::class.java)
            intent.putExtra("POST_ID", postId)
            intent.putExtra("PUBLISHER_ID", post.uid)
            context.startActivity(intent)

        }

        holder.shareBtn.setOnClickListener {
            Toast.makeText(context, "Post Shared", Toast.LENGTH_SHORT).show()
        }

        holder.feedOption.setOnClickListener { view ->

            val menu = PopupMenu(context, view)

            if (currentUserId == editAble) {
                menu.menuInflater.inflate(R.menu.settings_edit, menu.menu)
                menu.show()
                menu.setOnMenuItemClickListener {
                    when(it.itemId) {
                        R.id.edit_post -> {

                            val dialog = AlertDialog.Builder(context)
                            dialog.setTitle("Edit Post")
                            val editTitle = EditText(context)
                            dialog.setView(editTitle)
                            editTitle.setText(post.postTitle)
                            dialog.setPositiveButton("Save"){ _, _ ->
                                val ref = FeedPostDao().feedPostCollection.document(postId)
                                val text = editTitle.text.toString()

                                if (text.isEmpty()) {
                                    Toast.makeText(context, "Please Enter a title", Toast.LENGTH_SHORT).show()
                                    return@setPositiveButton
                                }

                                val map = mutableMapOf<String, Any>()
                                map["postTitle"] = text

                                ref.update(map)
                            }
                            dialog.setNegativeButton("Discard"){text, _ ->
                                text.dismiss()
                            }
                            dialog.setCancelable(false)
                            dialog.create()
                            dialog.show()

                        }

                        R.id.delete_post -> {

                            val dialog = AlertDialog.Builder(context)
                            dialog.setTitle("Delete")
                            dialog.setMessage("Are you sure want to delete this post ?")
                            dialog.setPositiveButton("Delete"){ _, _ ->

                                val ref = FeedPostDao().feedPostCollection.document(postId).delete()
                                ref.addOnSuccessListener {
                                    Toast.makeText(context, "Post Deleted Successfully.", Toast.LENGTH_SHORT).show()
                                }
                                ref.addOnFailureListener { e ->
                                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                                }

                            }
                            dialog.setNegativeButton("Cancel"){text, _ ->
                                text.dismiss()
                            }
                            dialog.setCancelable(false)
                            dialog.create()
                            dialog.show()

                        }

                    }

                    return@setOnMenuItemClickListener true
                }
            } else {
                menu.menuInflater.inflate(R.menu.setting_report, menu.menu)
                menu.show()
                menu.setOnMenuItemClickListener { it ->
                    when(it.itemId) {
                        R.id.report ->
                            Toast.makeText(context, "Thank for reporting we will refer this post to our t&c.", Toast.LENGTH_LONG).show()
                    }

                    return@setOnMenuItemClickListener true
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ProfileFeedPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName : TextView = itemView.findViewById(R.id.tvFeedPostUserName)
        val createdAt : TextView = itemView.findViewById(R.id.tvFeedPostCreatedAt)
        val title : TextView = itemView.findViewById(R.id.feedPostTitle)
        val userImage : ImageView = itemView.findViewById(R.id.imgFeedPostUserImage)
        val feedPostImage: ImageView = itemView.findViewById(R.id.feedPost)
        val feedLikeBtn : ImageView = itemView.findViewById(R.id.feedLikeBtn)
        val feedLikeCount : TextView = itemView.findViewById(R.id.feedLikeCount)
        val feedOption : ImageView = itemView.findViewById(R.id.feedOptions)
        val commentBtn : ImageView = itemView.findViewById(R.id.feedCommentBtn)
        val shareBtn : ImageView = itemView.findViewById(R.id.feedShareBtn)

    }

    private fun addNotifications(userId : String, postId : String){
        val ref = FirebaseFirestore.getInstance().collection("notification/posts/$userId")

        val map = mutableMapOf<String, Any>()
        map["uid"] = FirebaseAuth.getInstance().currentUser.uid
        map["commentText"] = "liked your feed post"
        map["postId"] = postId
        map["isPost"] = true

        ref.document().set(map)
    }
}