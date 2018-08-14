package com.halove.wifidirectmanager

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import android.util.Log

@SuppressLint("StaticFieldLeak")
/**
 * Created by xyp on 2018/8/9.
 * wifi直连管理类
 */
object WifiDirectManager {
    private val TAG = "xuyueping"

    var context: Context? = null
    var mWifiP2pManager: WifiP2pManager? = null
    var mChannel: WifiP2pManager.Channel? = null
    var mWifiDirectBroadcastReceiver: WifiDirectBroadcastReceiver? = null

    fun init(context: Context?, wifiDirectListener: WifiDirectActionListener) {
        WifiDirectManager.context = context
        initBroadcast(wifiDirectListener)
        mWifiP2pManager = context?.applicationContext?.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        mChannel = mWifiP2pManager?.initialize(context, Looper.getMainLooper(), wifiDirectListener)
    }

    //注册广播
    private fun initBroadcast(wifiDirectListener: WifiDirectActionListener) {
        mWifiDirectBroadcastReceiver = WifiDirectBroadcastReceiver(mWifiP2pManager, mChannel, wifiDirectListener)
        context?.applicationContext?.registerReceiver(mWifiDirectBroadcastReceiver, WifiDirectBroadcastReceiver.getIntentFilter())
    }

    //广播反注册
    private fun unRegisterReceiver() {
        context?.applicationContext?.unregisterReceiver(mWifiDirectBroadcastReceiver)
    }

    //搜索附近设备并把自己暴露出去
    fun discoverPeers() {
        mWifiP2pManager?.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "discoverPeers success")
            }

            override fun onFailure(code: Int) {
                Log.d(TAG, "discoverPeers failure code is $code")
            }

        })
    }

    fun stopDisCoverPeers(){
        mWifiP2pManager?.stopPeerDiscovery(mChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
            }

            override fun onFailure(p0: Int) {
            }
        })
    }


    //连接到设备
    fun connect(mWifiP2pDevice: WifiP2pDevice?) {
        val config = WifiP2pConfig()
        config.deviceAddress = mWifiP2pDevice?.deviceAddress
        config.wps.setup = WpsInfo.PBC
        mWifiP2pManager?.connect(mChannel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.e(TAG, "connect onSuccess")
                stopDisCoverPeers()
            }

            override fun onFailure(code: Int) {
                Log.e(TAG, "connect failure code is $code")
                discoverPeers()
            }
        })
    }

    //断开连接
    fun disConnect() {
        removeGroup()
        mWifiP2pManager?.cancelConnect(mChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.e(TAG, "cancelConnect success")
            }

            override fun onFailure(reason: Int) {
                Log.e(TAG, "cancelConnect failure")
            }
        })
    }

    //创建群组,如果直接连接的话，系统会默认分配一个群组
    fun createGroup() {
        mWifiP2pManager?.createGroup(mChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.e(TAG, "createGroup onSuccess")
            }

            override fun onFailure(code: Int) {
                Log.e(TAG, "createGroup onFailure code is $code")
            }
        })
    }

    //移除群組，此时会断开连接
    private fun removeGroup() {
        mWifiP2pManager?.removeGroup(mChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.e(TAG, "removeGroup onSuccess")
            }

            override fun onFailure(reason: Int) {
                Log.e(TAG, "removeGroup onFailure")
            }
        })
    }


    /**
     * 对一些资源的释放
     */
    fun release() {
        disConnect()
        unRegisterReceiver()
    }

}