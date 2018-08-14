package com.halove.wifidirectmanager.socketutils

import android.app.IntentService
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.halove.wifidirectmanager.protocol.BasicProtocol
import java.io.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

/**
 * Created by xyp on 2018/8/9.
 * 运行在后台的服务接收客户端发来的消息
 */
class ServerSocketIntentService constructor(name: String = "ServerSocketIntentService") : IntentService(name) {

    private var serverSocket: ServerSocket? = null

    private var inputStream: InputStream? = null


    private var callback: IMessageReceiveCallback? = null

    private var stopRecevie = false


    inner class MyBinder : Binder() {
        val service: ServerSocketIntentService
            get() = this@ServerSocketIntentService
    }

    override fun onBind(intent: Intent): IBinder? {
        return MyBinder()
    }

    override fun onHandleIntent(intent: Intent?) {
        release()
        while (!stopRecevie){
            if (serverSocket == null) {
                serverSocket = ServerSocket()
                serverSocket?.run {
                    reuseAddress = true
                    try {
                        bind(InetSocketAddress(Config.PORT))
                    } catch (e: SocketException) {
                        Log.e("xuyueping", e.message)
                        release()
                    }
                }

            }
            var client: Socket? = null
            try {
                client = serverSocket?.accept()
            } catch (e: SocketException) {
                Log.e("xuyueping", e.message)
            }


            client?.let {
                Config.CLIENT_IP = it.inetAddress?.hostAddress
                inputStream = it.getInputStream()
            }

            inputStream?.let {
                val (reciverData, isEmpty) = SocketUtil.readFromStream(it)
                if (reciverData != null) {
                    if (reciverData.getProtocolType() > 0) {
                        callback?.onReceive(reciverData)
                    } else {
                        Log.e("ReceiveRun", "reciverData：$reciverData\tisEmpty:$isEmpty")
                    }
                }
                if (!isEmpty) {
                    //todo 通道异常回调
                }
            }
        }
    }

    private fun release() {
        serverSocket?.close()
        serverSocket = null
        inputStream?.close()
        inputStream = null
        stopRecevie = true
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
    }


    interface IMessageReceiveCallback {
        fun onReceive(data: BasicProtocol)
    }

    fun setReceiveCallback(callback: IMessageReceiveCallback) {
        this.callback = callback
    }

}