package com.project.connectCoder.util

import android.content.Context
import android.net.ConnectivityManager

class ConnectionManager {

    fun checkConnectivity(context: Context) : Boolean{

        val connectionManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectionManager.activeNetworkInfo

        return if (activeNetwork?.isConnected != null)
            activeNetwork.isConnected
        else false

    }

}