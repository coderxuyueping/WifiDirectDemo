package com.halove.wifidirectmanager.socketutils

/**
 * Created by yanglijun on 18-8-8.
 */

object Config {
    val VERSION = 1                 //协议版本号
    val PORT = 9013                 //服务器端口号

    val PROTOCOL_TYPE_HEART = 0
    val PROTOCOL_TYPE_REPLY = 1
    val PROTOCOL_TYPE_DATA = 2
    @JvmField
    val PROTOCOL_TYPE_FILE = 3

    var CLIENT_IP: String? = null //客户端的ip
}