package com.example.teneasychatsdkdemo.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.teneasychatsdkdemo.R
import com.teneasy.sdk.CallbackMsg
import com.teneasy.sdk.ChatLib
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    private lateinit var myTest:ChatLib

    private lateinit var tvMsg: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        myTest = ChatLib()
        myTest.sayHello(requireContext())
        myTest.makeConnect(requireContext())

//        myTest.makeConnect2()
        //myTest.m
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvMsg = view.findViewById(R.id.message)
        val btnSend: Button = view.findViewById(R.id.btn_send)
        btnSend.setOnClickListener(View.OnClickListener { v:View ->
            myTest.sendMsg("android 测试")
        })

        if(!EventBus.getDefault().isRegistered(MainFragment@this)) {
            EventBus.getDefault().register(MainFragment@this)
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateMsg(data: CallbackMsg) {
        tvMsg.text = data.msg
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        if(!EventBus.getDefault().isRegistered(MainFragment@this)) {
            EventBus.getDefault().unregister(MainFragment@this)
        }
    }
}