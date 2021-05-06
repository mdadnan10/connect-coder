package com.project.connectCoder.daos

import com.google.firebase.firestore.FirebaseFirestore
import com.project.connectCoder.model.Comments
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CommentDao {
    val db = FirebaseFirestore.getInstance()
    fun addComments(postId : String, cUser : String, commentText : String){
         val commentCollection = db.collection("comments/posts/$postId")

        GlobalScope.launch {

            val commentId = commentCollection.document()
            val comment = Comments(cUser, commentId.id, postId, commentText)

            commentId.set(comment)
        }
    }
}