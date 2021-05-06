package com.project.connectCoder.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.project.connectCoder.R
import com.project.connectCoder.activity.messages.MessageActivity
import com.project.connectCoder.adapters.FeedPostAdapter
import com.project.connectCoder.adapters.IFeedPostAdapter
//import com.project.connectCoder.adapters.IPostAdapter
//import com.project.connectCoder.adapters.RandomPostAdapter
import com.project.connectCoder.daos.FeedPostDao
//import com.project.connectCoder.daos.RandomDao
import com.project.connectCoder.model.FeedPost
//import com.project.connectCoder.model.RandomPost
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment(), IFeedPostAdapter {

//    private lateinit var randomPostDao: RandomDao
//    private lateinit var randomPostAdapter: RandomPostAdapter
    private lateinit var feedPostDao: FeedPostDao
    private lateinit var feedPostAdapter: FeedPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, parent, false)

        val activity = context as Activity

        view.tvLogo.setColors(R.color.color_primary, R.color.color_secondary)

        val bottomSheetFragment = BottomSheetFragment()

        view.btn_add_everything.setOnClickListener {

            fragmentManager?.let { it1 -> bottomSheetFragment.show(it1, "Bottom Sheet Fragment") }

        }

        view.btn_message.setOnClickListener {
            startActivity(Intent(activity, MessageActivity::class.java))
        }

//        setUpRandomPostRecyclerView(view)
        setUpFeedPostRecyclerView(view)

        return view
    }

//    private fun setUpRandomPostRecyclerView(view: View?) {
//
//        randomPostDao = RandomDao()
//        val postCollections = randomPostDao.postCollection
//        val query = postCollections.orderBy("createdAt", Query.Direction.DESCENDING)
//        val recyclerViewOption = FirestoreRecyclerOptions.Builder<RandomPost>().setQuery(query, RandomPost::class.java).build()
//
//        val activity = context as Activity
//
//        randomPostAdapter = RandomPostAdapter(recyclerViewOption, this)
//
//        view?.randomPostRecyclerView?.adapter = randomPostAdapter
//        view?.randomPostRecyclerView?.layoutManager = LinearLayoutManager(activity)
//
//
//    }

    private fun setUpFeedPostRecyclerView(view: View?) {
        feedPostDao = FeedPostDao()
        val feedPostCollection = feedPostDao.feedPostCollection
        val query = feedPostCollection.orderBy("createdAt", Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<FeedPost>().setQuery(query, FeedPost::class.java).build()

        val activity = context as Activity

        feedPostAdapter = FeedPostAdapter(recyclerViewOptions, this)

        view?.feedPostRecyclerView?.adapter = feedPostAdapter
        view?.feedPostRecyclerView?.layoutManager = LinearLayoutManager(activity)
    }

    override fun onStart() {
        super.onStart()
//        randomPostAdapter.startListening()
        feedPostAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
//        randomPostAdapter.stopListening()
        feedPostAdapter.startListening()
    }

//    override fun onLikeClicked(postId: String) {
//        randomPostDao.updateRandomPostLikes(postId)
//    }

    override fun onFeedPostLikeClicked(postId: String) {
        feedPostDao.updateFeedPostLikes(postId)
    }

}
