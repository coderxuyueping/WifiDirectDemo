package com.halove.wifidirectmanager.protocol

import com.halove.wifidirectmanager.socketutils.Config
import java.io.ByteArrayOutputStream

/**
 * 答复协议包，收到数据后的回复确认
 * Created by yanglijun on 18-8-9.
 */
class ReplyProtocol : BasicProtocol() {

    private val answer = ByteArray(1) { 1 }

    override fun getProtocolType(): Int {
        return Config.PROTOCOL_TYPE_REPLY
    }

    override fun getLength(): Int {
        return super.getLength() + 1
    }

    override fun getContentData(): ByteArray {
        val bytes = super.getContentData()
        val baos = ByteArrayOutputStream(getLength())
        baos.write(bytes)
        baos.write(answer)
        return baos.toByteArray()
    }
}