package com.halove.wifidirectmanager.protocol

import com.halove.wifidirectmanager.socketutils.Config
import java.io.ByteArrayOutputStream

/**
 * 心跳数据包
 * Created by yanglijun on 18-8-8.
 */
class HeartProtocol : BasicProtocol() {

    //1表示心跳数据，只发送一个字节
    var data = ByteArray(1){1}

    override fun getProtocolType(): Int {
        return Config.PROTOCOL_TYPE_HEART
    }

    override fun getLength(): Int {
        return super.getLength()+1//增加一个字节心跳
    }

    override fun getContentData(): ByteArray {
        val bytes = super.getContentData()
        val baos = ByteArrayOutputStream(getLength())
        baos.write(bytes)
        baos.write(data)
        return baos.toByteArray()
    }
}