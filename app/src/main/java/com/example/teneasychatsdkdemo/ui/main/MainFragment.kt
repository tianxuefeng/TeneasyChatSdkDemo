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
import com.teneasy.sdk.MessageEventBus
import com.teneasy.sdk.ui.MessageItem
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
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

    private lateinit var timer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        chatLib = ChatLib()
        //myTest.sayHello(requireContext())
        chatLib.makeConnect(requireContext())
        timer = Timer()
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
        msgAdapter = MessageListAdapter(requireContext())
        listView.adapter = msgAdapter

        val btnSend: Button = view.findViewById(R.id.btn_send)
        btnSend.setOnClickListener(View.OnClickListener { v:View ->
//            chatLib.sendHeartBeat()
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
        requireActivity().title = "客服小福"
       // requireActivity().titleColor = R.color.black
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

    private fun startTimer() {
        timer.schedule(object : TimerTask() {
            override fun run() {
                //需要执行的任务
                sendHeartBeat()
            }
        }, 0,30000)     // 便于测试，暂时设定为30秒
    }

    private fun closeTimer() {
        if(timer != null) {
            timer.cancel()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateMsg(event: MessageEventBus<MessageItem>) {
        if(event.what == 0) {
            // 解析状态
            if(event.arg == 200) {
                startTimer()
            } else {
                closeTimer()
            }
        } else if(event.what == 1 && event.data != null) {
            // 解析数据
            val data = event.data
            if(!data!!.isSend)
                addMsgItem(data)
            else {
                if(data.payLoadId == null || data.payLoadId <= 0) {
                    // 初始添加
                    addMsgItem(data)
                } else {
                    // 修改状态
                    for (item in msgList) {
                        if(item.id == data.id && item.cMsg!!.content.data.equals(data.cMsg!!.content.data)) {
                            item.payLoadId = data.payLoadId
                            item.sendError = data.sendError

                            msgAdapter.setList(msgList)
                            msgAdapter.notifyDataSetChanged()
                            return
                        }
                    }
                }

            }
        }
    }

    private fun addMsgItem(data: MessageItem) {
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
        println("刷新连接")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        closeTimer()
        if(!EventBus.getDefault().isRegistered(MainFragment@this)) {
            EventBus.getDefault().unregister(MainFragment@this)
        }
    }
}