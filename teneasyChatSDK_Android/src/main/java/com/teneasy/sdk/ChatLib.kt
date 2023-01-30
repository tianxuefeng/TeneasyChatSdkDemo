package com.teneasy.sdk

import android.content.Context
import android.widget.Toast
import api.common.CMessage
import com.google.protobuf.Timestamp
import gateway.GAction
import gateway.GGateway
import gateway.GPayload.Payload
import io.crossbar.autobahn.websocket.WebSocketConnection
import io.crossbar.autobahn.websocket.WebSocketConnectionHandler
import io.crossbar.autobahn.websocket.types.ConnectionResponse
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import org.java_websocket.protocols.Protocol
import org.json.JSONObject
import java.net.URI
import java.nio.charset.Charset
import org.greenrobot.eventbus.EventBus





class ChatLib {
    val url = "wss://csapi.xdev.stream/v1/gateway/h5?token=" +
            "CCcQARgGIBwohOeGoN8w.MDFy6dFaTLFByZSuv9lP0fcYOaOGc_WgiTnTP8dFdE3prh7iiT37Ioe5FrelrDltQocQsGB3APz0WKUVUDdcDA"
    fun sayHello(context: Context){
        Toast.makeText(context, "Good sdk! Good!", Toast.LENGTH_LONG).show()
    }

    fun isConnection() : Boolean {
        socket?: return false
        return socket.isConnected
    }

    var context: Context? = null
    var payloadId: Long = 0
    private lateinit var socket: WebSocketConnection;

    fun makeConnect(context: Context){
        this.context = context
        socket = WebSocketConnection()
        socket.connect(url, object : WebSocketConnectionHandler() {
            override fun onConnect(response: ConnectionResponse) {
                println("Connected to server")
                Toast.makeText(context, "Connected to server", Toast.LENGTH_LONG).show()
            }

            override fun onOpen() {
                Toast.makeText(context, "open", Toast.LENGTH_LONG).show()
                //sendMsg("android 123")
//                connection.sendMessage("Echo with Autobahn")
            }

            override fun onClose(code: Int, reason: String) {
                println("Connection closed")
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
    }

    fun receiveMsg(data: ByteArray) {
        if(data.size == 1)
            print("在别处登录了")
        else {
            val payLoad = Payload.parseFrom(data)
            val msgData = payLoad.data

            println("act: ${payLoad.act.number}")
            if(payLoad.act == GAction.Action.ActionSCRecvMsg) {
                val msg = GGateway.SCRecvMessage.parseFrom(msgData)
//                val msg = GGateway.CSSendMessage.parseFrom(msgData)
//                val content = String(msg.toByteArray())
                println("recv: ${msg.msg.content.data}")
                EventBus.getDefault().post(CallbackMsg(msg.msg.content.data))
            } else if(payLoad.act == GAction.Action.ActionSCHi) {
                val msg = GGateway.SCHi.parseFrom(msgData)
                payloadId = msg.id
                println("schi: $msg")
            } else if(payLoad.act == GAction.Action.ActionForward) {
                val msg = GGateway.CSForward.parseFrom(msgData)

                println("forward: $msg.data")
            } else if(payLoad.act == GAction.Action.ActionSCSendMsgACK) {
                val msg = GGateway.SCSendMessage.parseFrom(msgData)
                print("消息回执")
                println(msg)
            } else
                print("received data: $data")
        }
    }

    fun sendMsg(msg: String) {
        socket?: throw IllegalStateException("connection is close!")
        if(!isConnection()) {
            Toast.makeText(this.context, "dis-connected", Toast.LENGTH_LONG).show()
            return
        }

        //第一层
        val content = CMessage.MessageContent.newBuilder()
        content.data = msg

        //第二层
        val msg = CMessage.Message.newBuilder()
        msg.content = content.build()
        msg.sender = 0
        msg.chatId = 2692944494598
        msg.worker = 3
        msg.msgTime = Timestamp.getDefaultInstance()

        // 第三层
        val cSendMsg = GGateway.CSSendMessage.newBuilder()
        cSendMsg.msg = msg.build()
        val cSendMsgData = cSendMsg.build().toByteString()

        //第四层
        val payload = Payload.newBuilder()
        payload.data = cSendMsgData
        payload.act = GAction.Action.ActionCSSendMsg
        payloadId += 1
        payload.id = payloadId

        socket.sendMessage(payload.build().toByteArray(), true)
    }

    fun makeConnect2(){

        val obj = JSONObject()
        obj.put("event", "addChannel")
        obj.put("channel", "ok_btccny_ticker")
        val message = obj.toString()
        //send message
        //send message
        val mWs: WebSocketClient =
            object : WebSocketClient(URI(url), Draft_6455()) {
                override fun onMessage(message: String) {
                    val obj = JSONObject(message)
                    val channel = obj.getString("channel")
                }

                override fun onOpen(handshake: ServerHandshake?) {
                    println("opened connection")
                }

                override fun onClose(code: Int, reason: String, remote: Boolean) {
                    println("closed connection")
                }

                override fun onError(ex: Exception) {
                    ex.printStackTrace()
                }
            }
        mWs.connect()
    }
}