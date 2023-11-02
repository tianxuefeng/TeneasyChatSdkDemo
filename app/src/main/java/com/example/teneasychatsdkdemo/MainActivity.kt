package com.example.teneasychatsdk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.teneasychatsdk.databinding.ActivityMainBinding
import com.teneasy.sdk.ChatLib
import com.teneasy.sdk.TeneasySDKDelegate
import com.teneasyChat.api.common.CMessage
import com.teneasyChat.gateway.GGateway

class MainActivity : AppCompatActivity(), TeneasySDKDelegate {

    private lateinit var chatLib: ChatLib
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatLib = ChatLib()
        chatLib.listener = this
        chatLib?.makeConnect()



        binding.btnSend.setOnClickListener {
            sendMsg()
        }
    }

    private fun sendMsg(){
        val sayHello = "你好！"
        val msgItem = chatLib.composeALocalMessage(sayHello)
        //addMsgItem(msgItem)
        chatLib.sendMessage(sayHello, CMessage.MessageFormat.MSG_TEXT)

        chatLib.sendMessage("https://www.video.123", CMessage.MessageFormat.MSG_VIDEO)
    }

    override fun receivedMsg(msg: CMessage.Message) {
        println(msg)
    }

    override fun msgReceipt(msg: CMessage.Message, payloadId: Long, msgId: Long) {
        //println(msg)
        val suc = if (msgId == 0L) "发送失败" else "发送成功"
        println(payloadId.toString() + " " +suc)
    }

    override fun systemMsg(msg: String) {
        //TODO("Not yet implemented")
        Log.i("MainAct systemMsg", msg)
    }

    override fun connected(c: GGateway.SCHi) {
        Log.i("MainAct connected", "成功连接")
    }

    override fun workChanged(msg: GGateway.SCWorkerChanged) {
        Log.i("MainAct connected", "已经更换客服")
    }

}