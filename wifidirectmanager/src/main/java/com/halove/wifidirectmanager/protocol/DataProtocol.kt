package com.halove.wifidirectmanager.protocol

import com.halove.wifidirectmanager.socketutils.Config
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException

/**
 * 纯数据类型包
 * Created by yanglijun on 18-8-8.
 */
class DataProtocol : BasicProtocol() {

    var data: String? = null

    override fun getLength(): Int {
        val size = data?.toByteArray()?.size ?: 0
        return super.getLength() + size
    }

    override fun getProtocolType(): Int {
        return Config.PROTOCOL_TYPE_DATA
    }

    /**
     * 拼接发送数据
     *
     * @return
     */
    override fun getContentData(): ByteArray {
        val base = super.getContentData()

        val baos = ByteArrayOutputStream(getLength())
        baos.write(base, 0, base.size)          //协议版本＋数据类型＋数据长度＋消息id
        this.data?.let {
            val data = it.toByteArray()
            baos.write(data, 0, data.size)          //业务数据
        }
        return baos.toByteArray()
    }

    /**
     * 解析接收数据，按顺序解析
     *
     * @param data
     * @return
     * @throws ProtocolException
     */
    @Throws(ProtocolException::class)
    override fun parseContentData(data: ByteArray): Int {
        var pos = super.parseContentData(data)

        //解析data
        try {
            this.data = String(data, pos, data.size - pos, Charsets.UTF_8)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return pos
    }

    override fun toString(): String {
        return "data: $data"
    }
}