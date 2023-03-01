package com.teneasy.sdk

import android.content.Context
import android.content.LocusId
import android.widget.Toast
import com.google.protobuf.Timestamp
import com.teneasy.sdk.ui.MessageItem
import com.teneasy.sdk.ui.MessageSendState
import com.teneasyChat.api.common.CMessage
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


class ChatLib {
    val baseUrl = "wss://csapi.xdev.stream/v1/gateway/h5?token="
    fun sayHello(context: Context) {
        Toast.makeText(context, "Good sdk! Good!", Toast.LENGTH_LONG).show()
    }

    fun isConnection() : Boolean {
        socket?: return false
        return socket.isOpen
    }

//    var context: Context? = null
//    var payloadId: Long = 0
    var sendingMessageItem: MessageItem? = null
    //var chatId: Long = 2692944494602 //2692944494608客服下线了
//    var token: String? = "CCcQARgGIBwohOeGoN8w.MDFy6dFaTLFByZSuv9lP0fcYOaOGc_WgiTnTP8dFdE3prh7iiT37Ioe5FrelrDltQocQsGB3APz0WKUVUDdcDA"//客服下线了
    /*
    测试环境 客服账号密码:  qixin001  qixin001  token: CAEQARjcCSBXKKWGwMroMA.15wWZ5uafQ0TGRbs7od1ZMmpiZrsgjAo9wqMPBKWLUgwbBeK1M9FZ8Yku5A5CYlQWlAJcOMpWBMCVD3rOJ4iAA
     */
    var token: String? = "CCcQARgRIBwoxtTNgeQw.BL9S_YLEWQmWzD1NjYHaDM3dUa6UOqgwOORaC9l8WyWuEVgCbxgd67GXmlQJsm1R2aQUgFDDrvpDsq3CmWqVAA"
    //private lateinit var socket: WebSocketConnection;
    private lateinit var socket: WebSocketClient;

    //这个方式也很好，只是需要安卓SDK>24
    /*fun makeConnect2(context: Context){
        this.context = context
        socket = WebSocketConnection()
        val url = baseUrl + token
        socket.connect(url, object : WebSocketConnectionHandler() {
            override fun onConnect(response: ConnectionResponse) {
                println("Connected to server")
                EventBus.getDefault().post(200)

                var eventBus = MessageEventBus<MessageItem>()
                eventBus.arg = 200
                EventBus.getDefault().post(eventBus)
                //Toast.makeText(context, "Connected to server", Toast.LENGTH_LONG).show()
            }

            override fun onOpen() {
                //Toast.makeText(context, "open", Toast.LENGTH_LONG).show()
                //sendMsg("android 123")
//                connection.sendMessage("Echo with Autobahn")
            }

            override fun onClose(code: Int, reason: String) {
                var eventBus = MessageEventBus<MessageItem>()
                eventBus.arg = -200
                EventBus.getDefault().post(eventBus)
                //println("Connection closed")
                Toast.makeText(context, "Closed", Toast.LENGTH_LONG).show()
            }

            override fun onMessage(payload: String) {
                println("Received message: $payload")
            }

            override fun onMessage(payload: ByteArray, isBinary: Boolean) {
                super.onMessage(payload, isBinary)
                println("Received message: $payload")
                receiveMsg(payload)
                //Toast.makeText(context, "Received", Toast.LENGTH_LONG).show()
            }
        })
    }*/

    fun makeConnect(){
        val obj = JSONObject()
        obj.put("event", "addChannel")
        obj.put("channel", "ok_btccny_ticker")
        val message = obj.toString()
        //send message
        //send message
        val url = baseUrl + token
        socket =
            object : WebSocketClient(URI(url), Draft_6455()) {
                override fun onMessage(message: String) {
                    val obj = JSONObject(message)
                    val channel = obj.getString("channel")
                }

                override fun onMessage(bytes: ByteBuffer?) {
                    super.onMessage(bytes)
                    if (bytes != null)
                        receiveMsg(bytes.array())
                }

                override fun onOpen(handshake: ServerHandshake?) {
                    println("opened connection")

                    EventBus.getDefault().post(200)

                    var eventBus = MessageEventBus<MessageItem>()
                    eventBus.arg = 200
                    EventBus.getDefault().post(eventBus)
                }



                override fun onClose(code: Int, reason: String, remote: Boolean) {
                    var eventBus = MessageEventBus<MessageItem>()
                    eventBus.arg = -200
                    EventBus.getDefault().post(eventBus)
                    //println("Connection closed")
//                    Toast.makeText(context, "Closed", Toast.LENGTH_LONG).show()
                }

                override fun onError(ex: Exception) {
                    ex.printStackTrace()
                }
            }
        socket.connect()
    }

    //发送文字消息
//    fun sendMsg(textMsg: String) {
//        sendingMessageItem = composeAChatmodel(textMsg, false)
//        if(!isConnection()) {
//            //Toast.makeText(this.context, "dis-connected", Toast.LENGTH_LONG).show()
//            makeConnect()
//            failedToSend()
//            return
//        }
//
//       /* //第一层
//        val content = CMessage.MessageContent.newBuilder()
//        content.data = textMsg
//
//        //第二层
//        val msg = CMessage.Message.newBuilder()
//        msg.content = content.build()
//        msg.sender = 0
//        msg.chatId = 0
//        msg.worker = 0
//        msg.msgTime = TimeUtil.msgTime()
//
//        sendingMessageItem = MessageItem()
//        sendingMessageItem!!.id = payloadId
//        sendingMessageItem!!.isSend = true
//        sendingMessageItem!!.cMsg = msg.build()*/
//
//
//
//        // 第三层
//        val cSendMsg = GGateway.CSSendMessage.newBuilder()
//        cSendMsg.msg = sendingMessageItem!!.cMsg
//        val cSendMsgData = cSendMsg.build().toByteString()
//
//        //第四层
//        val payload = GPayload.Payload.newBuilder()
//        payload.data = cSendMsgData
//        payload.act = GAction.Action.ActionCSSendMsg
//        payloadId += 1
//        payload.id = payloadId
//
//        //socket.sendMessage(payload.build().toByteArray(), true)
//        socket.send(payload.build().toByteArray())
//    }

    fun sendMsg(textMsg: MessageItem) {
        if(!isConnection()) {
            //Toast.makeText(this.context, "dis-connected", Toast.LENGTH_LONG).show()
            makeConnect()
            failedToSend()
            return
        }

        // 第三层
        val cSendMsg = GGateway.CSSendMessage.newBuilder()
        cSendMsg.msg = textMsg!!.cMsg
        val cSendMsgData = cSendMsg.build().toByteString()

        //第四层
        val payload = GPayload.Payload.newBuilder()
        payload.data = cSendMsgData
        payload.act = GAction.Action.ActionCSSendMsg
//        payloadId += 1
        payload.id = textMsg.payLoadId

        //socket.sendMessage(payload.build().toByteArray(), true)
        socket.send(payload.build().toByteArray())
    }

    //发送图片类型的消息
    fun sendMessageImage(url: String, id: Long) {
        //第一层
        val content = CMessage.MessageImage.newBuilder()
        content.setUri(url)

        //第二层
        val msg = CMessage.Message.newBuilder()
        msg.setImage(content)
        msg.sender = 0
        msg.chatId = 0
        msg.worker = 0
        msg.msgTime = TimeUtil.msgTime()
        sendingMessageItem = MessageItem()
        sendingMessageItem!!.isLeft = false
        sendingMessageItem!!.cMsg = msg.build()

        // 第三层
        val cSendMsg = GGateway.CSSendMessage.newBuilder()
        cSendMsg.msg = msg.build()
        val cSendMsgData = cSendMsg.build().toByteString()

        //第四层
        val payload = GPayload.Payload.newBuilder()
        payload.data = cSendMsgData
        payload.act = GAction.Action.ActionCSSendMsg
//        payloadId += 1
        payload.id = id

        //socket.sendMessage(payload.build().toByteArray(), true)
        if(!isConnection()) {
            //Toast.makeText(this.context, "dis-connected", Toast.LENGTH_LONG).show()
            failedToSend()
            makeConnect()
            return
        }
        socket.send(payload.build().toByteArray())
    }

    //目前每隔150秒，通信就好自动断掉，建议每隔60秒调用它
    fun sendHeartBeat(){
        //let value: Int32 = -1333
        //var zero  = 0
        //val beat = zero.toByte()
        val buffer = ByteArray(1)
        buffer[0] = 0
       // socket.sendMessage(buffer, true)
        socket.send(buffer)
    }

    fun receiveMsg(data: ByteArray) {
        if(data.size == 1)
            print("在别处登录了")
        else {
            val payLoad = GPayload.Payload.parseFrom(data)
            val msgData = payLoad.data
            val payloadId = payLoad.id
            println("act: ${payLoad.act.number}")
            if(payLoad.act == GAction.Action.ActionSCRecvMsg) {
                val msg = GGateway.SCRecvMessage.parseFrom(msgData)
//                val msg = GGateway.CSSendMessage.parseFrom(msgData)
//                val content = String(msg.toByteArray())
                println("recv: ${msg.msg.content.data}")

                var chatModel = MessageItem()
                chatModel.cMsg =  msg.msg
                chatModel.payLoadId = payloadId
                chatModel.id = payloadId.toString()
                chatModel.isLeft = true

                var eventBus = MessageEventBus<MessageItem>()
                eventBus.setData(chatModel)
                EventBus.getDefault().post(eventBus)

                /*EventBus.getDefault().post(MessageItem(false, msg.msg.content.data, payLoad.id, TimeUtil.getTimeStringAutoShort2(
                    Date(), true
                )))*/
            } else if(payLoad.act == GAction.Action.ActionSCHi) {
                val msg = GGateway.SCHi.parseFrom(msgData)
                token = msg.token
                //chatId = msg.id
                print("worker id"  + msg.workerId)
                //println("schi: $msg")



           //Compose chat model

                // 采用封装好的自定义事件类，来实现多类型传递
                var eventBus = MessageEventBus<GGateway.SCHi>()
                eventBus.setData(msg)
                EventBus.getDefault().post(eventBus)

            } else if(payLoad.act == GAction.Action.ActionForward) {
                val msg = GGateway.CSForward.parseFrom(msgData)

                println("forward: $msg.data")
            } else if(payLoad.act == GAction.Action.ActionSCSendMsgACK) {//消息回执
                val msg = GGateway.SCSendMessage.parseFrom(msgData)

                val msgItem = MessageItem()
                msgItem.payLoadId = payloadId
                if (msg.msgId != 0.toLong()) {
                    msgItem.sendStatus = MessageSendState.发送成功
                }

                var eventBus = MessageEventBus<MessageItem>()
                eventBus.setData(msgItem!!)
                EventBus.getDefault().post(eventBus)
//                sendingMessageItem?.apply {
//                    this.payLoadId = payloadId
//                    if (msg.msgId != 0.toLong()) {
//                        //this.msgId = msg.msgId
//                        this.sendStatus = MessageSendState.发送成功
//                    }
//                    var eventBus = MessageEventBus<MessageItem>()
//                    eventBus.setData(sendingMessageItem!!)
//                    EventBus.getDefault().post(eventBus)
//                    //sendingMessageItem.cMsg!!.msgId = msg.msgId
//                    //sendingMessageItem.cMsg!!.msgTime = TimeUtil.msgTime()
//                    //EventBus.getDefault().post(sendingMessageItem)
//                }

                print("消息回执: ${msg.msgId}")
            } else
                print("received data: $data")
        }
    }

    //撰写一条信息
    fun composeAChatmodel(textMsg: String, isLeft: Boolean) : MessageItem{
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
        cMContent.setData(textMsg)
        cMsg.setContent(cMContent)

        var chatModel = MessageItem()
        chatModel.cMsg = cMsg.build()
//        chatModel.payLoadId = System.currentTimeMillis()
        chatModel.isLeft = isLeft
        return chatModel
    }

    //撰写一条图片信息
    fun composeAChatmodelImg(imgPath: String, isLeft: Boolean) : MessageItem{
        var cMsg = CMessage.Message.newBuilder()
        var cMContent = CMessage.MessageImage.newBuilder()

        var d = Timestamp.newBuilder()
        val cal = Calendar.getInstance()
        cal.time = Date()
        val millis = cal.timeInMillis
        d.seconds = (millis * 0.001).toLong()

        //d.t = msgDate.time
        cMsg.msgTime = d.build()
        cMContent.uri = imgPath
        cMsg.setImage(cMContent)

        var chatModel = MessageItem()
        chatModel.cMsg = cMsg.build()
        chatModel.imgPath = imgPath
//        chatModel.payLoadId = payloadId
        chatModel.isLeft = isLeft
        return chatModel
    }

    private fun failedToSend(){
        sendingMessageItem?.let {
            var eventBus = MessageEventBus<MessageItem>()
            it.sendStatus = MessageSendState.发送失败
            eventBus.setData(it)
            EventBus.getDefault().post(eventBus)
        }
    }

    //断开连接需要调用
    fun disConnect(){
        socket.close()
    }
}