package com.project.connectCoder.fragment

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.project.connectCoder.R
import com.project.connectCoder.adapters.CodeHubPostAdapter
import com.project.connectCoder.adapters.ICodeHubPostAdapter
import com.project.connectCoder.daos.CodeHubDao
import com.project.connectCoder.model.CodeHubPosts
import kotlinx.android.synthetic.main.fragment_code_hub.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CodeHubFragment : Fragment(), ICodeHubPostAdapter {

    private val codeHubPosDao: CodeHubDao = CodeHubDao()
    private lateinit var codeHubPostAdapter: CodeHubPostAdapter
    var myDownloadId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_code_hub, parent, false)

        view.tv_code_hub.setColors(R.color.color_primary, R.color.color_secondary)

        setUpCodeHubRecyclerView(view)

        return view
    }

    private fun setUpCodeHubRecyclerView(view: View?) {
        val codeHubPostCollection = codeHubPosDao.codeHubPostCollection
        val query = codeHubPostCollection.orderBy("createdAt", Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<CodeHubPosts>()
            .setQuery(query, CodeHubPosts::class.java).build()

        val activity = context as Activity

        codeHubPostAdapter = CodeHubPostAdapter(recyclerViewOptions, this)

        view?.codeHubPostRecyclerView?.adapter = codeHubPostAdapter
        view?.codeHubPostRecyclerView?.layoutManager = LinearLayoutManager(activity)

    }

    override fun onStart() {
        super.onStart()
        codeHubPostAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        codeHubPostAdapter.stopListening()
    }

    override fun onCodeHubPostLikeClicked(postId: String) {
        codeHubPosDao.updateCodeHubPostLikes(postId)
    }

    private fun getCodeHubPostById(postId: String): Task<DocumentSnapshot> {
        return codeHubPosDao.codeHubPostCollection.document(postId).get()
    }

    override fun onCodeHubPostDownloadClicked(postId: String) {
        codeHubPosDao.updateCodeHubPostDownloads(postId)

        GlobalScope.launch {
            val user = getCodeHubPostById(postId).await().toObject(CodeHubPosts::class.java)!!
            val link = user.postDownLoadUrl

            val request = DownloadManager.Request(Uri.parse(link))
                .setTitle("Connect Coder file.")
                .setDescription("Your file is downloading...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)

            val dm = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            myDownloadId = dm.enqueue(request)


            val br = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == myDownloadId) {
                        Toast.makeText(
                            context,
                            "Connect Coder File downloaded Successfully, please check your download manager.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            context?.registerReceiver(br, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }
}