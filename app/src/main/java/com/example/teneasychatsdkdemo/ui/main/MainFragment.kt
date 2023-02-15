package com.example.teneasychatsdkdemo.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.utils.widget.MotionButton
import androidx.fragment.app.Fragment
import com.example.teneasychatsdkdemo.BR
import com.example.teneasychatsdkdemo.R
import com.teneasy.chatuisdk.*
import com.teneasy.sdk.MessageEventBus
import com.teneasy.sdk.ui.MessageItem
import gateway.GGateway.SCHi
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class MainFragment : Fragment(){

    companion object {
        fun newInstance() = MainFragment()
    }


    private lateinit var timer: Timer

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//       // val btnKeFu =
//
//
//    }

     //fun initView() {

    }


