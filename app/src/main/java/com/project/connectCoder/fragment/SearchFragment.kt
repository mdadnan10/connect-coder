package com.project.connectCoder.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.project.connectCoder.R
import com.project.connectCoder.adapters.SearchUsersAdapters
import com.project.connectCoder.daos.UserDao
import com.project.connectCoder.model.ConnectCoderUser
import kotlinx.android.synthetic.main.fragment_search.view.*

class SearchFragment : Fragment() {

    private lateinit var userDao: UserDao
    private val auth = FirebaseAuth.getInstance()
    private lateinit var searchUsersAdapter: SearchUsersAdapters

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, parent, false)

        setUpSearchUsersRecyclerView(view)

        return view
    }

    private fun setUpSearchUsersRecyclerView(view: View?) {
        userDao = UserDao()
        val usersCollection = userDao.usersCollection
        val query = usersCollection.orderBy("uid", Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<ConnectCoderUser>()
            .setQuery(query, ConnectCoderUser::class.java).build()

        val activity = context as Activity

        searchUsersAdapter = SearchUsersAdapters(recyclerViewOptions)

        view?.searchUserRecyclerView?.adapter = searchUsersAdapter
        view?.searchUserRecyclerView?.layoutManager = LinearLayoutManager(activity)

    }

    override fun onStart() {
        super.onStart()
        searchUsersAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        searchUsersAdapter.stopListening()
    }

}