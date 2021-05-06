package com.project.connectCoder.daos

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.project.connectCoder.model.CodeHubPosts
import com.project.connectCoder.model.ConnectCoderUser
import com.project.connectCoder.model.PostIds
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CodeHubDao {

    private val db = FirebaseFirestore.getInstance()
    val codeHubPostCollection = db.collection("codeHubPosts")
    private val userPostIds = db.collection("userPostIds")
    private val auth = FirebaseAuth.getInstance()
    private val currentCodeHubPostId: DocumentReference = codeHubPostCollection.document()

    fun addCodeHubPost(postTitle: String, imageUrl: String, docUrl: String){
        GlobalScope.launch {

            val currentUserId = auth.currentUser.uid
            val userDao = UserDao()
            val user = userDao.getUserById(currentUserId).await().toObject(ConnectCoderUser::class.java)!!
            val name = user.name
            val userName = user.userName
            val profile = user.profile
            val currTime = System.currentTimeMillis()
            val post = CodeHubPosts(currentUserId, currentCodeHubPostId.id, name, userName, profile, postTitle, imageUrl, docUrl, currTime)

            currentCodeHubPostId.set(post)
        }
    }

    fun getCodeHubPostById(postId: String): Task<DocumentSnapshot> {
        return codeHubPostCollection.document(postId).get()
    }

    fun updateCodeHubPostLikes(postId: String) {
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val post = getCodeHubPostById(postId).await().toObject(CodeHubPosts::class.java)!!
            val isLiked = post.likedBy.contains(currentUserId)

            if (isLiked) post.likedBy.remove(currentUserId)
            else post.likedBy.add(currentUserId)

            codeHubPostCollection.document(postId).set(post)
        }

    }

    fun updateCodeHubPostDownloads(postId: String){
        GlobalScope.launch {

            val currentUserId = auth.currentUser!!.uid
            val post = getCodeHubPostById(postId).await().toObject(CodeHubPosts::class.java)!!

            post.downloadedBy.add(currentUserId)

            codeHubPostCollection.document(postId).set(post)
        }
    }

    private fun getCodeHubPostIdsOfCurrentUser(currentUserId: String): Task<DocumentSnapshot> {
        return userPostIds.document(currentUserId).get()
    }

    fun updateCodeHubPostUserInfo(updatedUser: MutableMap<String, Any>) {

        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val currentUserPostIds = getCodeHubPostIdsOfCurrentUser(currentUserId).await().toObject(PostIds::class.java)

            for (ids in currentUserPostIds?.codeHubPostIds!!) {
                codeHubPostCollection.document(ids).update(updatedUser)
            }

        }
    }

    private fun getCurrentUserCodeHubPostIds(currentUserId: String): Task<DocumentSnapshot> {
        return userPostIds.document(currentUserId).get()
    }

    fun updateCodeHubPostIds() {

        GlobalScope.launch {
            val currentUserId = auth.currentUser.uid
            val user = getCurrentUserCodeHubPostIds(currentUserId).await().toObject(PostIds::class.java)!!
            user.codeHubPostIds.add(currentCodeHubPostId.id)

            userPostIds.document(currentUserId).set(user)
        }

    }

}