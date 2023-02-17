package com.teneasy.chatuisdk.ui.main;

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.teneasy.chatuisdk.BR
import com.teneasy.chatuisdk.databinding.FragmentKefuBinding
import com.teneasy.sdk.MessageEventBus
import com.teneasy.sdk.ui.MessageItem
import com.teneasyChat.gateway.GGateway
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.IOException
import java.util.*

class KeFuFragment : BaseBindingFragment<FragmentKefuBinding>() {

    companion object {
        fun newInstance() = KeFuFragment()
    }

    private lateinit var viewModel: KeFuViewModel

    private lateinit var timer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = KeFuViewModel(MainFragment@this)
        timer = Timer()
//        myTest.makeConnect2()
        //myTest.m
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        requireActivity().title = "客服"
//        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
       // requireActivity().titleColor = R.color.black
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
            } else if (event.data is GGateway.SCHi) {
                val data = event.data as GGateway.SCHi
                val workId = data.workerId
                loadWorker(workId)
                /* 此处需要调用Api来获取客服的名字，并显示在头部
                https://csapi.hfxg.xyz/v1/api/query-worker
{
"workerId": 1
}

header:
X-Token ="token"
                 */
//                viewModel.composeAChatmodel("你好，我是客服小福 ", true)
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


    fun loadWorker(workerId: Int) {
        val param = JSONObject()
        param.put("workerId", workerId)
        //创建一个OkHttpClient对象
        val okHttpClient = OkHttpClient()
        val requestBody: RequestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), param.toString())
        val request: Request = Request.Builder()
            .url("https://csapi.hfxg.xyz/v1/api/query-worker")
            .addHeader("X-Token", "CCcQARgRIBwoxtTNgeQw.BL9S_YLEWQmWzD1NjYHaDM3dUa6UOqgwOORaC9l8WyWuEVgCbxgd67GXmlQJsm1R2aQUgFDDrvpDsq3CmWqVAA") //添加header
            .post(requestBody)
            .build()
        //发送请求获取响应
        okHttpClient.newCall(request).enqueue(object : Callback {
            @Throws(IOException::class)
            override fun onResponse(arg0: Call, response: Response) {
                val body = response.body
                if(body != null) {
                    val json = JSONObject(body.string())
                    if(json.getString("msg").equals("ok", ignoreCase = true)) {
                        val workerInfo = json.getJSONObject("data")
                        val name = workerInfo.getString("workerName")

                        val msg = Message.obtain(handler, 200, name)
                        handler.sendMessage(msg)
                    }
                }
            }

            override fun onFailure(arg0: Call, arg1: IOException) {}
        })
    }

    // 主线程更新UI
    private val handler: Handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if(msg.what == 200) {
                binding!!.tvTitle.text = "客服${msg.obj}"
                viewModel.composeAChatmodel("你好，我是客服${msg.obj}", true)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeTimer()
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentKefuBinding {
        return FragmentKefuBinding.inflate(layoutInflater, parent, false)
    }
}