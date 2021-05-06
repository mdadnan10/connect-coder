package com.project.connectCoder.daos

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.project.connectCoder.model.ConnectCoderUser
import com.project.connectCoder.model.FeedPost
import com.project.connectCoder.model.PostIds
//import com.project.connectCoder.model.RandomPost
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FeedPostDao {

    private val db = FirebaseFirestore.getInstance()
    val feedPostCollection = db.collection("feedPosts")
    private val userPostIds = db.collection("userPostIds")
    private val auth = FirebaseAuth.getInstance()
    private val currentFeedPostId: DocumentReference = feedPostCollection.document()

    fun addFeedPost(title : String, image : String){
        GlobalScope.launch {

            val currentUserId = auth.currentUser.uid
            val userDao = UserDao()
            val user = userDao.getUserById(currentUserId).await().toObject(ConnectCoderUser::class.java)!!
            val name = user.name
            val userName = user.userName
            val profile = user.profile
            val currTime = System.currentTimeMillis()
            val post = FeedPost(currentUserId, currentFeedPostId.id, name, userName, profile, title, image, currTime)

            currentFeedPostId.set(post)
        }
    }

    fun getFeedPostById(postId: String): Task<DocumentSnapshot> {
        return feedPostCollection.document(postId).get()
    }

    // done
    fun updateFeedPostLikes(postId: String) {
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val post = getFeedPostById(postId).await().toObject(FeedPost::class.java)!!
            val isLiked = post.likedBy.contains(currentUserId)

            if (isLiked) post.likedBy.remove(currentUserId)
            else post.likedBy.add(currentUserId)

            feedPostCollection.document(postId).set(post)
        }

    }

    private fun getFeedPostIdsOfCurrentUser(currentUserId: String): Task<DocumentSnapshot> {
        return userPostIds.document(currentUserId).get()
    }

    fun updateFeedPostUserInfo(updatedUser: MutableMap<String, Any>) {

        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val currentUserPostIds = getFeedPostIdsOfCurrentUser(currentUserId).await().toObject(PostIds::class.java)

            for (ids in currentUserPostIds?.feedPostIds!!) {
                feedPostCollection.document(ids).update(updatedUser)
            }

        }
    }

    private fun getCurrentUserFeedPostIds(currentUserId: String): Task<DocumentSnapshot> {
        return userPostIds.document(currentUserId).get()
    }

    // done
    fun updateFeedPostIds() {

        GlobalScope.launch {
            val currentUserId = auth.currentUser.uid
            val user = getCurrentUserFeedPostIds(currentUserId).await().toObject(PostIds::class.java)!!
            user.feedPostIds.add(currentFeedPostId.id)

            userPostIds.document(currentUserId).set(user)
        }

    }

}