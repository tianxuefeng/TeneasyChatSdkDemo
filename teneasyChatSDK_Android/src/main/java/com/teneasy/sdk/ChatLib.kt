package com.teneasy.sdk

import android.content.Context
import android.content.LocusId
import android.util.Log
import android.widget.Toast
import com.google.protobuf.Timestamp
import com.teneasyChat.api.common.CMessage
import com.teneasyChat.api.common.CMessage.MessageFormat
import com.teneasyChat.gateway.GAction
import com.teneasyChat.gateway.GGateway
import com.teneasyChat.gateway.GPayload
//import io.crossbar.autobahn.websocket.WebSocketConnection
//import io.crossbar.autobahn.websocket.WebSocketConnectionHandler
//import io.crossbar.autobahn.websocket.types.ConnectionResponse
import org.greenrobot.eventbus.EventBus
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI
import java.nio.ByteBuffer
import java.util.*


interface TeneasySDKDelegate {
    // 收到消息
    fun receivedMsg(msg: CMessage.Message)

   /**
   消息回执
    @msg 已发送的消息
    @payloadId
    @msgId, 如果是0，表示服务器没有生成消息id, 发送失败
    */
    fun msgReceipt(msg: CMessage.Message, payloadId: Long, msgId: Long) // 使用Long代替UInt64

    // 系统消息，用于显示Tip
    fun systemMsg(msg: String)

    // 连接状态
    fun connected(c: GGateway.SCHi)

    // 客服更换回调
    fun workChanged(msg: GGateway.SCWorkerChanged)
}

/**
 * 通讯核心类，提供了发送消息、解析消息等功能
 */
class ChatLib {
    private val TAG = "ChatLib"
    // 通讯地址
    val baseUrl = "wss://csapi.xdev.stream/v1/gateway/h5?token="
    fun isConnection() : Boolean {
        socket?: return false
        return socket.isOpen
    }

    // 当前发送的消息实体，便于上层调用的逻辑处理
    var sendingMessage: CMessage.Message? = null
    //var chatId: Long = 2692944494602 //2692944494608客服下线了
    /*
    测试环境 客服账号密码:  qixin001  qixin001  token: CAEQARjeCSBXKLK3no7pMA.4ZFT0KP1_DaEtPcdVhSyL9Q4Aolk16-bCgT6P8tm-cMOUEl-m1ygdpeIXx9iDaZbTcxEcRqW0gr6v7cuUjY2Cg
     */
    //var token: String? = "CCcQARgRIBwoxtTNgeQw.BL9S_YLEWQmWzD1NjYHaDM3dUa6UOqgwOORaC9l8WyWuEVgCbxgd67GXmlQJsm1R2aQUgFDDrvpDsq3CmWqVAA"//Dev_xiaofua1234
    var token: String? = "CCcQARgRIBwoxtTNgeQw.BL9S_YLEWQmWzD1NjYHaDM3dUa6UOqgwOORaC9l8WyWuEVgCbxgd67GXmlQJsm1R2aQUgFDDrvpDsq3CmWqVAA"//qi xin
    //var token: String? = "CCcQARgCIBwo6_7VjN8w.Pa47pIINpFETl5RxrpTPqLcn8RVBAWrGW_ogyzQipI475MLhNPFFPkuCNEtsYvabF9uXMKK2JhkbRdZArUK3DQ"//XiaoFua001

    private lateinit var socket: WebSocketClient
    var listener: TeneasySDKDelegate? = null
    private var payloadId = 1111

    /**
     * 启动socket连接
      */
    fun makeConnect(){
        val obj = JSONObject()
        obj.put("event", "addChannel")
        obj.put("channel", "ok_btccny_ticker")
        val url = baseUrl + token
        socket =
            object : WebSocketClient(URI(url), Draft_6455()) {
                override fun onMessage(message: String) {
                }

                override fun onMessage(bytes: ByteBuffer?) {
                    super.onMessage(bytes)
                    if (bytes != null)
                        receiveMsg(bytes.array())
                }
                override fun onOpen(handshake: ServerHandshake?) {
                    Log.i(TAG, "opened connection")
                    listener?.systemMsg("已连接上服务器")
                }
                override fun onClose(code: Int, reason: String, remote: Boolean) {
//                    var eventBus = MessageEventBus<MessageItem>()
//                    eventBus.arg = -200
//                    EventBus.getDefault().post(eventBus)
                    listener?.systemMsg("已断开通信" + reason)
                }

                override fun onError(ex: Exception) {
                    ex.printStackTrace()
                }
            }
        socket.connect()
    }

    /**
     * 发送文本类型的消息
     * @param msg   消息内容或图片url,音频url,视频url...
     */
     fun sendMessage(msg: String, type: MessageFormat) {
      if (type == MessageFormat.MSG_TEXT){
          sendTextMessage(msg)
      }else if (type == MessageFormat.MSG_IMG){
          sendImageMessage(msg)
      }else if (type == MessageFormat.MSG_VIDEO){
          sendVideoMessage(msg)
      }else if (type == MessageFormat.MSG_VOICE){
          sendAudioMessage(msg)
      }else if (type == MessageFormat.MSG_FILE){
          sendFileMessage(msg)
      }else {
          sendTextMessage(msg)
      }
        sendingMessage?.let {
            doSendMsg(it)
        }
    }

    /**
     * 发送文本类型的消息
     * @param msg   消息内容
     */
   private fun sendTextMessage(msg: String) {
        //第一层
        val content = CMessage.MessageImage.newBuilder()
        content.uri = msg

        //第二层
        val msg = CMessage.Message.newBuilder()
        msg.setImage(content)
        msg.sender = 0
        msg.chatId = 0
        msg.worker = 0
        msg.msgTime = TimeUtil.msgTime()

        sendingMessage = msg.build()
    }


    /**
     * 发送图片类型的消息
     * @param url   图片地址
     */
   private fun sendImageMessage(url: String) {
        //第一层
        val content = CMessage.MessageImage.newBuilder()
        content.uri = url

        //第二层
        val msg = CMessage.Message.newBuilder()
        msg.setImage(content)
        msg.sender = 0
        msg.chatId = 0
        msg.worker = 0
        msg.msgTime = TimeUtil.msgTime()

        sendingMessage = msg.build()
    }

    /**
     * 发送视频类型的消息
     * @param url   视频地址
     */
    private fun sendVideoMessage(url: String) {
        //第一层
        val content = CMessage.MessageVideo.newBuilder()
        content.uri = url

        //第二层
        val msg = CMessage.Message.newBuilder()
        msg.setVideo(content)
        msg.sender = 0
        msg.chatId = 0
        msg.worker = 0
        msg.msgTime = TimeUtil.msgTime()

        sendingMessage = msg.build()
    }

    /**
     * 发送音频类型的消息
     * @param url   音频地址
     */
    private fun sendAudioMessage(url: String) {
        //第一层
        val content = CMessage.MessageAudio.newBuilder()
        content.uri = url

        //第二层
        val msg = CMessage.Message.newBuilder()
        msg.setAudio(content)
        msg.sender = 0
        msg.chatId = 0
        msg.worker = 0
        msg.msgTime = TimeUtil.msgTime()

        sendingMessage = msg.build()
    }

    /**
     * 发送文件类型的消息
     * @param url   文件地址
     */
    private fun sendFileMessage(url: String) {
        //第一层
        val content = CMessage.MessageFile.newBuilder()
        content.uri = url

        //第二层
        val msg = CMessage.Message.newBuilder()
        msg.setFile(content)
        msg.sender = 0
        msg.chatId = 0
        msg.worker = 0
        msg.msgTime = TimeUtil.msgTime()

        sendingMessage = msg.build()
    }


    /**
     * 发送文本消息
     * @param textMsg MessageItem
     */
    private fun doSendMsg(cMsg: CMessage.Message) {
        if(!isConnection()) {
            makeConnect()
            failedToSend()
            return
        }

        // 第三层
        val cSendMsg = GGateway.CSSendMessage.newBuilder()
        cSendMsg.msg = cMsg
        val cSendMsgData = cSendMsg.build().toByteString()

        //第四层
        val payload = GPayload.Payload.newBuilder()
        payload.data = cSendMsgData
        payload.act = GAction.Action.ActionCSSendMsg
        payloadId += 1
        payload.id = cMsg.chatId

        socket.send(payload.build().toByteArray())
    }

    /**
     *  心跳，一般建议每隔60秒调用
     */
    fun sendHeartBeat(){
        val buffer = ByteArray(1)
        buffer[0] = 0
        Log.i(TAG, "sending heart beat")
        socket.send(buffer)
    }

    /**
     * socket消息解析，内部方法
     * @param data
     */
    private fun receiveMsg(data: ByteArray) {
        if(data.size == 1)
            Log.i(TAG, "在别处登录了")
        else {
            val payLoad = GPayload.Payload.parseFrom(data)
            val msgData = payLoad.data
            val payloadId = payLoad.id

            //收到消息
            if(payLoad.act == GAction.Action.ActionSCRecvMsg) {
                val recvMsg = GGateway.SCRecvMessage.parseFrom(msgData)
                // 通过eventBus向上层发送数据，便于上层逻辑处理。调用时需在界面中注册eventBus事件
//                var eventBus = MessageEventBus<MessageItem>()
//                eventBus.setData(chatModel)
//                EventBus.getDefault().post(eventBus)
                recvMsg.msg.let {
                    listener?.receivedMsg(it)
                }
            } else if(payLoad.act == GAction.Action.ActionSCHi) {
                val msg = GGateway.SCHi.parseFrom(msgData)
                token = msg.token
                listener?.connected(msg)
                // 采用封装好的自定义事件类，来实现多类型传递
//                var eventBus = MessageEventBus<GGateway.SCHi>()
//                eventBus.setData(msg)
//                EventBus.getDefault().post(eventBus)
            } else if(payLoad.act == GAction.Action.ActionForward) {
                val msg = GGateway.CSForward.parseFrom(msgData)
                Log.i(TAG, "forward: ${msg.data}")
            } else if(payLoad.act == GAction.Action.ActionSCSendMsgACK) {//消息回执
                val scMsg = GGateway.SCSendMessage.parseFrom(msgData)

                if (sendingMessage != null){
                    listener?.msgReceipt(sendingMessage!!, payloadId, scMsg.msgId)
                }
                Log.i(TAG, "消息回执: ${scMsg.msgId}")
            } else
                Log.i(TAG, "received data: $data")
        }
    }

    /**
     * 通过指定的文本内容，创建消息实体。一般用于UI层对用户显示的自定义消息（该方法并未调用socket发送消息）。
     * 如需发送至后端，需获取返回的消息实体，再调用发送方法
     * @param textMsg
     * @param isLeft    指定消息显示方式
     */
    //撰写一条信息
    fun composeALocalMessage(textMsg: String) : CMessage.Message{
        //第一层
        var cMsg = CMessage.Message.newBuilder()
        //第二层
        var cMContent = CMessage.MessageContent.newBuilder()

        var d = Timestamp.newBuilder()
        val cal = Calendar.getInstance()
        cal.time = Date()
        val millis = cal.timeInMillis
        d.seconds = (millis * 0.001).toLong()

        //d.t = msgDate.time
        cMsg.msgTime = d.build()
        cMContent.data = textMsg
        cMsg.setContent(cMContent)

        return cMsg.build()
    }


    private fun failedToSend(){
//        sendingMessageItem?.let {
//            var eventBus = MessageEventBus<MessageItem>()
//            it.sendStatus = MessageSendState.发送失败
//            eventBus.setData(it)
//            EventBus.getDefault().post(eventBus)
//        }
    }

    /**
     * 关闭socket连接，在停止使用时，需调用该方法。
     */
    fun disConnect(){
        socket.close()
    }
}