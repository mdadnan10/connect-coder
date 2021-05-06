package com.project.connectCoder.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.connectCoder.R
import com.project.connectCoder.activity.insideMain.CommentActivity
import com.project.connectCoder.daos.FeedPostDao
import com.project.connectCoder.fragment.ProfileFragment
import com.project.connectCoder.model.FeedPost
import com.project.connectCoder.util.Utils

class FeedPostAdapter(options: FirestoreRecyclerOptions<FeedPost>, private val listener: IFeedPostAdapter) :
    FirestoreRecyclerAdapter<FeedPost, FeedPostAdapter.FeedPostViewHolder>(options) {

    lateinit var context : Context
    var uid : String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedPostViewHolder {
        val viewHolder = FeedPostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_feed_post, parent, false))
        context = parent.context
        viewHolder.likeBtn.setOnClickListener {
            listener.onFeedPostLikeClicked(snapshots.getSnapshot(viewHolder.adapterPosition).id)
            addNotifications(uid!!, snapshots.getSnapshot(viewHolder.adapterPosition).id)
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: FeedPostViewHolder, position: Int, model: FeedPost) {

        holder.userName.text = model.name
        holder.postTitle.text = model.postTitle
        holder.likeCount.text = model.likedBy.size.toString()
        holder.createdAt.text = Utils.getTimeAgo(model.createdAt).toString()
        Glide.with(holder.userImage.context).load(model.profile).into(holder.userImage)
        Glide.with(holder.postImage.context).load(model.postImg).into(holder.postImage)
        uid = model.uid

        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser!!.uid
        val isLiked =  model.likedBy.contains(currentUserId)
        val editAble = model.uid
        val postId = snapshots.getSnapshot(holder.adapterPosition).id

        holder.userName.setOnClickListener {
            val sharedPreferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            sharedPreferences.putString("userId", model.uid).apply()

            val transaction = context as FragmentActivity

            transaction.supportFragmentManager.beginTransaction().replace(R.id.frame_layout, ProfileFragment()).commit()
        }

        holder.options.setOnClickListener { view ->

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
                            editTitle.setText(model.postTitle)
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

        if (isLiked) {
            holder.likeBtn.setImageDrawable(ContextCompat.getDrawable(holder.likeBtn.context, R.drawable.ic_liked))
        }
        else{
            holder.likeBtn.setImageDrawable(ContextCompat.getDrawable(holder.likeBtn.context, R.drawable.ic_unliked))
        }

        holder.commentBtn.setOnClickListener {
            val intent = Intent(context, CommentActivity::class.java)
            intent.putExtra("POST_ID", postId)
            intent.putExtra("PUBLISHER_ID", model.uid)
            context.startActivity(intent)
        }

        holder.shareBtn.setOnClickListener {
            Toast.makeText(context, "Post Shared", Toast.LENGTH_SHORT).show()
        }

    }

    class FeedPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val userImage: ImageView = itemView.findViewById(R.id.imgFeedPostUserImage)
        val userName: TextView = itemView.findViewById(R.id.tvFeedPostUserName)
        val createdAt: TextView = itemView.findViewById(R.id.tvFeedPostCreatedAt)
        val postTitle: TextView = itemView.findViewById(R.id.feedPostTitle)
        val postImage: ImageView = itemView.findViewById(R.id.feedPost)
        val likeBtn: ImageView = itemView.findViewById(R.id.feedLikeBtn)
        val likeCount: TextView = itemView.findViewById(R.id.feedLikeCount)
        val options : ImageView = itemView.findViewById(R.id.feedOptions)
        val commentBtn : ImageView = itemView.findViewById(R.id.feedCommentBtn)
        val shareBtn : ImageView = itemView.findViewById(R.id.feedShareBtn)

    }

    private fun addNotifications(userId : String, postId : String){
        val ref = FirebaseFirestore.getInstance().collection("notification/posts/$userId")

        val map = mutableMapOf<String, Any>()
        map["uid"] = FirebaseAuth.getInstance().currentUser.uid
        map["commentText"] = "liked your feed post."
        map["postId"] = postId
        map["isPost"] = true

        ref.document().set(map)
    }
}

interface IFeedPostAdapter {
    fun onFeedPostLikeClicked(postId: String)
}