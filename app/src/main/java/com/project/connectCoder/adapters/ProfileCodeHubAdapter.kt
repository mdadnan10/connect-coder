package com.project.connectCoder.adapters

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
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
import com.project.connectCoder.daos.CodeHubDao
import com.project.connectCoder.model.CodeHubPosts
import com.project.connectCoder.util.Utils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileCodeHubAdapter(private val list : ArrayList<CodeHubPosts>): RecyclerView.Adapter<ProfileCodeHubAdapter.ProfileCodeHubPostViewHolder>() {

    private lateinit var context: Context
    var myDownloadId: Long = 0L

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileCodeHubPostViewHolder {
        context = parent.context
        return ProfileCodeHubPostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_code_hub_post, parent, false))
    }

    override fun onBindViewHolder(holder: ProfileCodeHubPostViewHolder, position: Int) {
        val post = list[position]

        holder.userName.text = post.name
        holder.createdAt.text = Utils.getTimeAgo(post.createdAt)
        holder.postTitle.text = post.postTitle
        holder.likeCount.text = post.likedBy.size.toString()
        holder.downloadCount.text = post.downloadedBy.size.toString()
        Glide.with(holder.userImage.context).load(post.profile).into(holder.userImage)
        Glide.with(holder.postImage.context).load(post.postImg).into(holder.postImage)

        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser!!.uid
        val isLiked = post.likedBy.contains(currentUserId)
        val isDownloaded = post.downloadedBy.contains(currentUserId)
        val postId = post.postId
        val editAble = post.uid

        holder.downloadBtn.setOnClickListener {

            CodeHubDao().updateCodeHubPostDownloads(postId)
            addDownloadNotifications(post.uid, postId)

            GlobalScope.launch {
                val user = CodeHubDao().getCodeHubPostById(postId).await()
                    .toObject(CodeHubPosts::class.java)!!
                val link = user.postDownLoadUrl

                val request = DownloadManager.Request(Uri.parse(link))
                    .setTitle("Connect Coder file.")
                    .setDescription("Your file is downloading...")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(false)

                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                myDownloadId = dm.enqueue(request)


                val br = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                        if (id == myDownloadId) {
                            Toast.makeText(context, "Connect Coder File downloaded Successfully, please check your download manager.", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                context.registerReceiver(br, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            }

        }

        if (isDownloaded){
            holder.downloadBtn.setImageDrawable(ContextCompat.getDrawable(holder.downloadBtn.context, R.drawable.ic_downloaded))
        }
        else{
            holder.downloadBtn.setImageDrawable(ContextCompat.getDrawable(holder.downloadBtn.context, R.drawable.ic_download))
        }

        holder.likeBtn.setOnClickListener {
            CodeHubDao().updateCodeHubPostLikes(postId)
            addNotifications(post.uid, postId)
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
            intent.putExtra("PUBLISHER_ID", post.uid)
            context.startActivity(intent)

        }

        holder.option.setOnClickListener { view ->

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
                                val ref = CodeHubDao().codeHubPostCollection.document(postId)
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

                                val ref = CodeHubDao().codeHubPostCollection.document(postId).delete()
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

    override fun getItemCount(): Int { return list.size }

    class ProfileCodeHubPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName : TextView = itemView.findViewById(R.id.tvCodeHubPostUserName)
        val createdAt : TextView = itemView.findViewById(R.id.tvCodeHubPostCreatedAt)
        val userImage : ImageView = itemView.findViewById(R.id.imgCodeHubPostUser)
        val postTitle : TextView = itemView.findViewById(R.id.codeHubPostTitle)
        val postImage: ImageView = itemView.findViewById(R.id.codeHubPost)
        val likeBtn : ImageView = itemView.findViewById(R.id.CodeHubLikeBtn)
        val likeCount : TextView = itemView.findViewById(R.id.CodeHubLikeCount)
        val downloadBtn : ImageView = itemView.findViewById(R.id.CodeHubDownLoadBtn)
        val downloadCount : TextView = itemView.findViewById(R.id.CodeHubDownloadCount)
        val option : ImageView = itemView.findViewById(R.id.codeHubPostOptions)
        val commentBtn : ImageView = itemView.findViewById(R.id.CodeHubCommentBtn)

    }

    private fun addNotifications(userId : String, postId : String){
        val ref = FirebaseFirestore.getInstance().collection("notification/posts/$userId")

        val map = mutableMapOf<String, Any>()
        map["uid"] = FirebaseAuth.getInstance().currentUser.uid
        map["commentText"] = "liked your code hub post"
        map["postId"] = postId
        map["isPost"] = true

        ref.document().set(map)
    }

    private fun addDownloadNotifications(userId : String, postId : String){
        val ref = FirebaseFirestore.getInstance().collection("notification/posts/$userId")

        val map = mutableMapOf<String, Any>()
        map["uid"] = FirebaseAuth.getInstance().currentUser.uid
        map["commentText"] = "download your code hub post."
        map["postId"] = postId
        map["isPost"] = true

        ref.document().set(map)
    }
}