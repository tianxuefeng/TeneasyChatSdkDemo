package com.teneasy.sdk

import android.content.Context
import android.widget.Toast
import dev.icerock.moko.socket.Socket
import dev.icerock.moko.socket.SocketEvent
import dev.icerock.moko.socket.SocketOptions
import io.crossbar.autobahn.websocket.WebSocketConnection
import io.crossbar.autobahn.websocket.WebSocketConnectionHandler
import io.crossbar.autobahn.websocket.types.ConnectionResponse


class MyTest {
    fun sayHello(context: Context){
        Toast.makeText(context, "Good sdk! Good!", Toast.LENGTH_LONG).show()
    }

    fun makeConnect(context: Context){
//         Ws ws = new Ws.Builder().from( "ws://server_address");
//        ws.connect();

        val connection = WebSocketConnection()
        connection.connect("wss://127.0.0.1:8013/v1/gateway/h5?token=CAsQAxgBIAwojeyt6tQw.NcnU2L85lR8ImA3rTbHl5f8UCTueNQ8oyj7Kb4w2EEazoywQAHDoh5sxTOflvfUkgvWBuE3llWNvH5rDSLHCAQ", object : WebSocketConnectionHandler() {
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
        val socket = Socket(
            endpoint = "http://csapi.xdev.stream/v1/gateway/h5?token=",
            config = SocketOptions(
                queryParams = mapOf("token" to "MySuperToken"),
                transport = SocketOptions.Transport.WEBSOCKET
            )
        ) {
            on(SocketEvent.Connect) {
                println("connected...")
            }

            on(SocketEvent.Connecting) {
                println("connecting")
            }

            on(SocketEvent.Disconnect) {
                println("disconnect")
            }

            on(SocketEvent.Error) {
                println("error $it")
            }

            on(SocketEvent.Reconnect) {
                println("reconnect")
            }

            on(SocketEvent.ReconnectAttempt) {
                println("reconnect attempt $it")
            }

            on(SocketEvent.Ping) {
                println("ping")
            }

            on(SocketEvent.Pong) {
                println("pong")
            }

            on("employee.connected") { data ->
               /* val serializer = DeliveryCar.serializer()
                val json = JSON.nonstrict
                val deliveryCar: DeliveryCar = json.parse(serializer, data)*/
                //...
            }
        }
    }
}