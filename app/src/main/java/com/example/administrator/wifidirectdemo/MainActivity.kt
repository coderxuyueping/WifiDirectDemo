package com.example.administrator.wifidirectdemo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.halove.wifidirectmanager.WifiDirectActionListener
import com.halove.wifidirectmanager.WifiDirectManager
import com.halove.wifidirectmanager.protocol.BasicProtocol
import com.halove.wifidirectmanager.protocol.DataProtocol
import com.halove.wifidirectmanager.protocol.FileProtocol
import com.halove.wifidirectmanager.socketutils.Config
import com.halove.wifidirectmanager.socketutils.FileUtils
import com.halove.wifidirectmanager.socketutils.SendSocketTask
import com.halove.wifidirectmanager.socketutils.ServerSocketIntentService
import com.halove.wifidirectmanager.toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    var mWifiP2pDevice: WifiP2pDevice? = null
    var mWifiP2pInfo: WifiP2pInfo? = null
    var serverSocket: ServerSocketIntentService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //开启服务获取数据
        bindService()

        connect_state_btn.setOnClickListener { disConnect() }

        WifiDirectManager.init(this, object : WifiDirectActionListener {
            override fun wifiP2pEnabled(enabled: Boolean) {
            }

            override fun onConnectionInfo(wifiP2pInfo: WifiP2pInfo) {
                //设备连接开启服务接收客户端消息
                startService(Intent(this@MainActivity, ServerSocketIntentService::class.java))
                mWifiP2pInfo = wifiP2pInfo
                connect_state_btn.text = "已连接,点击将断开连接"
                connect_state_btn.isEnabled = true
                connect_info.text = "连接设备的名字:${mWifiP2pDevice?.deviceName} \n 连接设备的地址:${mWifiP2pDevice?.deviceAddress} \n 是否是群主:${wifiP2pInfo.isGroupOwner} \n 群主地址:${wifiP2pInfo.groupOwnerAddress}"
                toast("连接成功")
            }

            override fun onDisconnection() {
                connect_state_btn.text = "未连接"
                connect_info.text = ""
                connect_state_btn.isEnabled = false
                mWifiP2pInfo = null
                toast("断开连接")
            }

            override fun onSelfDeviceInfo(wifiP2pDevice: WifiP2pDevice) {
                selDevice.text = "本机名称：${wifiP2pDevice.deviceName}\n本机mac地址:${wifiP2pDevice.deviceAddress}"
            }

            override fun onPeersDevices(wifiP2pDeviceList: Collection<WifiP2pDevice>) {
                Log.d("xuyueping", "找到${wifiP2pDeviceList.size}个设备")
                var array = ArrayList<String>()
                wifiP2pDeviceList.forEach {
                    array.add("name is ${it.deviceName} \n address is ${it.deviceAddress}")
                }
                list.adapter = ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_list_item_1, array)
                list.onItemClickListener = AdapterView.OnItemClickListener { p0, p1, p2, p3 ->
                    mWifiP2pDevice = wifiP2pDeviceList.elementAt(p2)
                    WifiDirectManager.connect(mWifiP2pDevice)
                }
            }

            override fun onChannelDisconnected() {
            }

        })
    }

    fun scanDevice(view: View) {

        WifiDirectManager.discoverPeers()
    }

    fun sendFile(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, 1)
    }

    fun sendText(view: View) {
        var data = DataProtocol()
        if (mWifiP2pInfo!!.isGroupOwner) {
            data.data = "服务端向你说hi"
            SendSocketTask(data).execute(Config.CLIENT_IP)
        } else {
            data.data = "客户端端向你说hi"
            SendSocketTask(data).execute(mWifiP2pInfo!!.groupOwnerAddress.hostAddress)
        }
    }

    private fun disConnect() {
        WifiDirectManager.disConnect()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                val uri = data?.data
                if (uri != null) {
                    val path = FileUtils.getPath(this, uri)
                    if (path != null) {
                        val file = File(path)
                        if (file.exists() && mWifiP2pInfo != null) {
                            if (mWifiP2pInfo!!.isGroupOwner)
                                SendSocketTask(FileProtocol(file)).execute(Config.CLIENT_IP)
                            else
                                SendSocketTask(FileProtocol(file)).execute(mWifiP2pInfo!!.groupOwnerAddress.hostAddress)
                        }
                    }
                }
            }
        }
    }

    private fun bindService() {
        val intent = Intent(this@MainActivity, ServerSocketIntentService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }


    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ServerSocketIntentService.MyBinder
            serverSocket = binder.service
            serverSocket?.setReceiveCallback(object : ServerSocketIntentService.IMessageReceiveCallback {
                override fun onReceive(data: BasicProtocol) {
                    Log.d("xuyueping", "接受到数据$data")
                    if (data.getProtocolType() == Config.PROTOCOL_TYPE_FILE) {
                        //文件
                        var fileProtocol: FileProtocol = data as FileProtocol
                        FileUtils.openFile(this@MainActivity,  FileUtils.getFilePath(fileProtocol.fileName?:""))
                    } else if (data.getProtocolType() == Config.PROTOCOL_TYPE_DATA) {
                        val msgProtocol = data as DataProtocol
                        runOnUiThread { toast(msgProtocol.data ?: "空数据") }
                    }
                }

            })
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serverSocket = null
            //重启服务
            bindService()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
        stopService(Intent(this, ServerSocketIntentService::class.java))
        WifiDirectManager.release()
    }

}
