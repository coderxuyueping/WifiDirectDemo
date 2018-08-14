package com.halove.wifidirectmanager

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager

/**
 * Created by xyp on 2018/8/9.
 *
 */
interface WifiDirectActionListener : WifiP2pManager.ChannelListener {

    //wifi是否可用
    fun wifiP2pEnabled(enabled: Boolean)

    //连接上返回的设备信息
    fun onConnectionInfo(wifiP2pInfo: WifiP2pInfo)

    //断开连接
    fun onDisconnection()

    //本设备信息
    fun onSelfDeviceInfo(wifiP2pDevice: WifiP2pDevice)

    //搜索到的附近可用设备信息
    fun onPeersDevices(wifiP2pDeviceList: Collection<WifiP2pDevice>)
}