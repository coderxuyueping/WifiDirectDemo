package com.halove.wifidirectmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Parcelable
import android.util.Log

/**
 * Created by xyp on 2018/8/9.
 * 监听wifi p2p的一些广播消息
 */
class WifiDirectBroadcastReceiver(private var mWifiP2pManager: WifiP2pManager?, private var mWifiChannel: WifiP2pManager.Channel?, private var listener: WifiDirectActionListener?): BroadcastReceiver() {

    private val TAG: String = "xuyueping"

    companion object {
        fun getIntentFilter(): IntentFilter{
            return IntentFilter().apply {
                addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
                addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
                addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
                addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
        // 用于指示 Wifi P2P 是否可用
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    listener?.wifiP2pEnabled(true)
                } else {
                    listener?.wifiP2pEnabled(false)
                }
            }
        // 对等节点列表发生了变化
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                mWifiP2pManager?.requestPeers(mWifiChannel) {
                    peers -> listener?.onPeersDevices(peers.deviceList)
                }
            }
        // Wifi P2P 的连接状态发生了改变
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                if (networkInfo.isConnected) {
                    mWifiP2pManager?.requestConnectionInfo(mWifiChannel) { info -> listener?.onConnectionInfo(info) }
                    Log.e(TAG, "已连接p2p设备")
                } else {
                    listener?.onDisconnection()
                    Log.e(TAG, "与p2p设备已断开连接")
                }
            }
        //本设备的设备信息发生了变化
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                listener?.onSelfDeviceInfo(intent.getParcelableExtra<Parcelable>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE) as WifiP2pDevice)
            }
        }
    }

}