package com.example.teneasychatsdkdemo.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.teneasychatsdkdemo.R
import com.teneasy.sdk.ChatLib

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    private lateinit var myTest:ChatLib

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
        val btnSend: Button = view.findViewById(R.id.btn_send)
        btnSend.setOnClickListener(View.OnClickListener { v:View ->
            myTest.sendMsg("android 测试")
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

}