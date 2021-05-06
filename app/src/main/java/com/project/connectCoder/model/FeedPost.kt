package com.project.connectCoder.model

data class FeedPost(
    val uid: String = "",
    val postId : String = "",
    val name: String = "",
    val userName: String = "",
    val profile: String = "",
    val postTitle: String = "",
    val postImg: String = "",
    val createdAt: Long = 0L,
    val likedBy: ArrayList<String> = ArrayList()
)
