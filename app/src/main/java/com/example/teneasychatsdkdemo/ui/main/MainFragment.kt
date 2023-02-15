package com.example.teneasychatsdkdemo.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.utils.widget.MotionButton
import androidx.fragment.app.Fragment
import com.example.teneasychatsdkdemo.BR
import com.teneasy.chatuisdk.*
import com.teneasy.chatuisdk.ui.main.KeFuActivity
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!EventBus.getDefault().isRegistered(MainFragment@this)) {
            EventBus.getDefault().register(MainFragment@this)
        }

        val btnKeFu = view.findViewById<MotionButton>(R.id.btn_send)
        btnKeFu.setOnClickListener({
            val keFuIntent = Intent(requireActivity(), KeFuActivity :: class.java)
            this.startActivity(keFuIntent)
        })
    }

     //fun initView() {

    }


