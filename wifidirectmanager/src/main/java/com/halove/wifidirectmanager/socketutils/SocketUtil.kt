package com.halove.wifidirectmanager.socketutils

import android.graphics.Bitmap
import android.util.Log
import com.halove.wifidirectmanager.protocol.BasicProtocol
import com.halove.wifidirectmanager.protocol.DataProtocol
import com.halove.wifidirectmanager.protocol.FileProtocol
import com.halove.wifidirectmanager.protocol.HeartProtocol
import java.io.*
import java.nio.ByteBuffer


/**
 * Created by yanglijun on 18-8-8.
 */
object SocketUtil {
    private val msgImp = HashMap<Int, BasicProtocol>()

    init {
        msgImp[Config.PROTOCOL_TYPE_HEART] = HeartProtocol()       //0
        msgImp[Config.PROTOCOL_TYPE_DATA] = DataProtocol() //1
        msgImp[Config.PROTOCOL_TYPE_FILE] = FileProtocol(null) //3
    }

    /**
     * 解析数据内容
     *
     * @param data
     * @return
     */
    fun parseContentMsg(data: ByteArray): BasicProtocol? {
        val protocolType = BasicProtocol.parseType(data)
        var basicProtocol: BasicProtocol? = msgImp.get(protocolType)
        try {
            basicProtocol?.parseContentData(data)
        } catch (e: Exception) {
            basicProtocol = null
            e.printStackTrace()
        }

        return basicProtocol
    }

    /**
     * 读数据
     *
     * @param inputStream
     * @return
     */
    fun readFromStream(inputStream: InputStream): Pair<BasicProtocol?, Boolean> {
        val protocol: BasicProtocol?
        val bis: BufferedInputStream

        //header中保存的是整个数据的长度值，4个字节表示。在下述write2Stream方法中，会先写入header
        val header = ByteArray(BasicProtocol.LENGTH_LEN)

        try {
            bis = BufferedInputStream(inputStream)

            var temp: Int
            var len = 0
            while (len < header.size) {
                temp = bis.read(header, len, header.size - len)
                if (temp > 0) {
                    len += temp
                } else if (temp == -1) {
                    bis.close()
                    return Pair(null, false)
                }
            }

            len = 0
            val length = byteArrayToInt(header)//数据的长度值
            val content = ByteArray(length)
            while (len < length) {
                temp = bis.read(content, len, length - len)

                if (temp > 0) {
                    len += temp
                }
            }

            protocol = parseContentMsg(content)
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(null, false)
        }

        return Pair(protocol, true)
    }

    /**
     * 写数据
     *
     * @param protocol
     * @param outputStream
     */
    fun write2Stream(protocol: BasicProtocol, outputStream: OutputStream) {
        val bufferedOutputStream = BufferedOutputStream(outputStream)
        Log.e("runtime", "开始1${System.currentTimeMillis()}")
        val buffData = protocol.getContentData()
        val header = int2ByteArrays(buffData.size)
        try {
            bufferedOutputStream.write(header)
            bufferedOutputStream.write(buffData)
            bufferedOutputStream.flush()
            Log.d("send", "数据发送成功")
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            bufferedOutputStream.close()
        }
    }

    /**
     * 关闭输入流
     *
     * @param is
     */
    fun closeInputStream(`is`: InputStream?) {
        try {
            `is`?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    /**
     * 关闭输出流
     *
     * @param os
     */
    fun closeOutputStream(os: OutputStream?) {
        try {
            os?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun int2ByteArrays(i: Int): ByteArray {
        val result = ByteArray(4)
        result[0] = (i shr 24 and 0xFF).toByte()
        result[1] = (i shr 16 and 0xFF).toByte()
        result[2] = (i shr 8 and 0xFF).toByte()
        result[3] = (i and 0xFF).toByte()
        Log.e("runtime", "结束${System.currentTimeMillis()}")
        return result
    }

    fun byteArrayToInt(b: ByteArray): Int {
        var intValue = 0
        for (i in b.indices) {
            intValue += b[i].toInt() and 0xFF shl 8 * (3 - i) //int占4个字节（0，1，2，3）
        }
        return intValue
    }

    fun byteArrayToInt(b: ByteArray, byteOffset: Int, byteCount: Int): Int {
        var intValue = 0
        for (i in byteOffset until byteOffset + byteCount) {
            intValue += b[i].toInt() and 0xFF shl 8 * (3 - (i - byteOffset))
        }
        return intValue
    }

    fun bytes2Int(b: ByteArray, byteOffset: Int): Int {
        val byteBuffer = ByteBuffer.allocate(Integer.SIZE / java.lang.Byte.SIZE)
        byteBuffer.put(b, byteOffset, 4) //占4个字节
        byteBuffer.flip()
        return byteBuffer.getInt()
    }

    @JvmStatic
    fun bitmap2Bytes(bm: Bitmap): ByteArray {
        var baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }

}