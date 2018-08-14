package com.halove.wifidirectmanager.protocol

import android.util.Log
import com.halove.wifidirectmanager.socketutils.FileUtils
import com.halove.wifidirectmanager.socketutils.Config
import com.halove.wifidirectmanager.socketutils.SocketUtil
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.UnsupportedEncodingException

/**
 * 文件类型数据包
 * Created by yanglijun on 18-8-9.
 */
class FileProtocol(var file: File?) : BasicProtocol() {

    var fileName: String? = null//用来区分是图片还是文本文件，在传输的时候由文件获取，在接收的时候去解析

    private val fileByte by lazy {
        FileUtils.fileToByte(file)
    }

    private val fileNameByte by lazy {
        file?.name?.toByteArray()
    }

    override fun getProtocolType(): Int {
        return Config.PROTOCOL_TYPE_FILE
    }

    override fun getLength(): Int {
        return super.getLength() + fileByte.size + fileNameByte!!.size
    }

    override fun getContentData(): ByteArray {
        val base = super.getContentData()
        val baos = ByteArrayOutputStream(getLength())
        baos.write(base, 0, base.size)          //协议版本＋数据类型＋数据长度＋文件长度+文件字节 + 文件名称
        //文件长度，用來解析出后面的文件名
        baos.write(SocketUtil.int2ByteArrays(fileByte.size),0, 4)
        baos.write(fileByte, 0, fileByte.size)
        baos.write(fileNameByte, 0, fileNameByte!!.size)
        return baos.toByteArray()
    }

    override fun parseContentData(data: ByteArray): Int {
        val pos = super.parseContentData(data)
        val fileLengthByte = ByteArray(4)
        System.arraycopy(data, pos, fileLengthByte, 0, fileLengthByte.size)
        val fileLength = SocketUtil.byteArrayToInt(fileLengthByte)
        val fileData = ByteArray(fileLength)
        System.arraycopy(data, pos+4, fileData, 0, fileData.size)

        val fileNameByte = ByteArray(data.size - pos - fileData.size-4)
        System.arraycopy(data, pos +4 + fileData.size, fileNameByte, 0, fileNameByte.size)

        fileName = String(fileNameByte, Charsets.UTF_8)
        Log.d("xuyueping", "接收到的文件名是:$fileName")

        try {
            FileUtils.saveFileFromBytes(fileData, FileUtils.getFilePath(fileName!!))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return pos
    }
}