package com.project.connectCoder.model

data class CodeHubPosts(
    val uid: String = "",
    val postId : String = "",
    val name: String = "",
    val userName: String = "",
    val profile: String = "",
    val postTitle: String = "",
    val postImg: String = "",
    val postDownLoadUrl: String = "",
    val createdAt: Long = 0L,
    val likedBy: ArrayList<String> = ArrayList(),
    val downloadedBy: ArrayList<String> = ArrayList()
)
