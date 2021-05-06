package com.project.connectCoder.activity.common

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.project.connectCoder.R
import com.project.connectCoder.activity.main.MainActivity
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash)

        tvLogo.setColors(R.color.color_primary, R.color.color_secondary)
        appSlogan.setColors(R.color.color_primary1, R.color.color_primary)

        Handler().postDelayed({
            if (auth.currentUser != null) {

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()

            } else {

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()

            }
        }, 1000)

    }
}
