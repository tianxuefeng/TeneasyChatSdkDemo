package com.teneasy.chatuisdk.ui.main;

import android.Manifest
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.*
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.android.common.view.chat.emoji.EmojiPan
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.JsonObject
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.tbruyelle.rxpermissions3.RxPermissions
import com.teneasy.chatuisdk.BR
import com.teneasy.chatuisdk.R
import com.teneasy.chatuisdk.databinding.FragmentKefuBinding
import com.teneasy.chatuisdk.ui.base.Constants
import com.teneasy.chatuisdk.ui.base.GlideEngine
import com.teneasy.chatuisdk.ui.http.MainApi
import com.teneasy.chatuisdk.ui.http.ReturnData
import com.teneasy.chatuisdk.ui.http.bean.WorkerInfo
import com.teneasy.sdk.MessageEventBus
import com.teneasy.sdk.ui.MessageItem
import com.teneasyChat.gateway.GGateway
import com.xuexiang.xhttp2.XHttp
import com.xuexiang.xhttp2.callback.ProgressLoadingCallBack
import com.xuexiang.xhttp2.subsciber.ProgressDialogLoader
import com.xuexiang.xhttp2.subsciber.impl.IProgressLoader
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class KeFuFragment : BaseBindingFragment<FragmentKefuBinding>() {

    companion object {
        fun newInstance() = KeFuFragment()
    }

    private lateinit var msgAdapter: MessageListAdapter

    private lateinit var viewModel: KeFuViewModel

    private var mIProgressLoader: IProgressLoader? = null

    private var timer: Timer? = null
    //final RxPermissions rxPermissions = new RxPermissions(this);

    private lateinit var dialogBottomMenu: DialogBottomMenu
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = KeFuViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        requireActivity().title = "客服"
       // requireActivity().titleColor = R.color.black
        // Must be done during an initialization phase like onCreate
        // Must be done during an initialization phase like onCreate

    }

    override fun initView() {
        binding!!.setVariable(BR.vm, viewModel)
        binding!!.lifecycleOwner = this

        msgAdapter = context?.let { MessageListAdapter(it) }!!

        binding!!.listView.adapter = msgAdapter

        binding!!.etMsg.setOnFocusChangeListener { v: View, hasFocus: Boolean ->
            if (!hasFocus) {
                closeSoftKeyboard(v)
            }
        }
        binding!!.etMsg.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // TODO Auto-generated method stub
                // 输入框有内容的时候，显示发送按钮，隐藏图片选择按钮
                viewModel.mlBtnSendVis.value = s != null && s.isNotEmpty()
            }

            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int,
                count: Int
            ) {}
        })

        binding!!.btnSend.setOnClickListener { v: View ->
            if (viewModel.sendMsg()) {
                closeSoftKeyboard(v)
            } else {
                Toast.makeText(context, "发送内容不能为空", Toast.LENGTH_SHORT).show()
            }
        }
        binding!!.etMsg.isFocusable = true
        binding!!.etMsg.setFocusableInTouchMode(true);


        binding!!.btnSendExpr.setOnClickListener {
            // 发送表情
            if (viewModel.mlExprIcon.value == R.drawable.h5_biaoqing) {
                viewModel.mlExprIcon.value = R.drawable.ht_shuru
                binding!!.etMsg.requestFocus()
                val inputMethodManager =  requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(binding!!.etMsg, InputMethodManager.SHOW_IMPLICIT)
                binding!!.etMsg.setRawInputType(InputType.TYPE_CLASS_TEXT)
                binding!!.etMsg.setTextIsSelectable(true)
            } else {
                viewModel.mlExprIcon.value = R.drawable.h5_biaoqing
                val inputMethodManager = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(binding!!.etMsg, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        binding!!.btnSendImg.setOnClickListener { v: View ->
            // 发送表情
//            mlBtnSendVis.value = mlBtnSendVis.value != true
            dialogBottomMenu.show(v)
        }

        dialogBottomMenu = DialogBottomMenu(context)
            .setItems(resources.getStringArray(R.array.bottom_menu))
            .setOnItemClickListener(AdapterView.OnItemClickListener{ adapterView, view, i, l ->
                when (i) {
                    0 -> {
                        // 选择相册
                        showSelectPic(object : OnResultCallbackListener<LocalMedia> {
                            override fun onResult(result: java.util.ArrayList<LocalMedia>) {
                                if(result != null && result.size > 0) {
                                    val item = result[0]
//                        uploadImg(item.path)
                                    dialogBottomMenu.dismiss()
                                    viewModel.composeAChatmodelImg(item.path, false)
                                }
                            }
                            override fun onCancel() {}
                        })
                    }
                    1 -> {
                        // 拍照
                        showCamera(object : OnResultCallbackListener<LocalMedia> {
                            override fun onResult(result: java.util.ArrayList<LocalMedia>) {
                                if(result != null && result.size > 0) {
                                    val item = result[0]
//                        uploadImg(item.path)
                                    dialogBottomMenu.dismiss()
                                    viewModel.composeAChatmodelImg(item.path, false)
                                }
                            }

                            override fun onCancel() {}
                        })
                    }
                    else -> {
                        dialogBottomMenu.dismiss()
                    }
                }
            })
            .build()

        initData()
    }

    private fun initData() {
        viewModel.mlMsgList.observe(this) {
            msgAdapter.setList(it)
        }
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
        closeTimer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                //需要执行的任务
                viewModel.sendHeartBeat()
            }
        }, 0,30000)     // 便于测试，暂时设定为30秒
    }

    private fun closeTimer() {
        if(timer != null) {
            timer?.cancel()
            timer = null
        }
    }

    fun getProgressLoader(): IProgressLoader? {
        if (mIProgressLoader == null) {
            mIProgressLoader =
                ProgressDialogLoader(context)
        }
        return mIProgressLoader
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
                loadWorker(3)
            }
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
        if (requireActivity().isFinishing){
            return
        }
        val param = JsonObject()
        param.addProperty("workerId", workerId)
        val request = XHttp.custom().accessToken(false)
        request.headers("X-Token", "CCcQARgRIBwoxtTNgeQw.BL9S_YLEWQmWzD1NjYHaDM3dUa6UOqgwOORaC9l8WyWuEVgCbxgd67GXmlQJsm1R2aQUgFDDrvpDsq3CmWqVAA")
        request.call(request.create(MainApi.IMainTask::class.java)
            .workerInfo(param),
            object : ProgressLoadingCallBack<ReturnData<WorkerInfo>>(getProgressLoader()) {
                override fun onSuccess(res: ReturnData<WorkerInfo>) {
                    if (res == null) {
                        Toast.makeText(context, "Server error: 500", Toast.LENGTH_SHORT).show()
                        return
                    }
                    if (res.msg.equals("ok", true) && res.data != null) {
                        binding!!.tvTitle.text = "客服${res.data.workerName}"
                        viewModel.composeAChatmodel("你好，我是客服${res.data.workerName}", true)

                        // 更新头像
                        if (res.data.workerAvatar != null && res.data.workerAvatar?.isEmpty() == false) {
                            val url = Constants.baseUrlImage + res.data.workerAvatar
                            print("avatar:$url")
                            Glide.with(binding!!.civAuthorImage).load(url).dontAnimate()
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .into(binding!!.civAuthorImage)
                        }
                    }
                }
            })

//        val param = JSONObject()
//        param.put("workerId", workerId)
//        //创建一个OkHttpClient对象
//        val okHttpClient = OkHttpClient()
//        val requestBody: RequestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), param.toString())
//        val request: Request = Request.Builder()
//            .url("https://csapi.hfxg.xyz/v1/api/query-worker")
//            .addHeader("X-Token", "CCcQARgRIBwoxtTNgeQw.BL9S_YLEWQmWzD1NjYHaDM3dUa6UOqgwOORaC9l8WyWuEVgCbxgd67GXmlQJsm1R2aQUgFDDrvpDsq3CmWqVAA") //添加header
//            .post(requestBody)
//            .build()
//        //发送请求获取响应
//        okHttpClient.newCall(request).enqueue(object : Callback {
//            @Throws(IOException::class)
//            override fun onResponse(arg0: Call, response: Response) {
//                val body = response.body
//                if(body != null) {
//                    val json = JSONObject(body.string())
//                    if(json.getString("msg").equals("ok", ignoreCase = true)) {
//                        val workerInfo = json.getJSONObject("data")
//                        val name = workerInfo.getString("workerName")
//
//                        val msg = Message.obtain(handler, 200, name)
//                        handler.sendMessage(msg)
//                    }
//                }
//            }
//
//            override fun onFailure(arg0: Call, arg1: IOException) {}
//        })
    }

    // 主线程更新UI
    private val handler: Handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if(binding != null && msg.what == 200) {
                binding!!.tvTitle.text = "客服${msg.obj}"
                viewModel.composeAChatmodel("你好，我是客服${msg.obj}", true)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
      exit()
    }

    fun exit(){
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

    //==========图片选择===========//
    fun showCamera(resultCallbackListener: OnResultCallbackListener<LocalMedia>
    ) {
        PictureSelector.create(KeFuFragment@this)
            .openCamera(SelectMimeType.ofImage())
            .forResult(resultCallbackListener)
    }

    fun showSelectPic(resultCallbackListener: OnResultCallbackListener<LocalMedia>) {
        PictureSelector.create(KeFuFragment@this)
            .openGallery(SelectMimeType.ofImage())
            .setImageEngine(GlideEngine.createGlideEngine())
            .setMaxSelectNum(1)
            .isDisplayCamera(false)
            .forResult(resultCallbackListener)
    }
}