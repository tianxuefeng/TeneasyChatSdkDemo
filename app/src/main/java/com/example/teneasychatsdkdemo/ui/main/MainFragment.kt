package com.example.teneasychatsdkdemo.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.teneasychatsdkdemo.R
import com.teneasy.sdk.ChatLib
import com.teneasy.sdk.ui.MessageItem
import com.teneasy.sdk.ui.MessageListAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    private lateinit var myTest:ChatLib

    private lateinit var etMsg: EditText

    private lateinit var msgAdapter: MessageListAdapter

    private lateinit var msgList: ArrayList<MessageItem>

    private lateinit var listView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        myTest = ChatLib()
        myTest.sayHello(requireContext())
        myTest.makeConnect(requireContext())

        msgList = ArrayList()


//        myTest.makeConnect2()
        //myTest.m
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etMsg = view.findViewById(R.id.et_msg)
        listView = view.findViewById(R.id.list_view)
        msgAdapter = MessageListAdapter()
        listView.adapter = msgAdapter

        val btnSend: Button = view.findViewById(R.id.btn_send)
        btnSend.setOnClickListener(View.OnClickListener { v:View ->
            if(etMsg.text != null && etMsg.text.isNotEmpty()) {
                val msg = etMsg.text.toString()
                myTest.sendMsg(msg)

                etMsg.text.clear()

                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                val formatted = current.format(formatter)
                val item = MessageItem(true, msg, 0, formatted)
                addMsgItem(item)
            } else {
                Toast.makeText(context, "Please enter the sending content", Toast.LENGTH_LONG).show()
            }
        })

        if(!EventBus.getDefault().isRegistered(MainFragment@this)) {
            EventBus.getDefault().register(MainFragment@this)
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateMsg(data: MessageItem) {
        addMsgItem(data)
    }

    fun addMsgItem(data: MessageItem) {
        msgList.add(data)
        msgAdapter.list = msgList
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