package com.teneasy.chatuisdk.ui.main;

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.JsonObject
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
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
import okhttp3.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.math.abs

/**
 * 客服主界面fragment
 */
class KeFuFragment : BaseBindingFragment<FragmentKefuBinding>() {

    companion object {
        fun newInstance() = KeFuFragment()
    }

    private lateinit var msgAdapter: MessageListAdapter

    private lateinit var viewModel: KeFuViewModel

    private var mIProgressLoader: IProgressLoader? = null

    private var timer: Timer? = null

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
    }

    // UI初始化
    override fun initView() {
        binding!!.setVariable(BR.vm, viewModel)
        binding!!.lifecycleOwner = this

        msgAdapter = context?.let { MessageListAdapter(it) }!!
        msgAdapter.setList(viewModel.mlMsgList.value)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.stackFromEnd = false
        binding!!.listView.layoutManager = layoutManager

        binding!!.listView.adapter = msgAdapter

        binding!!.etMsg.setOnFocusChangeListener { v: View, hasFocus: Boolean ->
            if (!hasFocus) {
                closeSoftKeyboard(v)
            }
        }
        // 聊天界面输入框，输入事件。实现文本输入和表情输入的UI切换功能
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

        // 点击发送按钮，发送消息
        binding!!.btnSend.setOnClickListener { v: View ->
            if (viewModel.sendMsg()) {
                closeSoftKeyboard(v)
            } else {
                Toast.makeText(context, "发送内容不能为空", Toast.LENGTH_SHORT).show()
            }
        }
        binding!!.etMsg.isFocusable = true
        binding!!.etMsg.isFocusableInTouchMode = true;

        // 发送表情
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
            dialogBottomMenu.show(v)
        }

        // 底部菜单初始化
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
                                    dialogBottomMenu.dismiss()
                                    // 上传图片之前，首先在聊天框添加一个图片消息，更新聊天界面
                                    val id = viewModel.composeAChatmodelImg(item.path, false)
                                    uploadImg(item.realPath, id)
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
                                    dialogBottomMenu.dismiss()
                                    val id = viewModel.composeAChatmodelImg(item.path, false)
                                    uploadImg(item.realPath, id)
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

    // 数据初始化
    private fun initData() {
        viewModel.mlMsgList.observe(this) {
            msgAdapter.notifyDataSetChanged()

            if(it!!.size > 1) {
                val layoutManager  = binding!!.listView.layoutManager as LinearLayoutManager
                layoutManager.scrollToPositionWithOffset(it.size - 1, 0)
                binding!!.listView.post {
                    val target = layoutManager.findViewByPosition(msgAdapter.itemCount - 1)
                    if(target != null) {
                        val offset = binding!!.listView.measuredHeight - target.measuredHeight - 50
                        layoutManager.scrollToPositionWithOffset(msgAdapter.itemCount - 1, offset)
                    }
                }
            }
        }
    }

    // 启动计时器，持续调用心跳方法
    private fun startTimer() {
        closeTimer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                //需要执行的任务
                viewModel.sendHeartBeat()
            }
        }, 0,5000)    //每隔5秒发送心跳
    }

    // 关闭计时器
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

    // EventBus 消息接收解析，针对socket sdk中的消息进行捕捉和解析。
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

    /**
     * 通过workerId加载客服头像，并添加一条打招呼的消息
     * @param workerId
     */
    private fun loadWorker(workerId: Int) {
        if (requireActivity().isFinishing){
            return
        }
        val param = JsonObject()
        param.addProperty("workerId", workerId)
        val request = XHttp.custom().accessToken(false)
        request.headers("X-Token", viewModel.getToken())
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
    }


    /**
     * 上传图片。上传成功后，会直接调用socket进行消息发送。
     *  @param filePath
     *  @param id
     */
    fun uploadImg(filePath: String, id: Long) {
        // 多文件上传Builder,用以匹配后台Springboot MultipartFile
        val file = File(filePath)
        Thread(Runnable {
            kotlin.run {
                val multipartBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("myFile", file.name, MultipartBody.create(MediaType.parse("multipart/form-data"), file))
                    .addFormDataPart("type", "1")
                    .build()

                val request2 = Request.Builder().url(Constants.baseUrlApi + "/v1/assets/upload/")
                    .addHeader("X-Token", viewModel.getToken())
                    .post(multipartBody).build()

                val okHttpClient = OkHttpClient()
                val call = okHttpClient.newCall(request2)
                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        // 上传失败
                        mIProgressLoader?.dismissLoading()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        mIProgressLoader?.dismissLoading()
                        val body = response.body()
                        if(body != null) {
                            val path = response.body()!!.string()
                            // 发送图片
                            viewModel.addMsgImg(Constants.baseUrlImage + path, id)
                        } else {
                            // 上传失败
                            Toast.makeText(context, "上传失败", Toast.LENGTH_LONG).show()
                        }

                    }
                })

            }
        }).start()
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
    // 调用拍照
    private fun showCamera(resultCallbackListener: OnResultCallbackListener<LocalMedia>
    ) {
        PictureSelector.create(KeFuFragment@this)
            .openCamera(SelectMimeType.ofImage())
            .forResult(resultCallbackListener)
    }

    // 选择图片
    private fun showSelectPic(resultCallbackListener: OnResultCallbackListener<LocalMedia>) {
        PictureSelector.create(KeFuFragment@this)
            .openGallery(SelectMimeType.ofImage())
            .setImageEngine(GlideEngine.createGlideEngine())
            .setMaxSelectNum(1)
            .isDisplayCamera(false)
            .forResult(resultCallbackListener)
    }
}