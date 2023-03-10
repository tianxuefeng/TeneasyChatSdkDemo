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

class KeFuViewModel() : ViewModel() {
    // TODO: Implement the ViewModel

    val mlSendMsg = MutableLiveData<String>()

    val mlTitle = MutableLiveData<String>()
    val mlBtnSendVis = MutableLiveData<Boolean>()
    val mlExprIcon = MutableLiveData<Int>()
    val mlMsgTypeTxt = MutableLiveData<Boolean>()

    val mlMsgList = MutableLiveData<ArrayList<MessageItem>?>()

    val mlMsgMap = MutableLiveData<HashMap<Long, MessageItem>?>()

//    val focusChangeListener = MutableLiveData<View.OnFocusChangeListener>()
//    val textChangedListener = MutableLiveData<TextWatcher>()

//    val clickSendExprListener = MutableLiveData<View.OnClickListener>()
//    val clickSendListener = MutableLiveData<View.OnClickListener>()
//
//    val clickSendImgListener = MutableLiveData<View.OnClickListener>()

//    private var msgList: ArrayList<MessageItem>
//    private var selectImgs: ArrayList<LocalMedia>

    private var chatLib: ChatLib

    init {
        mlSendMsg.value = ""
        mlTitle.value = ""
        mlExprIcon.value = R.drawable.h5_biaoqing
        mlMsgTypeTxt.value = true
        mlBtnSendVis.value = false

        chatLib = ChatLib()
        chatLib.makeConnect()


        mlMsgList.value = ArrayList()
        mlMsgMap.value = hashMapOf()
    }

    fun sendMsg(): Boolean {
        if(mlSendMsg.value != null && mlSendMsg.value!!.trim().isNotEmpty()) {
            val msg = mlSendMsg.value
            val msgItem = chatLib.composeAChatmodel(msg!!, false)
            addMsgItem(msgItem)
            chatLib.sendMsg(msgItem)

            mlSendMsg.value = "";

            /*val cMsg = CMessage.Message.newBuilder()
            var cMContent = MessageContent.newBuilder()
            cMContent.setData("????????????")
            cMsg.setContent(cMContent)
            print(cMsg.content.data)*/
            //val item = MessageItem(true, msg, 0, TimeUtil.getTimeStringAutoShort2(Date(), true))
//            if (chatLib.sendingMessageItem != null) {
//                addMsgItem(chatLib.sendingMessageItem!!)
//            }
            return true
        } else {
            return false
        }
    }

    private fun uploadImg(imgPath: String) {

    }

    fun addMsgImg(url: String, id: Long) {
        chatLib.sendMessageImage(url, id)
    }

    fun addMsgItem(data: MessageItem) {
        val list = mlMsgList.value
        data.payLoadId = System.currentTimeMillis()
        list!!.add(data)
        mlMsgList.value = list
        mlMsgMap.value!![data.payLoadId] = data
        //if  data.payloadCase == CMessage.Message.PayloadCase.CONTENT

        /* ???????????????????????????????????????????????????
          val msgDate =  Date(msg.msgTime.seconds * 1000L)
                    val localTime = TimeUtil.getTimeStringAutoShort2(msgDate, true)
                    Log.i("ChatLib", localTime)
         */
        //Toast.makeText(context, data.cMsg.content.data, Toast.LENGTH_LONG).show()


//        listView.smoothScrollToPosition(msgAdapter.itemCount)
    }

    //?????????60????????????????????????????????????socket??????????????????
    fun sendHeartBeat(){
        chatLib.sendHeartBeat()
        println("???????????????????????????")
    }

    fun updateMsgStatus(data: MessageItem) {
        if (data!!.isLeft)
            addMsgItem(data)
        else {
            if (data.payLoadId == null || data.payLoadId <= 0) {
                // ????????????
                addMsgItem(data)
            } else {
                val list = mlMsgList.value
                var msgItem = mlMsgMap.value!![data.payLoadId]
                if(msgItem != null) {
                    msgItem.sendStatus = data.sendStatus
                    mlMsgList.value = list
                }
                // ????????????
//                for (item in list!!) {
//                    if (item.payLoadId == data.payLoadId/* && item.cMsg!!.content.data.equals(data.cMsg!!.content.data)*/) {
////                        item.payLoadId = data.payLoadId
//                        item.sendStatus = data.sendStatus
//                        // ????????????observe
//                        mlMsgList.value = list
////                        msgAdapter.setList(msgList)
////                        msgAdapter.notifyDataSetChanged()
//                        return
//                    }
//                }

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatLib.disConnect()
    }

    fun composeAChatmodel(msg: String, left: Boolean) {
        addMsgItem(chatLib.composeAChatmodel(msg, left))
    }

    fun composeAChatmodelImg(imgPath: String, isLeft: Boolean): Long {
        val msgItem = chatLib.composeAChatmodelImg(imgPath, isLeft)
        addMsgItem(msgItem)
        return msgItem.payLoadId
    }

//    fun updateMsgItemImg(id: Long, newId: String) {
//        mlMsgMap!!.value!![id]!!.id = newId
//    }

    fun getToken():String {
        return chatLib.token!!
    }

}