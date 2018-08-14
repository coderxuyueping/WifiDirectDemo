package com.halove.wifidirectmanager

import android.content.Context
import android.widget.Toast

/**
 * Created by xyp on 2018/8/8.
 */
fun Context.toast(msg: String){
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}