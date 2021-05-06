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

class SearchUsersAdapters(options : FirestoreRecyclerOptions<ConnectCoderUser>) :
    FirestoreRecyclerAdapter<ConnectCoderUser, SearchUsersAdapters.SearchUsersViewHolder>(options) {

    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUsersViewHolder {
        val viewHolder = SearchUsersViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_search_user, parent, false))
        context = parent.context
        return viewHolder
    }

    override fun onBindViewHolder(holder: SearchUsersViewHolder, position: Int, model: ConnectCoderUser) {

            holder.userName.text = model.name
            holder.userProfession.text = model.profession
            Glide.with(holder.userImage.context).load(model.profile).into(holder.userImage)

        holder.itemView.setOnClickListener {
            val sharedPreferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            sharedPreferences.putString("userId", model.uid).apply()

            val transaction = context as FragmentActivity
            val ft = transaction.supportFragmentManager.beginTransaction()
            ft.replace(R.id.frame_layout, ProfileFragment())
            ft.addToBackStack("search")
            ft.commit()
        }

    }

    class SearchUsersViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val userImage : ImageView = itemView.findViewById(R.id.searchUserImage)
        val userName : TextView = itemView.findViewById(R.id.searchUserName)
        val userProfession: TextView = itemView.findViewById(R.id.searchUserProfession)

    }
}