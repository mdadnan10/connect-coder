package com.project.connectCoder.activity.insideMain

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.project.connectCoder.R
import com.project.connectCoder.daos.CodeHubDao
import com.project.connectCoder.daos.FeedPostDao
//import com.project.connectCoder.daos.RandomDao
import com.project.connectCoder.daos.UserDao
import com.project.connectCoder.model.ConnectCoderUser
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EditProfileActivity : AppCompatActivity() {

    private val userDao = UserDao()
//    private val randomDao = RandomDao()
    private val feedPostDao = FeedPostDao()
    private val codeHubPostDao = CodeHubDao()
    val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var checker = ""
    private lateinit var myUrl: String
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        tv_edit_text.setColors(R.color.color_primary, R.color.color_secondary)
        btn_change_profile_photo.setColors(R.color.color_primary, R.color.color_secondary)

        btn_discard.setOnClickListener {
            onBackPressed()
        }

        btn_change_profile_photo.setOnClickListener {
            checker = "clicked"
            CropImage.activity().setAspectRatio(1, 1).start(this)
        }

        btn_save.setOnClickListener {
            if (checker == "clicked") {
                if (saveDataWithProfilePhoto())
                    uploadProfilePhoto()
            } else {
                editProfile()
            }
        }

        btn_save_edit_info.setOnClickListener {
            if (checker == "clicked") {
                if (saveDataWithProfilePhoto())
                    uploadProfilePhoto()
            } else {
                editProfile()
            }
        }

        nav_progressBar_edit_profile.visibility = View.GONE
        progressbar_layout_edit_profile.visibility = View.GONE

        showUserData()

    }

    private fun uploadProfilePhoto() {
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image to upload.", Toast.LENGTH_SHORT).show()
            return
        } else {
            val pd = ProgressDialog(this)
            pd.setTitle("Updating Profile Photo")
            pd.show()
            pd.setCancelable(false)

            val imageRef = storage.reference.child("profile").child(auth.currentUser.uid)

            val uploadTask: StorageTask<*>
            uploadTask = imageRef.putFile(imageUri!!)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    pd.dismiss()
                    Toast.makeText(this, "Unable to upload ${task.exception}", Toast.LENGTH_SHORT)
                        .show()
                }

                return@Continuation imageRef.downloadUrl
            })

                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val downLoadUrl = task.result
                        myUrl = downLoadUrl.toString()

                        saveProfileImage(myUrl)
                        pd.dismiss()
                        Toast.makeText(
                            this,
                            "Profile Updated Successfully.",
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()
                    }
                }

            uploadTask.addOnFailureListener {
                pd.dismiss()
                Toast.makeText(this, "Unable to upload ${it.message}", Toast.LENGTH_SHORT).show()
            }

            uploadTask.addOnProgressListener {
                val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
                pd.setMessage("Uploading ${progress.toInt()}%")
            }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val code = CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE

        if (requestCode == code && resultCode == Activity.RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            img_edit_profile.setImageURI(imageUri)
        }

    }

    private fun showUserData() {

        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val user = userDao.getUserById(currentUserId).await().toObject(ConnectCoderUser::class.java)!!

            withContext(Dispatchers.Main) {
                displayData(user)
            }

        }

    }

    private fun displayData(connectCoderUser: ConnectCoderUser) {

        val name = edit_profile_name.editText
        val userName = edit_profile_user_name.editText
        val profession = edit_profile_profession.editText
        val bio = edit_profile_bio.editText

        name?.setText(connectCoderUser.name)
        userName?.setText(connectCoderUser.userName)
        profession?.setText(connectCoderUser.profession)
        bio?.setText(connectCoderUser.bio)
        Glide.with(this).load(connectCoderUser.profile).into(img_edit_profile)
    }

    private fun validateFullName(name: String, layout: TextInputLayout): Boolean {

        return if (name.isEmpty()) {
            layout.error = "Field can't be empty"
            false
        } else {
            layout.error = null
            layout.isErrorEnabled = false
            true
        }

    }

    private fun validateUserName(userName: String, layout: TextInputLayout): Boolean {

        return if (userName.isEmpty()) {
            layout.error = "Field can't be empty"
            false
        } else if (userName.length > 15) {
            layout.error = "Username too long"
            false
        } else {
            layout.error = null
            layout.isErrorEnabled = false
            true
        }

    }

    private fun validateProfession(profession: String, layout: TextInputLayout): Boolean {

        return if (profession.isEmpty()) {
            layout.error = "Field can't be empty"
            false
        } else {
            layout.error = null
            layout.isErrorEnabled = false
            true
        }

    }

    private fun validateBio(bio: String, layout: TextInputLayout): Boolean {

        return if (bio.isEmpty()) {
            layout.error = "Bio can't be empty"
            false
        } else if (bio.length > 250) {
            layout.error = "Enter a bio between 1 to 250 char"
            false
        } else {
            layout.error = null
            layout.isErrorEnabled = false
            true
        }

    }

    private fun editProfile() {

        val nameLayout = findViewById<View>(R.id.edit_profile_name) as TextInputLayout
        val userNameLayout = findViewById<View>(R.id.edit_profile_user_name) as TextInputLayout
        val professionLayout = findViewById<View>(R.id.edit_profile_profession) as TextInputLayout
        val bioLayout = findViewById<View>(R.id.edit_profile_bio) as TextInputLayout

        val name = edit_profile_name.editText?.text.toString().trim().capitalize()
        val userName = edit_profile_user_name.editText?.text.toString().trim()
        val profession = edit_profile_profession.editText?.text.toString().trim().capitalize()
        val bio = edit_profile_bio.editText?.text.toString().trim()

        if (!validateFullName(name, nameLayout) || !validateUserName(
                userName,
                userNameLayout
            ) || !validateProfession(
                profession,
                professionLayout
            ) || !validateBio(bio, bioLayout)
        ) return

        saveUpdateData(name, userName, profession, bio)
        finish()
    }

    private fun saveDataWithProfilePhoto(): Boolean {

        val nameLayout = findViewById<View>(R.id.edit_profile_name) as TextInputLayout
        val userNameLayout = findViewById<View>(R.id.edit_profile_user_name) as TextInputLayout
        val professionLayout = findViewById<View>(R.id.edit_profile_profession) as TextInputLayout
        val bioLayout = findViewById<View>(R.id.edit_profile_bio) as TextInputLayout

        val name = edit_profile_name.editText?.text.toString().trim()
        val userName = edit_profile_user_name.editText?.text.toString().trim()
        val profession = edit_profile_profession.editText?.text.toString().trim()
        val bio = edit_profile_bio.editText?.text.toString().trim()

        if (!validateFullName(name, nameLayout) || !validateUserName(
                userName,
                userNameLayout
            ) || !validateProfession(
                profession,
                professionLayout
            ) || !validateBio(bio, bioLayout)
        ) return false
        else {
            saveUpdateData(name, userName, profession, bio)
            return true
        }
    }

    private fun saveUpdateData(name: String, userName: String, profession: String, bio: String) {

        val updateInfo = mutableMapOf<String, Any>()
        updateInfo["name"] = name
        updateInfo["userName"] = userName
        updateInfo["profession"] = profession
        updateInfo["bio"] = bio

        val postUserDataUpdate = mutableMapOf<String, Any>()
        postUserDataUpdate["name"] = name
        postUserDataUpdate["userName"] = userName

        userDao.updateUserInfo(updateInfo)
//        randomDao.updateUserInfo(postUserDataUpdate)
        feedPostDao.updateFeedPostUserInfo(postUserDataUpdate)
        codeHubPostDao.updateCodeHubPostUserInfo(postUserDataUpdate)
    }

    private fun saveProfileImage(url: String) {
        val updateInfo = mutableMapOf<String, Any>()
        updateInfo["profile"] = url

        val postUserProfile = mutableMapOf<String, Any>()
        postUserProfile["profile"] = url

        userDao.updateUserInfo(updateInfo)
//        randomDao.updateUserInfo(postUserProfile)
        feedPostDao.updateFeedPostUserInfo(postUserProfile)
        codeHubPostDao.updateCodeHubPostUserInfo(postUserProfile)
    }
}