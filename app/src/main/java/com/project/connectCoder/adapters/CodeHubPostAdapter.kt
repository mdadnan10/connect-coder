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
import com.project.connectCoder.daos.CodeHubDao
import com.project.connectCoder.fragment.ProfileFragment
import com.project.connectCoder.model.CodeHubPosts
import com.project.connectCoder.util.Utils

class CodeHubPostAdapter(options: FirestoreRecyclerOptions<CodeHubPosts>, private val listener: ICodeHubPostAdapter) :
    FirestoreRecyclerAdapter<CodeHubPosts, CodeHubPostAdapter.CodeHubPostViewHolder>(options) {

    lateinit var context: Context
    var uid : String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodeHubPostViewHolder {
        val viewHolder = CodeHubPostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_code_hub_post, parent, false))
        context = parent.context
        viewHolder.likeBtn.setOnClickListener {
            listener.onCodeHubPostLikeClicked(snapshots.getSnapshot(viewHolder.adapterPosition).id)
            addNotifications(uid!!, snapshots.getSnapshot(viewHolder.adapterPosition).id)
        }
        viewHolder.downLoadBtn.setOnClickListener {
            listener.onCodeHubPostDownloadClicked(snapshots.getSnapshot(viewHolder.adapterPosition).id)
            addDownloadNotifications(uid!!, snapshots.getSnapshot(viewHolder.adapterPosition).id)
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: CodeHubPostViewHolder, position: Int, model: CodeHubPosts) {

        holder.userName.text = model.name
        holder.postTitle.text = model.postTitle
        holder.likeCount.text = model.likedBy.size.toString()
        holder.createdAt.text = Utils.getTimeAgo(model.createdAt).toString()
        holder.downloadCount.text = model.downloadedBy.size.toString()
        Glide.with(holder.userImage.context).load(model.profile).into(holder.userImage)
        Glide.with(holder.postImage.context).load(model.postImg).into(holder.postImage)
        uid = model.uid

        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser!!.uid
        val isLiked =  model.likedBy.contains(currentUserId)
        val isDownloaded = model.downloadedBy.contains(currentUserId)
        val editAble = model.uid

        val postId = snapshots.getSnapshot(holder.adapterPosition).id

        holder.userName.setOnClickListener {
            val sharedPreferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            sharedPreferences.putString("userId", model.uid).apply()

            val transaction = context as FragmentActivity

            transaction.supportFragmentManager.beginTransaction().replace(R.id.frame_layout, ProfileFragment()).commit()
        }

        if (isDownloaded){
            holder.downLoadBtn.setImageDrawable(ContextCompat.getDrawable(holder.downLoadBtn.context, R.drawable.ic_downloaded))
        }
        else{
            holder.downLoadBtn.setImageDrawable(ContextCompat.getDrawable(holder.downLoadBtn.context, R.drawable.ic_download))
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
                                val ref = CodeHubDao().codeHubPostCollection.document(postId)
                                val text = editTitle.text.toString().trim()

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
                                    Toast.makeText(context, "Code hub post deleted Successfully", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(context, "Thanks for reporting we will refer this post to our t&c.", Toast.LENGTH_LONG).show()
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

    }

    class CodeHubPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val userImage: ImageView = itemView.findViewById(R.id.imgCodeHubPostUser)
        val userName: TextView = itemView.findViewById(R.id.tvCodeHubPostUserName)
        val createdAt: TextView = itemView.findViewById(R.id.tvCodeHubPostCreatedAt)
        val postTitle: TextView = itemView.findViewById(R.id.codeHubPostTitle)
        val postImage: ImageView = itemView.findViewById(R.id.codeHubPost)
        val likeBtn: ImageView = itemView.findViewById(R.id.CodeHubLikeBtn)
        val likeCount: TextView = itemView.findViewById(R.id.CodeHubLikeCount)
        val options: ImageView = itemView.findViewById(R.id.codeHubPostOptions)
        val downLoadBtn : ImageView = itemView.findViewById(R.id.CodeHubDownLoadBtn)
        val downloadCount : TextView = itemView.findViewById(R.id.CodeHubDownloadCount)
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

interface ICodeHubPostAdapter {
    fun onCodeHubPostLikeClicked(postId: String)
    fun onCodeHubPostDownloadClicked(postId: String)
}