package com.example.teneasychatsdkdemo.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teneasychatsdkdemo.BR
import com.example.teneasychatsdkdemo.databinding.FragmentMainBinding
import com.example.teneasychatsdkdemo.ui.bind.BaseBindingFragment
import com.teneasy.chatuisdk.*
import com.teneasy.chatuisdk.ui.main.KeFuActivity
import com.teneasy.sdk.MessageEventBus
import com.teneasy.sdk.ui.MessageItem
import gateway.GGateway.SCHi
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class MainFragment : BaseBindingFragment<FragmentMainBinding>() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    private lateinit var timer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = MainViewModel(MainFragment@this)
        timer = Timer()
//        myTest.makeConnect2()
        //myTest.m

//        binding!!.btnSend!!.setOnClickListener {
//            val myIntent = Intent(requireActivity(), KeFuActivity::class.java)
//
//            this.startActivity(myIntent)
//        }




    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!EventBus.getDefault().isRegistered(MainFragment@this)) {
            EventBus.getDefault().register(MainFragment@this)
        }
        requireActivity().title = "客服小福"
//        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
       // requireActivity().titleColor = R.color.black

        binding?.btnSend?.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                val myIntent = Intent(requireActivity(), KeFuActivity::class.java)

                requireActivity().startActivity(myIntent)
            }

        })
    }

    override fun initView() {
        binding!!.setVariable(BR.vm, viewModel)
        binding!!.lifecycleOwner = this
    }

//    /**
//     * 关闭软键盘
//     *
//     * @param view 当前页面上任意一个可用的view
//     */
//    private fun closeSoftKeyboard(view: View?) {
//        if (view == null || view.windowToken == null) {
//            return
//        }
//        val imm: InputMethodManager =
//            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(view.windowToken, 0)
//    }

    private fun startTimer() {
        timer.schedule(object : TimerTask() {
            override fun run() {
                //需要执行的任务
                viewModel.sendHeartBeat()
            }
        }, 0,30000)     // 便于测试，暂时设定为30秒
    }

    private fun closeTimer() {
        if(timer != null) {
            timer.cancel()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateMsg(event: MessageEventBus<Any>) {
        if(event.what == 0) {
            // 解析状态
            if(event.arg == 200) {
                startTimer()
            } else {
                closeTimer()
            }
        } else if(event.what == 1 && event.data != null) {

            if (event.data is MessageItem) {
                // 解析数据
                val data = event.data as MessageItem
                viewModel.updateMsgStatus(data)
            } else if (event.data is SCHi) {
                val data = event.data as SCHi
                val workId = data.workerId
                /* 此处需要调用Api来获取客服的名字，并显示在头部
                https://csapi.hfxg.xyz/v1/api/query-worker
{
"workerId": 1
}

header:
X-Token ="token"
                 */
                viewModel.composeAChatmodel("你好，我是客服小福 " + workId, true)
            }
        }
    }

//    private fun addMsgItem(data: MessageItem) {
//        msgList.add(data)
//        //if  data.payloadCase == CMessage.Message.PayloadCase.CONTENT
//
//        /* 这是服务器时间转换为本地时间的办法
//          val msgDate =  Date(msg.msgTime.seconds * 1000L)
//                    val localTime = TimeUtil.getTimeStringAutoShort2(msgDate, true)
//                    Log.i("ChatLib", localTime)
//         */
//        //Toast.makeText(context, data.cMsg.content.data, Toast.LENGTH_LONG).show()
//        msgAdapter.setList(msgList)
//
//        bin.smoothScrollToPosition(msgAdapter.itemCount)
//    }
    override fun onDestroy() {
        super.onDestroy()
        closeTimer()
        if(!EventBus.getDefault().isRegistered(MainFragment@this)) {
            EventBus.getDefault().unregister(MainFragment@this)
        }
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentMainBinding {
        return FragmentMainBinding.inflate(layoutInflater, parent, false)
    }
}