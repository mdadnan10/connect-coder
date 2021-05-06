package com.project.connectCoder.activity.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.project.connectCoder.R
import com.project.connectCoder.daos.UserDao
import com.project.connectCoder.model.ConnectCoderUser
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_select_users.*
import kotlinx.android.synthetic.main.item_search_user.view.*

class SelectUsers : AppCompatActivity() {
    lateinit var userDao: UserDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_users)
        tv_select_users.setColors(R.color.color_primary, R.color.color_secondary)
        btn_back.setOnClickListener {
            onBackPressed()
            finish()
        }

        fetchUsers()
    }

    companion object{
        const val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers() {
        userDao = UserDao()
        val userCollection = userDao.usersCollection
        userCollection.addSnapshotListener { value, _ ->
            val adapter = GroupAdapter<GroupieViewHolder>()

            value?.forEach{
                val users = it.toObject(ConnectCoderUser::class.java)
                adapter.add(UserItems(users))
            }
            adapter.setOnItemClickListener { item, view ->
                val user = item as UserItems
                val intent = Intent(view.context, ChatLogActivity::class.java)
                intent.putExtra(USER_KEY, user.user)
                startActivity(intent)
                finish()
            }
          selectUserRecyclerView.adapter = adapter
        }

    }
}

class UserItems(val user : ConnectCoderUser) : Item<GroupieViewHolder>(){

    override fun getLayout(): Int {
        return  R.layout.item_search_user
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.searchUserName.text = user.name
        viewHolder.itemView.searchUserProfession.text = user.profession
        Glide.with(viewHolder.itemView.searchUserImage.context).load(user.profile).into(viewHolder.itemView.searchUserImage)
    }
}