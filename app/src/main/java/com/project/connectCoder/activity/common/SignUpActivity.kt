package com.project.connectCoder.activity.common

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.project.connectCoder.R
import com.project.connectCoder.activity.main.MainActivity
//import com.project.connectCoder.daos.RandomDao
import com.project.connectCoder.daos.UserDao
import com.project.connectCoder.model.PostIds
import com.project.connectCoder.model.ConnectCoderUser
import com.project.connectCoder.util.ConnectionManager
import kotlinx.android.synthetic.main.activity_sign_up.*


class SignUpActivity : AppCompatActivity() {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        btnBack.setOnClickListener {
            onBackPressed()
        }

        colorSignUp.setColors(R.color.color_primary, R.color.color_secondary)
        colorLogin.setColors(R.color.color_primary, R.color.color_secondary)

        colorLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


        if (ConnectionManager().checkConnectivity(this)) {

            progressbar_layout_signup.visibility = View.GONE

            btnSignUp.setOnClickListener {
                signUp()
            }
        } else {
            checkInterNet(this)
        }

    }

    private fun validateFullName(name: String, layout: TextInputLayout): Boolean {

        return if (name.isEmpty()) {
            layout.error = "You need to enter a name"
            false
        } else {
            layout.error = null
            layout.isErrorEnabled = false
            true
        }

    }

    private fun validateUserName(userName: String, layout: TextInputLayout): Boolean {

        return if (userName.isEmpty()) {
            layout.error = "You need to enter a ConnectCoderUser name"
            false
        } else if (userName.length >= 15) {
            layout.error = "Username too long"
            false
        } else {
            layout.error = null
            layout.isErrorEnabled = false
            true
        }

    }

    private fun validateEmail(email: String, layout: TextInputLayout): Boolean {

        return if (email.isEmpty()) {
            layout.error = "You need to enter a email"
            false
        } else if (!(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())) {
            layout.error = "enter a valid email"
            false
        } else {
            layout.error = null
            layout.isErrorEnabled = false
            true
        }

    }


    private fun showError(emailLayout: TextInputLayout): Boolean {

        emailLayout.error = "change this email"

        return false
    }

    private fun validateProfession(profession: String, layout: TextInputLayout): Boolean {

        return if (profession.isEmpty()) {
            layout.error = "Enter your profession like web dev"
            false
        }
        else {
            layout.error = null
            layout.isErrorEnabled = false
            true
        }

    }

    private fun validatePassword(password: String, layout: TextInputLayout): Boolean {

        return if (password.isEmpty()) {
            layout.error = "Create your password"
            false
        } else if (password.length < 6) {
            layout.error = "password should be more than 6 char"
            false
        } else {
            layout.error = null
            layout.isErrorEnabled = false
            true
        }

    }

    private fun signUp() {

        val nameLayout = findViewById<View>(R.id.etName) as TextInputLayout
        val userNameLayout = findViewById<View>(R.id.createUserName) as TextInputLayout
        val emailLayout = findViewById<View>(R.id.createEmail) as TextInputLayout
        val professionLayout = findViewById<View>(R.id.createProfession) as TextInputLayout
        val passwordLayout = findViewById<View>(R.id.createPassword) as TextInputLayout

        val name = etName.editText?.text.toString().trim().capitalize()
        val userName = createUserName.editText?.text.toString().trim()
        val email = createEmail.editText?.text.toString().trim()
        val profession = createProfession.editText?.text.toString().trim().capitalize()
        val password = createPassword.editText?.text.toString().trim()

        if (!validateFullName(name, nameLayout) || !validateUserName(
                userName,
                userNameLayout
            ) || !validateEmail(email, emailLayout) || !validateProfession(
                profession,
                professionLayout
            ) || !validatePassword(password, passwordLayout)
        ) {
            return
        }

        progressbar_layout_signup.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->

            if (task.isSuccessful) {

                saveUSerInfo(name, userName, email, profession)

            } else {

                val errorCode = (task.exception as FirebaseAuthException?)?.errorCode

                when (errorCode) {
                    "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> {
                        Toast.makeText(
                            this,
                            "An account already exists with the same email address.",
                            Toast.LENGTH_LONG
                        ).show()
                        showError(emailLayout)
                    }
                    "ERROR_EMAIL_ALREADY_IN_USE" -> {
                        Toast.makeText(
                            this,
                            "The email address is already in use by another account.",
                            Toast.LENGTH_LONG
                        ).show()
                        showError(emailLayout)
                    }
                    "ERROR_CREDENTIAL_ALREADY_IN_USE" -> {
                        Toast.makeText(
                            this,
                            "This credential is already associated with a different ConnectCoderUser account.",
                            Toast.LENGTH_LONG
                        ).show()
                        showError(emailLayout)
                    }

                    else -> Toast.makeText(
                        this,
                        "Some Error occurred check your internet connection or try again later.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                auth.signOut()
                progressbar_layout_signup.visibility = View.GONE
            }

        }

    }

    private fun saveUSerInfo(
        name: String,
        userName: String,
        email: String,
        profession: String
    ) {

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "current ConnectCoderUser"
        val bio = "Hey i am a Connect Coder User"
        val profile = "https://firebasestorage.googleapis.com/v0/b/connect-coder.appspot.com/o/Default%20Images%2FDefault%20Images%2Fprofile.png?alt=media&token=0a7029fb-a18c-40f5-9a28-c4d845042f68"

        val user = ConnectCoderUser(currentUserId, name, userName, email, profession, bio, profile)
        val userDao = UserDao()
        userDao.addUsers(user)
        val postDao = UserDao()
        postDao.savePostIds(PostIds())

        progressbar_layout_signup.visibility = View.GONE
        Toast.makeText(this, "Welcome $name", Toast.LENGTH_LONG).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()

    }

    override fun onResume() {

        if (!ConnectionManager().checkConnectivity(this)) {
            checkInterNet(this)
        }

        super.onResume()
    }
}