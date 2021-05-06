package com.project.connectCoder.daos

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.project.connectCoder.model.ConnectCoderUser
import com.project.connectCoder.model.PostIds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserDao {
    private val db = FirebaseFirestore.getInstance()
    val usersCollection = db.collection("users")
    private val auth = FirebaseAuth.getInstance()
    private val userPostIds = db.collection("userPostIds")

    fun addUsers(connectCoderUser: ConnectCoderUser?) {

        connectCoderUser?.let {
            GlobalScope.launch(Dispatchers.IO) {
                usersCollection.document(connectCoderUser.uid).set(it)
            }
        }

    }

    fun getUserById(uId: String): Task<DocumentSnapshot> {
        return usersCollection.document(uId).get()
    }

    fun updateUserInfo(map: MutableMap<String, Any>) {
        GlobalScope.launch {
            val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
            usersCollection.document(currentUserId).update(map)
        }
    }

    fun savePostIds(postIds: PostIds) {

        postIds.let {
            GlobalScope.launch {
                val currentUserIds = auth.currentUser.uid
                userPostIds.document(currentUserIds).set(it)
            }
        }
    }

}
