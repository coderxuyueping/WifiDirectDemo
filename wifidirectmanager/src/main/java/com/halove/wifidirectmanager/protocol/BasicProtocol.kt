package com.halove.wifidirectmanager.protocol

import android.util.Log
import com.halove.wifidirectmanager.socketutils.Config
import com.halove.wifidirectmanager.socketutils.SocketUtil
import java.io.ByteArrayOutputStream

/**
 * 基础协议包
 * Created by yanglijun on 18-8-8.
 */
abstract class BasicProtocol {

    private var reserved = 0                     //预留信息
    private var version = Config.VERSION         //版本号

    companion object {
        // 长度均以字节（byte）为单位
        val LENGTH_LEN = 4       //记录整条数据长度数值的长度
        protected val VER_LEN = 1       //协议的版本长度（其中前3位作为预留位，后5位作为版本号）
        protected val TYPE_LEN = 1      //协议的数据类型长度

        /**
         * 解析出协议类型
         *
         * @param data
         * @return
         */
        fun parseType(data: ByteArray): Int {
            val t = data[LENGTH_LEN + VER_LEN]//前4个字节（0，1，2，3）为数据长度的int值，以及ver占一个字节
            return t.toInt() and 0xFF
        }

    }

    /**
     * 获取整条数据长度
     * 单位：字节（byte）
     *
     * @return
     */
    open fun getLength(): Int {
        return LENGTH_LEN + VER_LEN + TYPE_LEN
    }

    open fun getReserved(): Int {
        return reserved
    }

    open fun setReserved(reserved: Int) {
        this.reserved = reserved
    }

    open fun getVersion(): Int {
        return version
    }

    open fun setVersion(version: Int) {
        this.version = version
    }

    /**
     * 获取协议类型，由子类实现
     *
     * @return
     */
    abstract fun getProtocolType(): Int

    /**
     * 由预留值和版本号计算完整版本号的byte[]值
     *
     * @return
     */
    private fun getVer(r: Byte, v: Byte, vLen: Int): Int {
        var num = 0
        val rLen = 8 - vLen
        for (i in 0 until rLen) {
            num += r.toInt() shr rLen - 1 - i and 0x1 shl 7 - i
        }
        return num + v
    }

    /**
     * 拼接发送数据，此处拼接了协议版本、协议类型和数据长度，具体内容子类中再拼接
     * 按顺序拼接
     *
     * @return
     */
    open fun getContentData(): ByteArray {
        Log.e("runtime", "开始3${System.currentTimeMillis()}")
        val length = SocketUtil.int2ByteArrays(getLength())
        val reserved = getReserved().toByte()
        val version = getVersion().toByte()
        val ver = byteArrayOf(getVer(reserved, version, 5).toByte())
        val type = byteArrayOf(getProtocolType().toByte())

        val baos = ByteArrayOutputStream(LENGTH_LEN + VER_LEN + TYPE_LEN)
        baos.write(length, 0, LENGTH_LEN)
        baos.write(ver, 0, VER_LEN)
        baos.write(type, 0, TYPE_LEN)
        return baos.toByteArray()
    }

    /**
     * 解析出整条数据长度
     *
     * @param data
     * @return
     */
    open fun parseLength(data: ByteArray): Int {
        return SocketUtil.byteArrayToInt(data, 0, LENGTH_LEN)
    }

    /**
     * 解析出预留位
     *
     * @param data
     * @return
     */
    open fun parseReserved(data: ByteArray): Int {
        val r = data[LENGTH_LEN]//前4个字节（0，1，2，3）为数据长度的int值，与版本号组成一个字节
        return r.toInt() shr 5 and 0xFF
    }

    /**
     * 解析出版本号
     *
     * @param data
     * @return
     */
    open fun parseVersion(data: ByteArray): Int {
        val v = data[LENGTH_LEN] //与预留位组成一个字节
        return v.toInt() shl 3 and 0xFF shr 3
    }


    /**
     * 解析接收数据，此处解析了协议版本、协议类型和数据长度，具体内容子类中再解析
     *
     * @param data
     * @return
     * @throws ProtocolException 协议版本不一致，抛出异常
     */
    @Throws(ProtocolException::class)
    open fun parseContentData(data: ByteArray): Int {
//        val reserved = parseReserved(data)
        val version = parseVersion(data)
//        val protocolType = parseType(data)
        if (version != getVersion()) {
            throw ProtocolException("input version is error: $version")
        }
        return LENGTH_LEN + VER_LEN + TYPE_LEN
    }

    override fun toString(): String {
        return "Version: " + getVersion() + ", Type: " + getProtocolType()
    }

}