package com.halove.wifidirectmanager.socketutils

import android.os.AsyncTask
import android.util.Log
import com.halove.wifidirectmanager.protocol.BasicProtocol
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Created by xyp on 2018/8/9.
 * 发送数据
 */
class SendSocketTask(var data: BasicProtocol) : AsyncTask<String, Int, Boolean>() {

    var socket: Socket? = null
    var outputStream: OutputStream? = null


    override fun doInBackground(vararg p0: String?): Boolean {
        release()
        socket = Socket()
        socket!!.bind(null)
        try {
            socket!!.connect(InetSocketAddress(p0[0], Config.PORT), 10000)
            outputStream = socket!!.getOutputStream()
        } catch (e: Exception) {
            Log.e("xuyueping", e.message)
        }

        if (outputStream != null)
            SocketUtil.write2Stream(data, outputStream!!)
        return true
    }

    private fun release() {
        socket?.close()
        socket = null
        outputStream?.close()
        outputStream = null
    }

    override fun onPreExecute() {
        Log.d("xuyueping", "开始传送")
    }

    override fun onProgressUpdate(vararg values: Int?) {
        Log.d("xuyueping", "传送${values[0]}")
    }

    override fun onPostExecute(aBoolean: Boolean?) {
        Log.d("xuyueping", "传送结束")
    }
}