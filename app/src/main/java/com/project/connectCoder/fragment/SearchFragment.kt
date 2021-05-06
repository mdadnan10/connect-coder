package com.project.connectCoder.fragment

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.Query
import com.project.connectCoder.R
import com.project.connectCoder.adapters.SearchUsersAdapters
import com.project.connectCoder.daos.UserDao
import com.project.connectCoder.model.ConnectCoderUser
import kotlinx.android.synthetic.main.fragment_search.view.*
import java.util.*
import kotlin.collections.ArrayList

class SearchFragment : Fragment() {

    private val userDao: UserDao = UserDao()
    private lateinit var searchUsersAdapter: SearchUsersAdapters
    lateinit var users: ArrayList<ConnectCoderUser>

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, parent, false)

        setUpSearchUsersRecyclerView(view)
        readUsers(view)
        view?.et_search_users?.addTextChangedListener(textWatcher)

        return view
    }

    private fun setUpSearchUsersRecyclerView(view: View?) {

        val activity = context as Activity

        view?.searchUserRecyclerView?.setHasFixedSize(true)
        view?.searchUserRecyclerView?.layoutManager = LinearLayoutManager(activity)

        users = ArrayList()
        searchUsersAdapter = SearchUsersAdapters(users)
        view?.searchUserRecyclerView?.adapter = searchUsersAdapter

    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            getSpecificUSer(s.toString().capitalize())
        }

        override fun afterTextChanged(s: Editable?) {

        }

    }

    private fun getSpecificUSer(name: String) {
        val usersCollection = userDao.usersCollection
        val query = usersCollection.orderBy("name").startAt(name)
            .endAt("$name\uf8ff")

        query.addSnapshotListener { value, error ->
            users.clear()

            for (dc in value!!.documentChanges) {
                val user = dc.document.toObject(ConnectCoderUser::class.java)
                users.add(user)
            }

            searchUsersAdapter.notifyDataSetChanged()
        }

    }

    private fun readUsers(view: View?) {
        val ref = userDao.usersCollection

        ref.addSnapshotListener { value, error ->

            if (view?.et_search_users?.text.toString().trim() == "") {
                users.clear()

                for (data in value!!.documentChanges) {

                    val user = data.document.toObject(ConnectCoderUser::class.java)
                    users.add(user)

                }
                searchUsersAdapter.notifyDataSetChanged()

            }

        }

    }

}