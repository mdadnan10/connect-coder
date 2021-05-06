//package com.project.connectCoder.activity.insideMain
//
//import android.os.Bundle
//import android.view.View
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.material.textfield.TextInputLayout
//import com.project.connectCoder.R
//import com.project.connectCoder.daos.RandomDao
//import kotlinx.android.synthetic.main.activity_random_post.*
//
//class RandomPostActivity : AppCompatActivity() {
//
//    private lateinit var postDao: RandomDao
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_random_post)
//
//        tv_random_post.setColors(R.color.color_primary, R.color.color_secondary)
//
//        progressbar_layout_random_post.visibility = View.GONE
//        nav_progressBar_random_post.visibility = View.GONE
//
//        btn_back.setOnClickListener {
//            onBackPressed()
//            finish()
//        }
//
//        btn_save_random_post.setOnClickListener {
//            post()
//        }
//
//        btn_random_post.setOnClickListener {
//            post()
//        }
//
//    }
//
//    private fun validatePost(postText: String, layout: TextInputLayout): Boolean {
//
//        return if (postText.isEmpty()) {
//            layout.error = "Write something..."
//            false
//        } else {
//            layout.error = null
//            layout.isErrorEnabled = false
//            true
//        }
//
//    }
//
//    private fun post() {
//
//        val postLayout = findViewById<View>(R.id.et_random_post) as TextInputLayout
//        val postText = postLayout.editText?.text.toString().trim()
//
//        if (!validatePost(postText, postLayout)) return
//
//        nav_progressBar_random_post.visibility = View.VISIBLE
//        progressbar_layout_random_post.visibility = View.VISIBLE
//        btn_save_random_post.visibility = View.GONE
//        btn_random_post.visibility = View.GONE
//
//        postDao = RandomDao()
//        postDao.addPost(postText)
//        postDao.updatePostIds()
//
//        nav_progressBar_random_post.visibility = View.GONE
//        progressbar_layout_random_post.visibility = View.GONE
//        btn_save_random_post.visibility = View.VISIBLE
//        btn_random_post.visibility = View.VISIBLE
//
//        finish()
//    }
//}