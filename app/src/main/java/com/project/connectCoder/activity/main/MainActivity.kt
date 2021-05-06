package com.project.connectCoder.activity.main

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.project.connectCoder.R
import com.project.connectCoder.daos.UserDao
import com.project.connectCoder.fragment.*
import com.project.connectCoder.model.ConnectCoderUser
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val userDao = UserDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val homeFragment = HomeFragment()
        val searchFragment = SearchFragment()
        val codeHubFragment = CodeHubFragment()
        val notificationFragment = NotificationFragment()
        val profileFragment = ProfileFragment()

        setCurrentFragment(homeFragment)

        Handler().postDelayed({
            badgeSetup(R.id.nav_notification, 7)
        }, 2000)

        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val user = userDao.getUserById(currentUserId).await().toObject(ConnectCoderUser::class.java)!!

            withContext(Dispatchers.Main) {
                Glide.with(this@MainActivity).load(user.profile).circleCrop().into(mainProfile)
            }

        }

        bottom_navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {

                R.id.nav_home -> {
                    setCurrentFragment(homeFragment)
                }

                R.id.nav_search -> {
                    setCurrentFragment(searchFragment)
                }

                R.id.nav_code_hub -> {
                    setCurrentFragment(codeHubFragment)
                }

                R.id.nav_notification -> {
                    setCurrentFragment(notificationFragment)
                    badgeClear(R.id.nav_notification)
                }

                R.id.nav_profile -> {
                    setCurrentFragment(profileFragment)
                }

            }

            return@setOnNavigationItemSelectedListener true
        }

    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_layout, fragment)
            commit()
        }
    }

    private fun badgeSetup(id : Int, alerts: Int){

        val badge = bottom_navigation.getOrCreateBadge(id)
        badge.isVisible = true
        badge.number = alerts

    }

    private fun badgeClear(id : Int){
        val badgeDrawable = bottom_navigation.getBadge(id)
        if (badgeDrawable != null){
            badgeDrawable.isVisible = false
            badgeDrawable.clearNumber()
        }

    }

}