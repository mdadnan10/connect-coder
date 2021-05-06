package com.project.connectCoder.activity.common

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.project.connectCoder.R
import com.project.connectCoder.util.ConnectionManager
import kotlinx.android.synthetic.main.activity_reset_password.*


class ResetPassword : AppCompatActivity() {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        btnBack.setOnClickListener {
            onBackPressed()
        }

        colorForget.setColors(R.color.color_primary, R.color.color_secondary)

        progressBar_reset.visibility = View.GONE


        if (ConnectionManager().checkConnectivity(this)) {
            btnSubmit.setOnClickListener {
                forgetPassword()
            }
        } else {
            checkInterNet(this)
        }

    }

    private fun validateEmailId(loginId: String, layout: TextInputLayout): Boolean {

        return if (loginId.isEmpty()) {
            layout.error = "Enter your registered email id"
            false
        } else if (!(android.util.Patterns.EMAIL_ADDRESS.matcher(loginId).matches())) {
            layout.error = "enter a valid email id"
            false
        } else {
            layout.error = null
            layout.isErrorEnabled = false
            true
        }

    }

    private fun forgetPassword() {

        val phoneLayout = findViewById<View>(R.id.etForgetEmail) as TextInputLayout
        val email = etForgetEmail.editText?.text.toString().trim()

        if (!validateEmailId(email, phoneLayout)) return

        sendEmail(email)

    }

    private fun sendEmail(email: String) {

        progressBar_reset.visibility = View.VISIBLE

        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->

            if (task.isSuccessful) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Reset your password")
                builder.setMessage("Please check your email $email to reset your password.")
                builder.setPositiveButton("mail") { _, _ ->
                    val mailIntent = Intent()
                    mailIntent.action = Intent.ACTION_SEND
                    mailIntent.putExtra(Intent.EXTRA_TEXT, "check you email")
                    mailIntent.type = "text/plain"
                    try {
                        startActivity(Intent.createChooser(mailIntent, "Choose email/gmail..."))
                    } catch (e: Exception) {
                        Toast.makeText(this, "some error $e", Toast.LENGTH_LONG).show()
                    }
                }
                builder.setNegativeButton("Cancel") { response, _ ->
                    response.dismiss()
                }
                builder.create()
                builder.setCancelable(false)
                builder.show()
                progressBar_reset.visibility = View.GONE
            } else {
                val errorCode = (task.exception as FirebaseAuthException?)?.errorCode

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


                progressBar_reset.visibility = View.GONE
            }

        }
    }

    override fun onResume() {

        if (!ConnectionManager().checkConnectivity(this)) {
            checkInterNet(this)
        }

        super.onResume()
    }

}