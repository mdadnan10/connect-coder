package com.project.connectCoder.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.connectCoder.R
import com.project.connectCoder.daos.CommentDao
import com.project.connectCoder.daos.UserDao
import com.project.connectCoder.model.Comments
import com.project.connectCoder.model.ConnectCoderUser

class CommentsAdapter(private val list : ArrayList<Comments>) : RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder>() {

    lateinit var context : Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
        context = parent.context
        return CommentsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false))
    }

    override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
        val comments = list[position]

        val currentUserId = FirebaseAuth.getInstance().currentUser.uid
        val editAble = comments.uid
        val commentId = comments.commentId
        val postId = comments.postId

        holder.commentText.text = comments.commentText
        getUserInfo(holder.commentUserImage, holder.userName, comments.uid)

        holder.option.setOnClickListener { view ->

            val menu = PopupMenu(context, view)

            if (currentUserId == editAble) {
                menu.menuInflater.inflate(R.menu.settings_edit, menu.menu)
                menu.show()
                menu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.edit_post -> {

                            val dialog = AlertDialog.Builder(context)
                            dialog.setTitle("Edit Comment")
                            val editComment = EditText(context)
                            dialog.setView(editComment)
                            editComment.setText(comments.commentText)
                            dialog.setPositiveButton("Save"){ _, _ ->
                                val ref = CommentDao().db.collection("comments/posts/$postId").document(commentId)

                                val text = editComment.text.toString()

                                if (text.isEmpty()) {
                                    Toast.makeText(context, "Please Enter a comment to update.", Toast.LENGTH_SHORT).show()
                                    return@setPositiveButton
                                }

                                val map = mutableMapOf<String, Any>()
                                map["commentText"] = text

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
                            dialog.setMessage("Are you sure you want to delete this comment ?")
                            dialog.setPositiveButton("Delete") { _, _ ->

                                val ref = CommentDao().db.collection("comments/posts/$postId").document(commentId).delete()
                                ref.addOnSuccessListener {
                                    Toast.makeText(context, "Comment Deleted Successfully.", Toast.LENGTH_SHORT).show()
                                }
                                ref.addOnFailureListener { e ->
                                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                                }

                            }
                            dialog.setNegativeButton("Cancel") { text, _ ->
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
                    when (it.itemId) {
                        R.id.report ->
                            Toast.makeText(
                                context,
                                "Thank for reporting we will refer this comment to our t&c.",
                                Toast.LENGTH_LONG
                            ).show()
                    }

                    return@setOnMenuItemClickListener true
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class CommentsViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val userName : TextView = itemView.findViewById(R.id.commentUserName)
        val commentText : TextView = itemView.findViewById(R.id.commentShowText)
        val commentUserImage : ImageView = itemView.findViewById(R.id.commentImg)
        val option: ImageView = itemView.findViewById(R.id.commentOptions)

    }

    private fun getUserInfo(commentUserImg : ImageView, userName : TextView, userId : String){
        val ref = UserDao().getUserById(userId)

        ref.addOnSuccessListener {

            val user = it.toObject(ConnectCoderUser::class.java)

            userName.text = user!!.name
            Glide.with(context).load(user.profile).into(commentUserImg)

        }

    }
}