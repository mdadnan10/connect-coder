package com.project.connectCoder.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.connectCoder.R
import com.project.connectCoder.adapters.NotificationAdapter
import com.project.connectCoder.model.Notification
import kotlinx.android.synthetic.main.fragment_notification.view.*

class NotificationFragment : Fragment() {

    lateinit var notificationAdapter: NotificationAdapter
    lateinit var notificationList: ArrayList<Notification>
    val currentUserId = FirebaseAuth.getInstance().currentUser.uid
    private val database = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_notification, parent, false)

        view.tv_notification.setColors(R.color.color_primary, R.color.color_secondary)

        setUpRecyclerView(view)
        readNotifications()

        return view
    }

    private fun setUpRecyclerView(view: View?) {

        val activity = context as Activity

        view?.notificationRecyclerView?.setHasFixedSize(true)
        view?.notificationRecyclerView?.layoutManager = LinearLayoutManager(activity)
        notificationList = ArrayList()
        if (notificationList != null) {
            notificationAdapter = NotificationAdapter(notificationList)
        }
        else Toast.makeText(activity, "There is no notification.", Toast.LENGTH_LONG).show()
        view?.notificationRecyclerView?.adapter = notificationAdapter

    }

    private fun readNotifications() {
        val ref = database.collection("notification/posts/$currentUserId")

        ref.addSnapshotListener { value, error ->
            notificationList.clear()

            value?.forEach {

                val notification = it.toObject(Notification::class.java)
                notificationList.add(notification)

            }

            notificationAdapter.notifyDataSetChanged()
        }

    }
}