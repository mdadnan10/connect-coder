//package com.project.connectCoder.adapters
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.PopupMenu
//import android.widget.TextView
//import android.widget.Toast
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.FragmentActivity
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.firebase.ui.firestore.FirestoreRecyclerAdapter
//import com.firebase.ui.firestore.FirestoreRecyclerOptions
//import com.google.firebase.auth.FirebaseAuth
//import com.project.connectCoder.R
//import com.project.connectCoder.fragment.ProfileFragment
//import com.project.connectCoder.model.RandomPost
//import com.project.connectCoder.util.Utils
//
//class RandomPostAdapter(options: FirestoreRecyclerOptions<RandomPost>, private val listener : IPostAdapter) :
//    FirestoreRecyclerAdapter<RandomPost, RandomPostAdapter.RandomPostViewHolder>(options) {
//
//    lateinit var context : Context
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RandomPostViewHolder {
//        val viewHolder =  RandomPostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_random_post, parent, false))
//        context = parent.context
//        viewHolder.likeBtn.setOnClickListener {
//            listener.onLikeClicked(snapshots.getSnapshot(viewHolder.adapterPosition).id)
//        }
//        return viewHolder
//    }
//
//    override fun onBindViewHolder(holder: RandomPostViewHolder, position: Int, model: RandomPost) {
//
//        holder.userName.text = model.name
//        holder.postMessage.text = model.text
//        Glide.with(holder.userImage.context).load(model.profile).circleCrop().into(holder.userImage)
//        holder.likeCount.text = model.likedBy.size.toString()
//        holder.createdAt.text = Utils.getTimeAgo(model.createdAt)
//
//        val auth = FirebaseAuth.getInstance()
//        val currentUserId = auth.currentUser!!.uid
//        val isLiked =  model.likedBy.contains(currentUserId)
//        val editAble = model.uid
//
//        holder.userName.setOnClickListener {
//            val sharedPreferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
//            sharedPreferences.putString("userId", model.uid).apply()
//
//            val transaction = context as FragmentActivity
//
//            transaction.supportFragmentManager.beginTransaction().replace(R.id.frame_layout, ProfileFragment()).commit()
//        }
//
//        holder.options.setOnClickListener { view ->
//
//            val menu = PopupMenu(context, view)
//
//            if (currentUserId == editAble) {
//                menu.menuInflater.inflate(R.menu.settings_edit, menu.menu)
//                menu.show()
//                menu.setOnMenuItemClickListener {
//                    when (it.itemId) {
//                        R.id.edit_post ->
//                            Toast.makeText(context, "Edit item clicked on random Post", Toast.LENGTH_SHORT).show()
//
//                        R.id.delete_post ->
//                            Toast.makeText(context, "Random Post deleted", Toast.LENGTH_SHORT)
//                                .show()
//
//                    }
//
//                    return@setOnMenuItemClickListener true
//                }
//            }
//            else {
//                menu.menuInflater.inflate(R.menu.setting_report, menu.menu)
//                menu.show()
//                menu.setOnMenuItemClickListener { it ->
//                    when(it.itemId) {
//                        R.id.report ->
//                            Toast.makeText(context, "Thank for reporting we will refer this post to our t&c.", Toast.LENGTH_LONG).show()
//                    }
//
//                    return@setOnMenuItemClickListener true
//                }
//            }
//        }
//
//        if (isLiked) {
//            holder.likeBtn.setImageDrawable(
//                ContextCompat.getDrawable(
//                    holder.likeBtn.context,
//                    R.drawable.ic_liked
//                )
//            )
//        }
//        else {
//            holder.likeBtn.setImageDrawable(
//                ContextCompat.getDrawable(
//                    holder.likeBtn.context,
//                    R.drawable.ic_notification_outlined
//                )
//            )
//        }
//
//    }
//
//    class RandomPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//
//        val userImage: ImageView = itemView.findViewById(R.id.randomPostUserImage)
//        val userName: TextView = itemView.findViewById(R.id.tvRandomPostUserName)
//        val createdAt: TextView = itemView.findViewById(R.id.randomPostCreatedAt)
//        val postMessage: TextView = itemView.findViewById(R.id.randomPostMessage)
//        val likeBtn: ImageView = itemView.findViewById(R.id.randomLikeBtn)
//        val likeCount: TextView = itemView.findViewById(R.id.randomLikeCount)
//        val options : ImageView = itemView.findViewById(R.id.randomPostOptions)
//
//    }
//
//}
//
//interface IPostAdapter{
//    fun onLikeClicked(postId : String)
//}