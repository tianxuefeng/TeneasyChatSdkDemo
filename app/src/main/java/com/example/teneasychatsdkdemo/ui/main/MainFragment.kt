package com.example.teneasychatsdkdemo.ui.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.teneasychatsdkdemo.R
import com.teneasy.sdk.ChatLib
import com.teneasy.sdk.ui.MessageItem
import com.teneasy.sdk.ui.MessageListAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.collections.ArrayList

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    private lateinit var chatLib:ChatLib

    private lateinit var etMsg: EditText

    private lateinit var msgAdapter: MessageListAdapter

    private lateinit var msgList: ArrayList<MessageItem>

    private lateinit var listView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        chatLib = ChatLib()
        //myTest.sayHello(requireContext())
        chatLib.makeConnect(requireContext())

        msgList = ArrayList()


//        myTest.makeConnect2()
        //myTest.m
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etMsg = view.findViewById(R.id.et_msg)
        etMsg.onFocusChangeListener = View.OnFocusChangeListener { v: View, hasFocus: Boolean ->
            if (!hasFocus) {
                closeSoftKeyboard(v)
            }
        }
        listView = view.findViewById(R.id.list_view)
        msgAdapter = MessageListAdapter()
        listView.adapter = msgAdapter

        val btnSend: Button = view.findViewById(R.id.btn_send)
        btnSend.setOnClickListener(View.OnClickListener { v:View ->
            if(etMsg.text != null && etMsg.text.isNotEmpty()) {
                closeSoftKeyboard(etMsg)
                val msg = etMsg.text.toString()
                chatLib.sendMsg(msg)

                etMsg.text.clear()

                /*val cMsg = CMessage.Message.newBuilder()
                var cMContent = MessageContent.newBuilder()
                cMContent.setData("测试消息")
                cMsg.setContent(cMContent)
                print(cMsg.content.data)*/

                //val item = MessageItem(true, msg, 0, TimeUtil.getTimeStringAutoShort2(Date(), true))

                if (chatLib.sendingMessageItem != null) {
                    addMsgItem(chatLib.sendingMessageItem!!)
                }
            } else {
                Toast.makeText(context, "Please enter the sending content", Toast.LENGTH_LONG).show()
            }
        })

        if(!EventBus.getDefault().isRegistered(MainFragment@this)) {
            EventBus.getDefault().register(MainFragment@this)
        }
    }

    /**
     * 关闭软键盘
     *
     * @param view 当前页面上任意一个可用的view
     */
    private fun closeSoftKeyboard(view: View?) {
        if (view == null || view.windowToken == null) {
            return
        }
        val imm: InputMethodManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateMsg(data: MessageItem) {
        addMsgItem(data)
    }

    fun addMsgItem(data: MessageItem) {
        msgList.add(data)
        //if  data.payloadCase == CMessage.Message.PayloadCase.CONTENT

        /* 这是服务器时间转换为本地时间的办法
          val msgDate =  Date(msg.msgTime.seconds * 1000L)
                    val localTime = TimeUtil.getTimeStringAutoShort2(msgDate, true)
                    Log.i("ChatLib", localTime)
         */
        //Toast.makeText(context, data.cMsg.content.data, Toast.LENGTH_LONG).show()
        msgAdapter.setList(msgList)

        listView.smoothScrollToPosition(msgAdapter.itemCount)
    }

    //需要每60秒调用一次这个函数，确保socket的活动状态。
    fun sendHeartBeat(){
        chatLib.sendHeartBeat()
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