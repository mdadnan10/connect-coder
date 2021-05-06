package com.project.connectCoder.activity.insideMain

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.connectCoder.R
import com.project.connectCoder.adapters.CommentsAdapter
import com.project.connectCoder.daos.CommentDao
import com.project.connectCoder.daos.UserDao
import com.project.connectCoder.model.Comments
import com.project.connectCoder.model.ConnectCoderUser
import kotlinx.android.synthetic.main.activity_comment.*

class CommentActivity : AppCompatActivity() {

    val currentUser = FirebaseAuth.getInstance().currentUser.uid
    val userDao = UserDao()
    private val commentDao = CommentDao()
    var postId: String? = null
    var publisherId : String? = null
    lateinit var commentsAdapter: CommentsAdapter
    lateinit var commentsList: ArrayList<Comments>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        postId = intent.getStringExtra("POST_ID")
        publisherId = intent.getStringExtra("PUBLISHER_ID")

        tv_comments.setColors(R.color.color_primary, R.color.color_secondary)
        btn_back.setOnClickListener {
            onBackPressed()
            finish()
        }

        btnComment.setOnClickListener {
            uploadComment()
        }

        showCommentingUserImage()
        setUpRecyclerView()
        showComments()
    }

    private fun setUpRecyclerView() {

        commentRecyclerView.setHasFixedSize(true)
        commentRecyclerView.layoutManager = LinearLayoutManager(this)

        commentsList = ArrayList()
        commentsAdapter = CommentsAdapter(commentsList)

        commentRecyclerView.adapter = commentsAdapter

    }

    private fun showComments() {
        val ref = commentDao.db.collection("comments/posts/$postId")

        ref.addSnapshotListener { value, _ ->
            commentsList.clear()

            value?.forEach {

                val comments = it.toObject(Comments::class.java)
                commentsList.add(comments)

            }
            commentsAdapter.notifyDataSetChanged()
        }

    }

    private fun showCommentingUserImage() {
        val user = userDao.getUserById(currentUser)
        user.addOnSuccessListener {

            val cUSer = it.toObject(ConnectCoderUser::class.java)
            Glide.with(this).load(cUSer!!.profile).into(commentingUserImg)

        }

    }

    private fun uploadComment() {

        val commentText = etCommentText.text.toString().trim()

        if (commentText.isEmpty()) {
            Toast.makeText(this, "Enter a comment to post.", Toast.LENGTH_SHORT).show()
            return
        }

        etCommentText.text.clear()
        commentDao.addComments(postId!!, currentUser, commentText)
        addNotifications(commentText)
    }

    private fun addNotifications(commentText : String) {
        val ref = FirebaseFirestore.getInstance().collection("notification/posts/$publisherId").document()

        val map = mutableMapOf<String, Any>()
        map["uid"] = currentUser
        map["commentText"] = "commented: $commentText"
        map["postId"] = postId!!
        map["isPost"] = true

        ref.set(map)
    }
}