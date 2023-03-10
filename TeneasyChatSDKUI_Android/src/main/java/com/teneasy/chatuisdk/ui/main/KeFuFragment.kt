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
        requireActivity().title = "??????"
       // requireActivity().titleColor = R.color.black
        // Must be done during an initialization phase like onCreate
        // Must be done during an initialization phase like onCreate

    }

//    override fun initView() {
//        netscape.javascript.JSObject.getWindow().
//        recyclerView = findViewById(R.id.chat_listview) as RecyclerView
//        layoutManager = LinearLayoutManager(this)
//        layoutManager.setOrientation(LinearLayoutManager.VERTICAL)
//        layoutManager.setStackFromEnd(false)
//        recyclerView.setLayoutManager(layoutManager)
//        //??????????????????view
//        inputEdit = findViewById(R.id.bar_edit_text) as EditText
//        btnSend = findViewById(R.id.bar_btn_send) as Button
//    }


    private fun setScrollBottom() {
        binding!!.listView.post {

            binding!!.listView.scrollToPosition(msgAdapter.itemCount - 1)
            val layoutManager = binding!!.listView.layoutManager as LinearLayoutManager
            val target =
                layoutManager.findViewByPosition(msgAdapter.itemCount - 1)
            if (target != null) {
                // int offset=  recyclerView.getMeasuredHeight() - target.getMeasuredHeight();
                layoutManager.scrollToPositionWithOffset(
                    msgAdapter.itemCount - 1,
                    Int.MAX_VALUE
                ) //?????????????????????
            }
        }
    }

    var isShow = false

    override fun initView() {
//        (activity)?.window?.decorView?.viewTreeObserver
//            ?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
//
//                fun screenHeight(): Int {
//                    return (context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager?)!!.defaultDisplay.height
//                }
//
//                override fun onGlobalLayout() {
//                    val rect = Rect()
//                    requireActivity().window.decorView
//                        .getWindowVisibleDisplayFrame(rect)
//                    val screenHeight = screenHeight()
//                    val keyboardHeight: Int = screenHeight - rect.bottom //???????????????
//                    if (abs(keyboardHeight) > screenHeight / 5 && !isShow) {
//                        setScrollBottom()
//                        isShow = true
//                    } else {
//                        isShow = false
//                    }
//                }
//            }) //?????????????????????
        binding!!.setVariable(BR.vm, viewModel)
        binding!!.lifecycleOwner = this

        msgAdapter = context?.let { MessageListAdapter(it) }!!
        msgAdapter.setList(viewModel.mlMsgList.value)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.stackFromEnd = false
        binding!!.listView.layoutManager = layoutManager

//        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
//        binding!!.listView.layoutManager = layoutManager
        binding!!.listView.adapter = msgAdapter

        binding!!.etMsg.setOnFocusChangeListener { v: View, hasFocus: Boolean ->
            if (!hasFocus) {
                closeSoftKeyboard(v)
            }
        }
        binding!!.etMsg.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // TODO Auto-generated method stub
                // ???????????????????????????????????????????????????????????????????????????
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
                Toast.makeText(context, "????????????????????????", Toast.LENGTH_SHORT).show()
            }
        }
        binding!!.etMsg.isFocusable = true
        binding!!.etMsg.setFocusableInTouchMode(true);


        binding!!.btnSendExpr.setOnClickListener {
            // ????????????
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
            // ????????????
//            mlBtnSendVis.value = mlBtnSendVis.value != true
            dialogBottomMenu.show(v)
        }

        dialogBottomMenu = DialogBottomMenu(context)
            .setItems(resources.getStringArray(R.array.bottom_menu))
            .setOnItemClickListener(AdapterView.OnItemClickListener{ adapterView, view, i, l ->
                when (i) {
                    0 -> {
                        // ????????????
                        showSelectPic(object : OnResultCallbackListener<LocalMedia> {
                            override fun onResult(result: java.util.ArrayList<LocalMedia>) {
                                if(result != null && result.size > 0) {
                                    val item = result[0]
//                        uploadImg(item.path)
                                    dialogBottomMenu.dismiss()
                                    val id = viewModel.composeAChatmodelImg(item.path, false)
                                    uploadImg(item.realPath, id)
                                }
                            }
                            override fun onCancel() {}
                        })
                    }
                    1 -> {
                        // ??????
                         showCamera(object : OnResultCallbackListener<LocalMedia> {
                            override fun onResult(result: java.util.ArrayList<LocalMedia>) {
                                if(result != null && result.size > 0) {
                                    val item = result[0]
                        //uploadImg(item.path)
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


//    val mRunnable = Runnable {
//        if (msgAdapter.itemCount > 0 && binding!!.listView != null) {
//            val layoutManager  = binding!!.listView.layoutManager as LinearLayoutManager
//            val target = layoutManager.findViewByPosition(msgAdapter.itemCount - 1)
//
//            if(target != null) {
//                if(!msgAdapter.getList()!![msgAdapter.itemCount-1].isLeft) {
//                    val img = target!!.findViewById(R.id.iv_left_image) as AppCompatImageView
//
//                }
//                val offset = binding!!.listView.measuredHeight - target.measuredHeight
//                layoutManager.scrollToPositionWithOffset(msgAdapter.itemCount  - 1, offset)
//            }
////            binding!!.listView.scrollToPosition(msgAdapter.itemCount - 1)
//        }
//    }

    private fun initData() {

        viewModel.mlMsgList.observe(this) {
//            msgAdapter.setList(it)
            msgAdapter.notifyDataSetChanged()

            if(it!!.size > 1) {
//                moveToPosition(it!!.size - 1)
////                binding!!.listView.scrollToPosition(it!!.size - 1)
////                binding!!.listView.postDelayed(mRunnable, 10)
//
                val layoutManager  = binding!!.listView.layoutManager as LinearLayoutManager
////                val lastIndex = it.size - 1
                layoutManager.scrollToPositionWithOffset(it.size - 1, 0)
                binding!!.listView.post {
                    val target = layoutManager.findViewByPosition(msgAdapter.itemCount - 1)
                    if(target != null) {
                        val offset = binding!!.listView.measuredHeight - target.measuredHeight - 50
                        layoutManager.scrollToPositionWithOffset(msgAdapter.itemCount - 1, offset)
                    }
                }
////                scroller.targetPosition = binding!!.listView.adapter!!.itemCount - 1
////                binding!!.listView.layoutManager!!.startSmoothScroll(scroller)
            }
        }
//        binding.listView.canScrollVertically(1)
    }

    fun moveToPosition(position: Int) {
        val layoutManager = binding!!.listView.layoutManager
        //????????????LinearLayoutManager ?????????????????????????????????
        if (layoutManager is LinearLayoutManager) {
            val firstItem = layoutManager.findFirstVisibleItemPosition()
            val lastItem = layoutManager.findLastVisibleItemPosition()
            if (position < firstItem || position > lastItem) {
                binding!!.listView.smoothScrollToPosition(position)
            } else {
                val movePosition = position - firstItem
                val top: Int = binding!!.listView.getChildAt(movePosition).top
                binding!!.listView.smoothScrollBy(0, top)
            }
        }
    }

//    /**
//     * ???????????????
//     *
//     * @param view ????????????????????????????????????view
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
                //?????????????????????
                viewModel.sendHeartBeat()
            }
        }, 0,5000)    //??????5???????????????
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
            // ????????????
            if(event.arg == 200) {
                startTimer()
            } else {
                closeTimer()
            }
        } else if(event.what == 1 && event.data != null) {

            if (event.data is MessageItem) {
                // ????????????
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
     * ???????????????
     *
     * @param view ????????????????????????????????????view
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
//        /* ???????????????????????????????????????????????????
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
                        binding!!.tvTitle.text = "??????${res.data.workerName}"
                        viewModel.composeAChatmodel("?????????????????????${res.data.workerName}", true)

                        // ????????????
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
//        //????????????OkHttpClient??????
//        val okHttpClient = OkHttpClient()
//        val requestBody: RequestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), param.toString())
//        val request: Request = Request.Builder()
//            .url("https://csapi.hfxg.xyz/v1/api/query-worker")
//            .addHeader("X-Token", "CCcQARgRIBwoxtTNgeQw.BL9S_YLEWQmWzD1NjYHaDM3dUa6UOqgwOORaC9l8WyWuEVgCbxgd67GXmlQJsm1R2aQUgFDDrvpDsq3CmWqVAA") //??????header
//            .post(requestBody)
//            .build()
//        //????????????????????????
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

    // ???????????????UI
    private val handler: Handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if(binding != null && msg.what == 200) {
                binding!!.tvTitle.text = "??????${msg.obj}"
                viewModel.composeAChatmodel("?????????????????????${msg.obj}", true)
            }
        }
    }

    fun uploadImg(filePath: String, id: Long) {
//        val request = XHttp.custom().accessToken(false)
//        request.baseUrl(Constants.baseUrlApi)

        // ???????????????Builder,??????????????????Springboot MultipartFile
        val file = File(filePath)

//        mIProgressLoader!!.showLoading()
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
                        // ????????????
                        mIProgressLoader?.dismissLoading()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        mIProgressLoader?.dismissLoading()
                        val body = response.body()
                        if(body != null) {
                            val path = response.body()!!.string()
                            // ????????????
                            viewModel.addMsgImg(Constants.baseUrlImage + path, id)
//                            viewModel.updateMsgItemImg(id, urlPath)
                        } else {
                            // ????????????
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

    //==========????????????===========//
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