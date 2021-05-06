package com.project.connectCoder.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.project.connectCoder.R
import com.project.connectCoder.activity.common.LoginActivity
import com.project.connectCoder.activity.insideMain.EditProfileActivity
import com.project.connectCoder.activity.messages.ChatLogActivity
import com.project.connectCoder.adapters.ProfileCodeHubAdapter
import com.project.connectCoder.adapters.ProfileFeedAdapter
import com.project.connectCoder.daos.CodeHubDao
import com.project.connectCoder.daos.FeedPostDao
import com.project.connectCoder.daos.UserDao
import com.project.connectCoder.model.CodeHubPosts
import com.project.connectCoder.model.ConnectCoderUser
import com.project.connectCoder.model.FeedPost
import com.project.connectCoder.util.ImagePicker
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class ProfileFragment : Fragment() {

    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser!!.uid
    lateinit var profileId: String
    private val userDao = UserDao()
    lateinit var profileView: View
    lateinit var postList: ArrayList<FeedPost>
    var codeHubList: ArrayList<CodeHubPosts>? = null
    lateinit var profileFeedPostAdapter: ProfileFeedAdapter
    lateinit var profileCodeHubAdapter: ProfileCodeHubAdapter
    private lateinit var sendMessageUser: ConnectCoderUser

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, parent, false)
        view.tv_profile_user_name.setColors(R.color.color_primary, R.color.color_secondary)

        val activity = context as Activity
        profileView = view
        val sharedPreferences = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)

        if (sharedPreferences != null)
            profileId = sharedPreferences.getString("userId", currentUserId)!!

        if (profileId == currentUserId) {
            view.btn_edit_profile.text = "EDIT PROFILE"
        } else {
            view.btn_add_everything.visibility = View.GONE
            view.btn_open_drawer.visibility = View.GONE
            view.btn_more_profile.visibility = View.VISIBLE
            view.btn_edit_profile.text = "MESSAGE"
        }

        getActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val bottomSheetFragment = BottomSheetFragment()
        view.profile_bg.setImageResource(ImagePicker.getImage())

        view.btn_add_everything.setOnClickListener {
            fragmentManager?.let { it1 -> bottomSheetFragment.show(it1, "Bottom Sheet Fragment") }

        }

        view.btn_more_profile.setOnClickListener { v ->
            val menu = PopupMenu(context, v)
            menu.menuInflater.inflate(R.menu.setting_report, menu.menu)
            menu.show()

            menu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.report ->
                        Toast.makeText(
                            context,
                            "Thanks for reporting we will refer this account to our t&c.",
                            Toast.LENGTH_LONG
                        ).show()
                }
                return@setOnMenuItemClickListener true
            }
        }

        view.btn_edit_profile.setOnClickListener {
            val text = btn_edit_profile.text.toString()

            if (text == "Edit Profile" || profileId == currentUserId) {
                val intent = Intent(activity, EditProfileActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(activity, ChatLogActivity::class.java)
                intent.putExtra(USER_KEY, sendMessageUser)
                startActivity(intent)
            }
        }

        view.btnProfileFeed.setOnClickListener {
            view?.profileFeedPost?.visibility = View.VISIBLE
            view?.profileCodeHubPost?.visibility = View.GONE
            view?.feed_active?.visibility = View.VISIBLE
            view.code_hub_active?.visibility = View.INVISIBLE
            setUpProfileFeedPostRecyclerView(view)
            myFeedPosts()
        }

        view.btnProfileCodeHub.setOnClickListener {
            view?.profileFeedPost?.visibility = View.GONE
            view?.profileCodeHubPost?.visibility = View.VISIBLE
            view?.feed_active?.visibility = View.INVISIBLE
            view.code_hub_active?.visibility = View.VISIBLE
            setUpCodeHubPostRecyclerView(view)
            myCodeHubPosts()
        }

        setUpProfileFeedPostRecyclerView(view)
        myFeedPosts()

        showData(view)

        return view
    }

    companion object {
        const val USER_KEY = "USER_KEY"
    }

    private fun setUpProfileFeedPostRecyclerView(view: View?) {

        val context = context as Activity

        view?.profileFeedPost?.isNestedScrollingEnabled = false

        view?.profileFeedPost?.setHasFixedSize(true)
        view?.profileFeedPost?.layoutManager = LinearLayoutManager(context)
        postList = ArrayList()
        profileFeedPostAdapter = ProfileFeedAdapter(postList)
        view?.profileFeedPost?.adapter = profileFeedPostAdapter

    }

    private fun setUpCodeHubPostRecyclerView(view: View?) {
        val context = context as Activity

        view?.profileCodeHubPost?.isNestedScrollingEnabled = false

        view?.profileCodeHubPost?.setHasFixedSize(true)
        view?.profileCodeHubPost?.layoutManager = LinearLayoutManager(context)
        codeHubList = ArrayList()
        profileCodeHubAdapter = ProfileCodeHubAdapter(codeHubList!!)
        view?.profileCodeHubPost?.adapter = profileCodeHubAdapter
    }

    private fun myFeedPosts() {
        val postCollection = FeedPostDao().feedPostCollection

        postCollection.addSnapshotListener { value, error ->
            postList.clear()

            value?.forEach {
                val post = it.toObject(FeedPost::class.java)
                if (post.uid == profileId) {
                    postList.add(post)
                }
            }
            profileFeedPostAdapter.notifyDataSetChanged()
        }

    }

    private fun myCodeHubPosts() {
        val postCollection = CodeHubDao().codeHubPostCollection

        postCollection.addSnapshotListener { value, error ->
            codeHubList?.clear()

            value?.forEach {
                val post = it.toObject(CodeHubPosts::class.java)
                if (post.uid == profileId)
                    codeHubList?.add(post)
            }
            profileCodeHubAdapter.notifyDataSetChanged()
        }
    }

    private fun showData(view: View) {

        view.progressLayoutProfile.visibility = View.VISIBLE

        GlobalScope.launch(Dispatchers.IO) {
            val user =
                userDao.getUserById(profileId).await().toObject(ConnectCoderUser::class.java)!!
            withContext(Dispatchers.Main) {
                displayData(user, view)
            }
        }
    }

    private fun displayData(connectCoderUser: ConnectCoderUser, view: View) {

        view.tv_profile_user_name.text = connectCoderUser.userName
        Glide.with(context!!).load(connectCoderUser.profile).centerCrop().into(view.profile_image)
        view.profile_full_name.text = connectCoderUser.name
        view.profile_bio.text = connectCoderUser.bio
        if (codeHubList === null) view.tv_post_count.text = (postList.size + 0).toString()
        else view.tv_post_count.text = (postList.size + codeHubList!!.size).toString()
        view.tv_profession.text = connectCoderUser.profession

        sendMessageUser = connectCoderUser

        view.progressLayoutProfile.visibility = View.INVISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        btn_open_drawer.setOnClickListener {
            mainDrawer.openDrawer(GravityCompat.END)
        }

        val `this` = context as Activity

        navigationView.setNavigationItemSelectedListener {

            when (it.itemId) {

                R.id.btn_share -> {
                    Toast.makeText(`this`, "Share clicked", Toast.LENGTH_LONG).show()
                    mainDrawer.closeDrawers()
                }

                R.id.btn_faqs -> {
                    Toast.makeText(`this`, "FAQs clicked", Toast.LENGTH_LONG).show()
                    mainDrawer.closeDrawers()
                }

                R.id.btn_rate_us -> {
                    Toast.makeText(`this`, "Rate us clicked", Toast.LENGTH_LONG).show()
                    mainDrawer.closeDrawers()
                }

                R.id.btn_follow_us -> {
                    Toast.makeText(`this`, "Follow us clicked", Toast.LENGTH_LONG).show()
                    mainDrawer.closeDrawers()
                }

                R.id.btn_logout -> {
                    val builder = AlertDialog.Builder(`this`)
                    builder.setTitle("Confirm Sign Out")
                    builder.setMessage("You are signing out of your Connect Coder app on this device.")
                    builder.setPositiveButton("Sign Out") { _, _ ->
                        auth.signOut()
                        val intent = Intent(`this`, LoginActivity::class.java)
                        startActivity(intent)
                        `this`.finish()
                    }
                    builder.setNegativeButton("Cancel") { response, _ ->
                        response.dismiss()
                    }
                    builder.create()
                    builder.setCancelable(false)
                    builder.show()
                    mainDrawer.closeDrawers()
                }

            }

            return@setNavigationItemSelectedListener true
        }

    }

    override fun onResume() {
        super.onResume()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        showData(profileView)
    }

    override fun onStart() {
        super.onStart()
        val sharedPreferences = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        sharedPreferences?.putString("userId", currentUserId)?.apply()
    }

    override fun onStop() {
        super.onStop()
        val sharedPreferences = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        sharedPreferences?.putString("userId", currentUserId)?.apply()
    }

    override fun onPause() {
        super.onPause()
        val sharedPreferences = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        sharedPreferences?.putString("userId", currentUserId)?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val sharedPreferences = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        sharedPreferences?.putString("userId", currentUserId)?.apply()
    }

}