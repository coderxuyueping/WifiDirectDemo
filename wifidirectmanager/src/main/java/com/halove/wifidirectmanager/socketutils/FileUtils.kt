package com.halove.wifidirectmanager.socketutils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.webkit.MimeTypeMap
import com.halove.wifidirectmanager.toast
import java.io.*
import java.util.*

/**
 * Created by xyp on 2018/8/9.
 */
object FileUtils {
    fun isSdCardExit() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    fun getFilePath(fileName: String) = Environment.getExternalStorageDirectory().absolutePath + "/$fileName"

    fun openFile(context: Context, filePath: String) {
        val ext = filePath.substring(filePath.lastIndexOf('.')).toLowerCase(Locale.US)
        try {
            val mimeTypeMap = MimeTypeMap.getSingleton()
            var mime = mimeTypeMap.getMimeTypeFromExtension(ext.substring(1))
            mime = if (TextUtils.isEmpty(mime)) "" else mime
            val intent = Intent()
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.action = Intent.ACTION_VIEW
            intent.setDataAndType(Uri.fromFile(File(filePath)), mime)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("xuyueping", "文件打开异常：$e.message")
            context.toast("文件打开异常：$e.message")
        }

    }

    fun getPath(context: Context, uri: Uri): String? {
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            val cursor = context.contentResolver.query(uri, arrayOf("_data"), null, null, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val data = cursor.getString(cursor.getColumnIndex("_data"))
                    cursor.close()
                    return data
                }
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    @JvmStatic
    fun saveFileFromBytes(b: ByteArray, outputFile: String): File {
        var stream: BufferedOutputStream? = null
        var file = File(outputFile)
        try {
            var fstream = FileOutputStream(file)
            stream = BufferedOutputStream(fstream)
            stream.write(b)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            stream?.close()
        }
        return file
    }

    //file转byte
    fun fileToByte(file: File?): ByteArray {
        file?.let {
            var fileInputStream = FileInputStream(file)
            var out = ByteArrayOutputStream()
            var bytes = ByteArray(1024)
            var len = 0
            while (fileInputStream.read(bytes).apply { len = this } != -1) {
                out.write(bytes, 0, len)
            }

            return out.toByteArray()
        }
        return ByteArray(0)
    }
}