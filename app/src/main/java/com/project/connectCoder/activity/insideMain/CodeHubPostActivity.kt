package com.project.connectCoder.activity.insideMain

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import com.project.connectCoder.daos.CodeHubDao
import kotlinx.android.synthetic.main.activity_code_hub_post.*

class CodeHubPostActivity : AppCompatActivity() {

    val auth = FirebaseAuth.getInstance()
    lateinit var codeHubDao: CodeHubDao
    private val storage = FirebaseStorage.getInstance()
    private lateinit var myUrl: String
    private var fileUri: Uri? = null
    private var imageUri: String? = null
    private var mimeType: String? = null
    private var selected = ""
    private var selectedUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_hub_post)

        tv_code_hub_post.setColors(R.color.color_primary, R.color.color_secondary)
        selectCodeHubFile.setColors(R.color.color_primary, R.color.color_secondary)
        nav_progressBar_code_hub_post.visibility = View.GONE


        btn_back.setOnClickListener {
            onBackPressed()
        }

        cardViewFile.setOnClickListener {
            chooseFile()
        }

        selectCodeHubFile.setOnClickListener {
            chooseFile()
        }

        btn_save_code_hub_post.setOnClickListener {
            savePost()
        }

        btn_code_hub_post.setOnClickListener {
            savePost()
        }

    }

    private fun chooseFile() {
        val intent = Intent().setType("*/*")
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select a file"), 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            fileUri = data.data

            mimeType = fileUri!!.let { returnUri ->
                contentResolver.getType(returnUri)
            }

            Log.d("excel", mimeType!!)

            if (mimeType!! == "application/pdf") {
                codeHubPostImage.setImageResource(R.drawable.ic_pdf)
                imageUri = "https://firebasestorage.googleapis.com/v0/b/connect-coder.appspot.com/o/code%20hub%20cover%2Fpdf.png?alt=media&token=47175b89-058c-4d99-ab1c-0ad69054bfa5"
            }
            else if (mimeType!! == "application/zip") {
                codeHubPostImage.setImageResource(R.drawable.ic_zip)
                imageUri = "https://firebasestorage.googleapis.com/v0/b/connect-coder.appspot.com/o/code%20hub%20cover%2Fzip.png?alt=media&token=dc89a550-d72c-4aef-8d8a-ed6610422b26"
            }
            else if (mimeType!! == "application/msword") {
                codeHubPostImage.setImageResource(R.drawable.ic_word)
                imageUri = "https://firebasestorage.googleapis.com/v0/b/connect-coder.appspot.com/o/code%20hub%20cover%2Fword.png?alt=media&token=72652def-1cd3-4c22-ab6b-1bf3702b8388"
            }
            else if (mimeType!! == "application/vnd.ms-powerpoint") {
                codeHubPostImage.setImageResource(R.drawable.ic_powerpoint)
                imageUri = "https://firebasestorage.googleapis.com/v0/b/connect-coder.appspot.com/o/code%20hub%20cover%2Fpowerpoint.png?alt=media&token=8b49bcfb-989c-4d17-ba1c-5d3d5326fef1"
            }
            else if (mimeType!! == "application/vnd.ms-excel" || mimeType!! == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" || mimeType!! == "application/xlsx") {
                codeHubPostImage.setImageResource(R.drawable.ic_excel)
                imageUri = "https://firebasestorage.googleapis.com/v0/b/connect-coder.appspot.com/o/code%20hub%20cover%2Fexcel.png?alt=media&token=18087919-ec06-4ad8-aaef-5586c013cf1e"
            }
            else if (mimeType!! == "image/png" || mimeType!! == "image/jpeg" || mimeType!! == "image/webp") {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, fileUri)
                codeHubPostImage.setImageBitmap(bitmap)
                selected = "selected"
            }
            else if (mimeType!! == "video/mp4") {
                codeHubPostImage.setImageResource(R.drawable.ic_multimedia)
                imageUri = "https://firebasestorage.googleapis.com/v0/b/connect-coder.appspot.com/o/code%20hub%20cover%2Fmovie.png?alt=media&token=d327a58b-9c0f-4e84-810f-af32faf9dd8b"
            }
            else if (mimeType!! == "text/plain") {
                codeHubPostImage.setImageResource(R.drawable.ic_note)
                imageUri = "https://firebasestorage.googleapis.com/v0/b/connect-coder.appspot.com/o/code%20hub%20cover%2Fnote.png?alt=media&token=22d8ed8e-29d6-47de-8293-fb1322857f09"
            }
            else if (mimeType!! == "audio/mpeg" || mimeType!! == "audio/ogg") {
                codeHubPostImage.setImageResource(R.drawable.audio)
                imageUri = "https://firebasestorage.googleapis.com/v0/b/connect-coder.appspot.com/o/code%20hub%20cover%2Faudio-waves.png?alt=media&token=b2b9feae-d924-474c-b71c-99df39a32ee2"
            }
            else {
                codeHubPostImage.setImageResource(R.drawable.ic_documents)
                imageUri = "https://firebasestorage.googleapis.com/v0/b/connect-coder.appspot.com/o/code%20hub%20cover%2Fdocuments.png?alt=media&token=34bc87c5-5d49-4df2-81bd-305e331d7dbf"
            }

            itemSelected.visibility = View.GONE
            selectCodeHubFile.visibility = View.VISIBLE
        }

    }

    private fun validateTitle(titleText: String, layout: TextInputLayout): Boolean {

        return if (titleText.isEmpty()) {
            layout.error = "Write a title..."
            false
        } else {
            layout.error = null
            layout.isErrorEnabled = false
            true
        }

    }


    private fun savePost() {
        val postLayout = findViewById<View>(R.id.et_code_hub_post_title) as TextInputLayout
        val postTitle = postLayout.editText?.text.toString().trim()

        if (!validateTitle(postTitle, postLayout)) return
        if (fileUri == null) {
            Toast.makeText(this, "Please select a file.", Toast.LENGTH_SHORT).show()
            return
        }

        if (fileUri != null) {
            val pd = ProgressDialog(this)
            pd.setTitle("Adding your post")
            pd.show()
            pd.setCancelable(false)

            val li = mimeType!!.lastIndexOf("/")
            val extension = mimeType!!.substring(li + 1)

            val postFileRef = storage.reference.child("code hub").child("${auth.currentUser.uid}/ ${System.currentTimeMillis()}.$extension")
            val postImageRef = storage.reference.child("code hub cover/ ${System.currentTimeMillis()}.jpg")

            if (selected == "selected") {

                val uploadImages : StorageTask<*>
                uploadImages = postImageRef.putFile(fileUri!!)

                uploadImages.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> {
                    if (!it.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Unable to upload ${it.exception}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    return@Continuation postImageRef.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val downloadImageUrl = task.result
                         imageUri = downloadImageUrl.toString()

                    }
                }
            }


            val uploadTask: StorageTask<*>
            uploadTask = postFileRef.putFile(fileUri!!)

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

                return@Continuation postFileRef.downloadUrl
            })

                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val downLoadUrl = task.result
                        myUrl = downLoadUrl.toString()

                        saveCodeHubPost(postTitle, imageUri!!, myUrl)

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

    private fun saveCodeHubPost(postTitle: String, imageUrl: String, myUrl: String) {

        codeHubDao = CodeHubDao()
        codeHubDao.addCodeHubPost(postTitle, imageUrl, myUrl)
        codeHubDao.updateCodeHubPostIds()

    }

}