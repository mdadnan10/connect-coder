package com.project.connectCoder.activity.common

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.project.connectCoder.R
import com.project.connectCoder.activity.main.MainActivity
//import com.project.connectCoder.daos.RandomDao
import com.project.connectCoder.daos.UserDao
import com.project.connectCoder.model.PostIds
import com.project.connectCoder.model.ConnectCoderUser
import com.project.connectCoder.util.ConnectionManager
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

fun checkInterNet(context: Activity) {

    val dialog = AlertDialog.Builder(context)
    dialog.setTitle("Turn on mobile data")
    dialog.setMessage("Enable mobile data to connect with Coders.")
    dialog.setPositiveButton("Enable") { _, _ ->

        val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        context.startActivity(settingsIntent)
        context.finish()

    }
    dialog.setNegativeButton("Exit") { _, _ ->
        ActivityCompat.finishAffinity(context)
    }
    dialog.setCancelable(false)
    dialog.create()
    dialog.show()

}

class LoginActivity : AppCompatActivity() {

    private val RC_SIGN_IN: Int = 123
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        colorLogin.setColors(R.color.color_primary, R.color.color_secondary)
        colorSignUp.setColors(R.color.color_primary, R.color.color_secondary)

        colorSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        progressbar_layout_login.visibility = View.GONE

        if (ConnectionManager().checkConnectivity(this)) {

            forgetPassword.setOnClickListener {
                val intent = Intent(this, ResetPassword::class.java)
                startActivity(intent)
            }

            btnLogin.setOnClickListener {
                login()
            }


            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)

            btnSignUpGoogle.setOnClickListener {
                signIn()
            }
        } else {

            checkInterNet(this)

        }

    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        progressbar_layout_login.visibility = View.VISIBLE

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {

                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)

            } catch (e: ApiException) {
                Toast.makeText(
                    this,
                    "Google Sign in Failed please try again after some time.",
                    Toast.LENGTH_LONG
                ).show()
                progressbar_layout_login.visibility = View.GONE
            }
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        progressbar_layout_login.visibility = View.VISIBLE

        GlobalScope.launch(Dispatchers.IO) {
            val mAuth = auth.signInWithCredential(credential).await()
            val firebaseUser = auth.currentUser
            withContext(Dispatchers.Main) {
                saveUSerInfo(firebaseUser)
            }
        }

        auth.signInWithCredential(credential)
            .addOnFailureListener(this) { task ->

                val errorCode = (task as FirebaseAuthException?)?.errorCode

                when (errorCode) {
                    "ERROR_INVALID_CREDENTIAL" -> Toast.makeText(
                        this,
                        "Your account may have expired please contact admin.",
                        Toast.LENGTH_LONG
                    ).show()
                    "ERROR_USER_DISABLED" -> Toast.makeText(
                        this,
                        "The ConnectCoderUser account has been disabled by an administrator.",
                        Toast.LENGTH_LONG
                    ).show()
                    "ERROR_USER_NOT_FOUND" -> Toast.makeText(
                        this,
                        "there is no such ConnectCoderUser exist.",
                        Toast.LENGTH_LONG
                    ).show()


                    else -> Toast.makeText(
                        this,
                        "Some Error occurred check your internet connection or try again later.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                progressbar_layout_login.visibility = View.GONE
            }
    }

    private fun saveUSerInfo(user: FirebaseUser?) {

        val currentUserId = auth.currentUser?.uid ?: "current ConnectCoderUser"
        val name = user?.displayName ?: "ConnectCoderUser"
        val userName = "update ConnectCoderUser Name"
        val email = user?.email ?: "ccuser@gmail.com"
        val profession = "Developer"
        val bio = "Hey i am a Connect Coder User"
        val profile = user?.photoUrl.toString()

        progressbar_layout_login.visibility = View.VISIBLE


        val userDao = UserDao()
        val userData = userDao.usersCollection.document(currentUserId).get()

        userData.addOnCompleteListener {

            if (it.result?.exists() == true) {

                Toast.makeText(this, "Welcome Back $name", Toast.LENGTH_LONG).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()

            } else {

                if (user != null) {
                    val currentUser =
                        ConnectCoderUser(currentUserId, name, userName, email, profession, bio, profile)
                    userDao.addUsers(currentUser)
                    val postDao = UserDao()
                    postDao.savePostIds(PostIds())

                    Toast.makeText(this, "Welcome $name", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(
                        this,
                        "Some error occurred while saving your data, please try after some time.",
                        Toast.LENGTH_LONG
                    ).show()
                    auth.signOut()
                    progressbar_layout_login.visibility = View.GONE
                }
            }


        }

    }

    private fun validateEmail(loginId: String, layout: TextInputLayout): Boolean {

        return if (loginId.isEmpty()) {
            layout.error = "Enter your registered email"
            false
        } else if (!(android.util.Patterns.EMAIL_ADDRESS.matcher(loginId).matches())) {
            layout.error = "enter a valid email"
            false
        } else {
            layout.error = null
            layout.isErrorEnabled = false
            true
        }

    }

    private fun validatePassword(password: String, layout: TextInputLayout): Boolean {

        return if (password.isEmpty()) {
            layout.error = "Enter your Password"
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

    private fun login() {

        val emailLayout = findViewById<View>(R.id.loginUserEmailId) as TextInputLayout
        val passwordLayout = findViewById<View>(R.id.loginPassword) as TextInputLayout


        val loginId = emailLayout.editText?.text.toString().trim()
        val password = loginPassword.editText?.text.toString().trim()

        if (!validateEmail(loginId, emailLayout) || !validatePassword(
                password,
                passwordLayout
            )
        ) return

        progressbar_layout_login.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(loginId, password).addOnCompleteListener { task ->

            if (task.isSuccessful) {
                progressbar_layout_login.visibility = View.GONE
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                Toast.makeText(this, "welcome Back", Toast.LENGTH_LONG).show()
                finish()
            } else {
                progressbar_layout_login.visibility = View.GONE

                val errorCode = (task.exception as FirebaseAuthException?)?.errorCode

                when (errorCode) {
                    "ERROR_INVALID_CREDENTIAL" -> Toast.makeText(
                        this,
                        "Your account may have expired please contact admin.",
                        Toast.LENGTH_LONG
                    ).show()
                    "ERROR_WRONG_PASSWORD" ->
                        Toast.makeText(
                            this,
                            "Email and password does't match, or ConnectCoderUser don't have password.",
                            Toast.LENGTH_LONG
                        ).show()

                    "ERROR_USER_DISABLED" -> Toast.makeText(
                        this,
                        "The ConnectCoderUser account has been disabled by an administrator.",
                        Toast.LENGTH_LONG
                    ).show()

                    "ERROR_USER_TOKEN_EXPIRED" -> Toast.makeText(
                        this,
                        "The ConnectCoderUser\'s credential is no longer valid. The ConnectCoderUser must sign Up again.",
                        Toast.LENGTH_LONG
                    ).show()

                    "ERROR_USER_NOT_FOUND" -> Toast.makeText(
                        this,
                        "there is no such ConnectCoderUser exist.",
                        Toast.LENGTH_LONG
                    ).show()
                    else ->
                        Toast.makeText(
                            this,
                            "Some Error occurred check your internet connection or try again later.",
                            Toast.LENGTH_LONG
                        ).show()
                }

            }

        }

    }

    override fun onBackPressed() {
        finishAffinity()
    }

    override fun onResume() {

        if (!ConnectionManager().checkConnectivity(this)) {
            checkInterNet(this)
        }
        super.onResume()
    }
}