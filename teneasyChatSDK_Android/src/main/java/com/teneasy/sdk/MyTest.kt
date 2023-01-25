package com.teneasy.sdk

import android.content.Context
import android.widget.Toast
import io.crossbar.autobahn.websocket.WebSocketConnection
import io.crossbar.autobahn.websocket.WebSocketConnectionHandler
import io.crossbar.autobahn.websocket.types.ConnectionResponse
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI


class MyTest {
    //val url = "wss://127.0.0.1:8013/v1/gateway/h5?token=CAsQAxgBIAwojeyt6tQw.NcnU2L85lR8ImA3rTbHl5f8UCTueNQ8oyj7Kb4w2EEazoywQAHDoh5sxTOflvfUkgvWBuE3llWNvH5rDSLHCAQ"
   val url = "wss://csapi.xdev.stream/v1/gateway/h5?token="
    fun sayHello(context: Context){
        Toast.makeText(context, "Good sdk! Good!", Toast.LENGTH_LONG).show()
    }

    fun makeConnect(context: Context){
//         Ws ws = new Ws.Builder().from( "ws://server_address");
//        ws.connect();

        val connection = WebSocketConnection()
        connection.connect(url, object : WebSocketConnectionHandler() {
            override fun onConnect(response: ConnectionResponse) {
                println("Connected to server")
                Toast.makeText(context, "Connected to server", Toast.LENGTH_LONG).show()
            }

            override fun onOpen() {
                connection.sendMessage("Echo with Autobahn")
            }

            override fun onClose(code: Int, reason: String) {
                println("Connection closed")
                Toast.makeText(context, "Closed", Toast.LENGTH_LONG).show()
            }

            override fun onMessage(payload: String) {
                println("Received message: $payload")
                connection.sendMessage(payload)
            }
        })
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
        //open websocket
        //open websocket
        mWs.connect()

    }
}