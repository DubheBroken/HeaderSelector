package com.dubhe.headerselector

import android.content.Intent

interface HeaderSelectorInterface {

    fun onHeaderResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun onHeaderProcessFinish(): String

}