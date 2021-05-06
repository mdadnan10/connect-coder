package com.project.connectCoder.activity.insideMain

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.project.connectCoder.R
import com.project.connectCoder.daos.FeedPostDao
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_feed_post.*


class FeedPostActivity : AppCompatActivity() {

    lateinit var feedPostDao: FeedPostDao
    val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var myUrl: String
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_post)

        tv_feed_post.setColors(R.color.color_primary, R.color.color_secondary)
        selectFeedPostImage.setColors(R.color.color_primary, R.color.color_secondary)

        nav_progressBar_feed_post.visibility = View.GONE

        btn_back.setOnClickListener {
            onBackPressed()
        }

        cardViewImg.setOnClickListener {
            CropImage.activity().setAspectRatio(1, 1).start(this)
        }

        selectFeedPostImage.setOnClickListener {
            CropImage.activity().setAspectRatio(1, 1).start(this)
        }

        btn_save_feed_post.setOnClickListener {
            savePost()
        }

        btn_save_feed_posts.setOnClickListener {
            savePost()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val code = CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE

        if (requestCode == code && resultCode == Activity.RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            uploadFeedPostImage.setImageURI(imageUri)
            selectFeedPostImage.visibility = View.VISIBLE
        }
    }

    private fun validateTitle(titleText: String, layout: TextInputLayout): Boolean {

        return if (titleText.isEmpty()) {
            layout.error = "Write a title"
            false
        } else {
            layout.error = null
            layout.isErrorEnabled = false
            true
        }

    }

    private fun savePost() {
        val postLayout = findViewById<View>(R.id.et_feed_post_title) as TextInputLayout
        val postTitle = postLayout.editText?.text.toString().trim()

        if (!validateTitle(postTitle, postLayout)) return
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_LONG).show()
            return
        }

        if (imageUri != null) {
            val pd = ProgressDialog(this)
            pd.setTitle("Adding your post")
            pd.show()
            pd.setCancelable(false)

            val postImageRef = storage.reference.child("feed post")
                .child("${auth.currentUser.uid}/ ${System.currentTimeMillis()}.jpg")

            val uploadTask: StorageTask<*>
            uploadTask = postImageRef.putFile(imageUri!!)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    pd.dismiss()
                    Toast.makeText(
                        this,
                        "Unable to upload ${task.exception}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

                return@Continuation postImageRef.downloadUrl
            })

                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val downLoadUrl = task.result
                        myUrl = downLoadUrl.toString()

                        saveFeePost(postTitle, myUrl)

                        pd.dismiss()
                        Toast.makeText(
                            this,
                            "Post Added Successfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }

            uploadTask.addOnFailureListener {
                pd.dismiss()
                Toast.makeText(this, "Unable to upload ${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }

            uploadTask.addOnProgressListener {
                val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
                pd.setMessage("Uploading ${progress.toInt()}%")
            }

        }
    }

    private fun saveFeePost(postTitle: String, myUrl: String) {

        feedPostDao = FeedPostDao()
        feedPostDao.addFeedPost(postTitle, myUrl)
        feedPostDao.updateFeedPostIds()

    }
}