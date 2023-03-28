package com.teneasy.chatuisdk.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.teneasy.chatuisdk.R
import com.teneasy.sdk.ChatLib
import com.teneasy.sdk.ui.MessageItem


/**
 * 客户界面的viewModel，主要UI层的数据。例如：socket消息发送、聊天数据。
 */
class KeFuViewModel() : ViewModel() {
    // TODO: Implement the ViewModel

    val mlSendMsg = MutableLiveData<String>()

    val mlTitle = MutableLiveData<String>()
    val mlBtnSendVis = MutableLiveData<Boolean>()
    val mlExprIcon = MutableLiveData<Int>()
    val mlMsgTypeTxt = MutableLiveData<Boolean>()

    val mlMsgList = MutableLiveData<ArrayList<MessageItem>?>()

    val mlMsgMap = MutableLiveData<HashMap<Long, MessageItem>?>()

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

    /**
     * 根据输入框的内容，发送文本消息。
     */
    fun sendMsg(): Boolean {
        if(mlSendMsg.value != null && mlSendMsg.value!!.trim().isNotEmpty()) {
            val msg = mlSendMsg.value
            val msgItem = chatLib.composeAChatmodel(msg!!, false)
            addMsgItem(msgItem)
            chatLib.sendMsg(msgItem)

            mlSendMsg.value = "";
            return true
        } else {
            return false
        }
    }

    private fun uploadImg(imgPath: String) {

    }

    /**
     * 根据传递的图片地址，发送图片消息。该方法会发送socket消息
     * @param url
     * @param id
     */
    fun addMsgImg(url: String, id: Long) {
        chatLib.sendMessageImage(url, id)
    }

    /**
     * 往聊天界面添加一个消息，不会触发socket消息发送。该方法自动会生成消息ID（以当前时间currentTimeMillis）
     *
     */
    private fun addMsgItem(data: MessageItem) {
        val list = mlMsgList.value
        data.payLoadId = System.currentTimeMillis()
        list!!.add(data)
        mlMsgList.value = list
        mlMsgMap.value!![data.payLoadId] = data
    }

    /**
     * 需要每60秒调用一次这个函数，确保socket的活动状态。
     */
    fun sendHeartBeat() {
        chatLib.sendHeartBeat()
        println("确保通信在活跃状态")
    }

    /**
     * 根据消息类型和状态，更新消息的显示状态
     */
    fun updateMsgStatus(data: MessageItem) {
        if (data!!.isLeft)
            addMsgItem(data)
        else {
            if (data.payLoadId == null || data.payLoadId <= 0) {
                // 初始添加
                addMsgItem(data)
            } else {
                val list = mlMsgList.value
                var msgItem = mlMsgMap.value!![data.payLoadId]
                if(msgItem != null) {
                    msgItem.sendStatus = data.sendStatus
                    mlMsgList.value = list
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatLib.disConnect()
    }

    /**
     * 往聊天界面添加一个消息。不会触发socket消息发送
     * @param msg
     * @param left
     */
    fun composeAChatmodel(msg: String, left: Boolean) {
        addMsgItem(chatLib.composeAChatmodel(msg, left))
    }

    /**
     * 往聊天界面添加一个图片消息。不会触发socket消息发送
     * @param imgPath
     * @param isLeft
     */
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