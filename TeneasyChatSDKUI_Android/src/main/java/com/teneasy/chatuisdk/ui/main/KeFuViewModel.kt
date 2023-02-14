package com.teneasy.chatuisdk.ui.main

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.teneasy.chatuisdk.R
import com.teneasy.chatuisdk.ui.base.GlideEngine
import com.teneasy.sdk.ChatLib
import com.teneasy.sdk.ui.MessageItem

class KeFuViewModel(fragment: Fragment) : ViewModel() {
    // TODO: Implement the ViewModel
    var msgAdapter: MessageListAdapter

    val mlSendMsg = MutableLiveData<String>()

    val mlTitle = MutableLiveData<String>()
    val mlBtnSendVis = MutableLiveData<Boolean>()
    val mlExprIcon = MutableLiveData<Int>()
    val mlMsgTypeTxt = MutableLiveData<Boolean>()

    val focusChangeListener = MutableLiveData<View.OnFocusChangeListener>()
    val textChangedListener = MutableLiveData<TextWatcher>()

    val clickSendExprListener = MutableLiveData<View.OnClickListener>()
    val clickSendListener = MutableLiveData<View.OnClickListener>()

    val clickSendImgListener = MutableLiveData<View.OnClickListener>()

    val fragment = fragment

    private var msgList: ArrayList<MessageItem>
    private var selectImgs: ArrayList<LocalMedia>

    private var chatLib: ChatLib

    private lateinit var dialogBottomMenu: DialogBottomMenu

    init {
        mlSendMsg.value = ""
        mlTitle.value = ""
        mlExprIcon.value = R.drawable.h5_biaoqing
        mlMsgTypeTxt.value = true
        mlBtnSendVis.value = false
        msgList = ArrayList()
        msgAdapter = MessageListAdapter(fragment.requireContext())

        chatLib = ChatLib()
        chatLib.makeConnect(fragment.requireContext())

        selectImgs = ArrayList()

        dialogBottomMenu = DialogBottomMenu(fragment.requireContext())
            .setItems(fragment.resources.getStringArray(R.array.bottom_menu))
            .setOnItemClickListener(AdapterView.OnItemClickListener{adapterView, view, i, l ->
                when (i) {
                    0 -> {
                        // 选择相册
                        showSelectPic(fragment, object : OnResultCallbackListener<LocalMedia> {
                            override fun onResult(result: java.util.ArrayList<LocalMedia>) {
                                if(result != null && result.size > 0) {
                                    val item = result[0]
//                        uploadImg(item.path)
                                    dialogBottomMenu.dismiss()
                                    addMsgItem(chatLib.composeAChatmodelImg(item.path, true))
                                }
                            }
                            override fun onCancel() {}
                        })
                    }
                    1 -> {
                        // 拍照
                        showCamera(fragment, object : OnResultCallbackListener<LocalMedia> {
                            override fun onResult(result: java.util.ArrayList<LocalMedia>) {
                                if(result != null && result.size > 0) {
                                    val item = result[0]
//                        uploadImg(item.path)
                                    dialogBottomMenu.dismiss()
                                    addMsgItem(chatLib.composeAChatmodelImg(item.path, true))
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

//        dialogBottomMenu = DialogBottomMenu(fragment.requireContext(), "", AdapterView.OnItemClickListener{adapterView, view, i, l ->
//            when (i) {
//                0 -> {
//                    // 选择相册
//
//                }
//                1 -> {
//                    // 拍照
//                    showCamera(fragment, object : OnResultCallbackListener<LocalMedia> {
//                        override fun onResult(result: java.util.ArrayList<LocalMedia>) {
//                            if(result != null && result.size > 0) {
//                                val item = result[0]
////                        uploadImg(item.path)
//                                addMsgItem(chatLib.composeAChatmodelImg(item.path, true))
//                            }
//                        }
//
//                        override fun onCancel() {}
//                    })
//                }
//                else -> {
//                    dialogBottomMenu.dismiss()
//                }
//            }
//        })
//        dialogBottomMenu.setItems(fragment.resources.getStringArray(R.array.bottom_menu))

        initListener()
    }

    private fun initListener() {
        focusChangeListener.value = View.OnFocusChangeListener { v: View, hasFocus: Boolean ->
            if (!hasFocus) {
                closeSoftKeyboard(v)
            }
        }

        textChangedListener.value = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // TODO Auto-generated method stub
                // 输入框有内容的时候，显示发送按钮，隐藏图片选择按钮
                mlBtnSendVis.value = s != null && s.isNotEmpty()
            }

            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int,
                count: Int
            ) {}
        }

        clickSendExprListener.value = View.OnClickListener { v:View ->
            // 发送表情
            if(mlExprIcon.value == R.drawable.h5_biaoqing) {
                mlExprIcon.value = R.drawable.ht_shuru
            } else {
                mlExprIcon.value = R.drawable.h5_biaoqing
            }
        }

        clickSendImgListener.value = View.OnClickListener { v:View ->
            // 发送表情
//            mlBtnSendVis.value = mlBtnSendVis.value != true
            dialogBottomMenu.show(v)
        }

        clickSendListener.value = View.OnClickListener { v:View ->
            if(mlSendMsg.value != null && mlSendMsg.value!!.trim().isNotEmpty()) {
                closeSoftKeyboard(v)
                val msg = mlSendMsg.value
                chatLib.sendMsg(msg!!)

                mlSendMsg.value = "";

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
                Toast.makeText(fragment.requireContext(), "请输入信息内容", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun uploadImg(imgPath: String) {

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

//        listView.smoothScrollToPosition(msgAdapter.itemCount)
    }

    //需要每60秒调用一次这个函数，确保socket的活动状态。
    fun sendHeartBeat(){
        chatLib.sendHeartBeat()
        println("确保通信在活跃状态")
    }

    fun updateMsgStatus(data: MessageItem) {
        if (data!!.isLeft)
            addMsgItem(data)
        else {
            if (data.payLoadId == null || data.payLoadId <= 0) {
                // 初始添加
                addMsgItem(data)
            } else {
                // 修改状态
                for (item in msgList) {
                    if (item.id == data.id && item.cMsg!!.content.data.equals(data.cMsg!!.content.data)) {
                        item.payLoadId = data.payLoadId
                        item.sendStatus = data.sendStatus

                        msgAdapter.setList(msgList)
                        msgAdapter.notifyDataSetChanged()
                        return
                    }
                }
            }
        }
    }

    fun composeAChatmodel(msg: String, left: Boolean) {
        addMsgItem(chatLib.composeAChatmodel(msg, left))
    }

    //==========图片选择===========//
    fun showCamera(
        fragment: Fragment?,
        resultCallbackListener: OnResultCallbackListener<LocalMedia>
    ) {
        PictureSelector.create(fragment)
            .openCamera(SelectMimeType.ofImage())
            .forResult(resultCallbackListener)
    }

    fun showSelectPic(fragment: Fragment?,
                      resultCallbackListener: OnResultCallbackListener<LocalMedia>) {
        PictureSelector.create(fragment)
            .openGallery(SelectMimeType.ofImage())
            .setImageEngine(GlideEngine.createGlideEngine())
            .setMaxSelectNum(1)
            .isDisplayCamera(false)
            .forResult(resultCallbackListener)
    }
}