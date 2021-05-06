//package com.project.connectCoder.daos
//
//import com.google.android.gms.tasks.Task
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.DocumentReference
//import com.google.firebase.firestore.DocumentSnapshot
//import com.google.firebase.firestore.FirebaseFirestore
//import com.project.connectCoder.model.PostIds
//import com.project.connectCoder.model.RandomPost
//import com.project.connectCoder.model.ConnectCoderUser
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//
//class RandomDao {
//
//    private val db = FirebaseFirestore.getInstance()
//    val postCollection = db.collection("randomPosts")
//    private val userPostIds = db.collection("userPostIds")
//    private val auth = FirebaseAuth.getInstance()
//    private val currentRandomPostId: DocumentReference = postCollection.document()
//
//    fun addPost(text: String) {
//        GlobalScope.launch {
//            val currentUserId = auth.currentUser!!.uid
//            val userDao = UserDao()
//            val user = userDao.getUserById(currentUserId).await().toObject(ConnectCoderUser::class.java)!!
//            val name = user.name
//            val userName = user.userName
//            val profile = user.profile
//            val currTime = System.currentTimeMillis()
//            val post = RandomPost(currentUserId, name, userName, profile, text, currTime)
//
//            currentRandomPostId.set(post)
//        }
//    }
//
//    private fun getPostById(postId: String): Task<DocumentSnapshot> {
//        return postCollection.document(postId).get()
//    }
//
//    fun updateRandomPostLikes(postId: String) {
//        GlobalScope.launch {
//            val currentUserId = auth.currentUser!!.uid
//            val post = getPostById(postId).await().toObject(RandomPost::class.java)!!
//            val isLiked = post.likedBy.contains(currentUserId)
//
//            if (isLiked) post.likedBy.remove(currentUserId)
//            else post.likedBy.add(currentUserId)
//
//            postCollection.document(postId).set(post)
//        }
//
//    }
//
//
//    private fun getPostIdsOfCurrentUser(currentUserId: String): Task<DocumentSnapshot> {
//        return userPostIds.document(currentUserId).get()
//    }
//
//    fun updateUserInfo(updatedUser: MutableMap<String, Any>) {
//
//        GlobalScope.launch {
//            val currentUserId = auth.currentUser!!.uid
//            val currentUserPostIds = getPostIdsOfCurrentUser(currentUserId).await().toObject(PostIds::class.java)
//
//            for (ids in currentUserPostIds?.postIds!!) {
//                postCollection.document(ids).update(updatedUser)
//            }
//
//        }
//    }
//
//    private fun getCurrentUserPostIds(currentUserId: String): Task<DocumentSnapshot> {
//        return userPostIds.document(currentUserId).get()
//    }
//
//    fun updatePostIds() {
//
//        GlobalScope.launch {
//            val currentUserId = auth.currentUser.uid
//            val user = getCurrentUserPostIds(currentUserId).await().toObject(PostIds::class.java)!!
//            user.postIds.add(currentRandomPostId.id)
//
//            userPostIds.document(currentUserId).set(user)
//        }
//
//    }
//
//}
