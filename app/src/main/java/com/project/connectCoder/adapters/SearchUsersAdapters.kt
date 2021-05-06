package com.project.connectCoder.adapters

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.project.connectCoder.R
import com.project.connectCoder.fragment.ProfileFragment
import com.project.connectCoder.model.ConnectCoderUser

class SearchUsersAdapters(private val list : ArrayList<ConnectCoderUser>) : RecyclerView.Adapter<SearchUsersAdapters.SearchUsersViewHolder>() {

    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUsersViewHolder {
        context = parent.context
        return SearchUsersViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_search_user, parent, false))
    }

    override fun onBindViewHolder(holder: SearchUsersViewHolder, position: Int) {

        val users = list[position]

            holder.userName.text = users.name
            holder.userProfession.text = users.profession
            Glide.with(holder.userImage.context).load(users.profile).into(holder.userImage)

        holder.itemView.setOnClickListener {
            val sharedPreferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            sharedPreferences.putString("userId", users.uid).apply()

            val transaction = context as FragmentActivity
            val ft = transaction.supportFragmentManager.beginTransaction()
            ft.replace(R.id.frame_layout, ProfileFragment())
            ft.addToBackStack("search")
            ft.commit()
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class SearchUsersViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val userImage : ImageView = itemView.findViewById(R.id.searchUserImage)
        val userName : TextView = itemView.findViewById(R.id.searchUserName)
        val userProfession: TextView = itemView.findViewById(R.id.searchUserProfession)

    }

}