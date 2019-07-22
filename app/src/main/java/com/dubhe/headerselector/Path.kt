package com.dubhe.headerselector

import android.net.Uri

class Path {
    companion object {
        @JvmField
        var imgPath: String = ""
        val REQUEST_SYSTEM_PIC = 1//打开系统相册
        //请求码
        val REQUEST_OPEN_CAMERA = 2//打开相机
        val REQUEST_PERMISSIONS = 3//请求权限
        val REQUEST_CROP_PHOTO = 4//裁图

        //结果错误码
        val CAMERA_OPEN_FAIL = 1//打开相机失败
        val FILE_SYSTEM_FAIL = 2
        val INTERNET_ERROR = 3
        val SERVER_ERROR = 4

        var imgPathOri: String? = null
        var imgUriOri: Uri? = null
    }
}