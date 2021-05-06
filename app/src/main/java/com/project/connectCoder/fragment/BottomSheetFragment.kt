package com.project.connectCoder.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.project.connectCoder.R
import com.project.connectCoder.activity.insideMain.CodeHubPostActivity
import com.project.connectCoder.activity.insideMain.FeedPostActivity
//import com.project.connectCoder.activity.insideMain.RandomPostActivity
import kotlinx.android.synthetic.main.bottom_sheet_fragment.view.*


@SuppressLint("ResourceType")
class BottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_fragment, container, false)

        val activity = context as Activity

        view.btn_feed_post.setOnClickListener {
            val intent = Intent(activity, FeedPostActivity::class.java)
            startActivity(intent)
        }

        view.btn_code_hub_post.setOnClickListener {
            val intent = Intent(activity, CodeHubPostActivity::class.java)
            startActivity(intent)
        }

//        view.btn_random.setOnClickListener {
//
//            val intent = Intent(activity, RandomPostActivity::class.java)
//            startActivity(intent)
//        }

        return view
    }

}

