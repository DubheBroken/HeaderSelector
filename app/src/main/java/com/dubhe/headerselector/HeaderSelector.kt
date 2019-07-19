package com.dubhe.headerselector

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.dubhe.headerselector.Path.Companion.REQUEST_CROP_PHOTO
import com.dubhe.headerselector.Path.Companion.REQUEST_OPEN_CAMERA
import com.dubhe.headerselector.Path.Companion.REQUEST_SYSTEM_PIC
import com.dubhe.headerselector.Path.Companion.imgPath
import com.dubhe.headerselector.Path.Companion.imgPathOri
import com.dubhe.headerselector.Path.Companion.imgUriOri
import com.dubhe.wang.ClipImageActivity
import java.io.File

class HeaderSelector(private var mActivity: AppCompatActivity) {

    var enableClip = false//是否裁剪图片
    var enableZip = false//是否压缩图片
    var onProcessFinishListener: OnProcessFinishListener? = null//TODO:这个监听器必须不为空，否则将无法返回最终结果给调用者
    var clipMode = ClipImageActivity.TYPE_CIRCLE//裁图模式，默认圆形

    interface OnProcessFinishListener {
        fun onProcessFinish(path: String)//返回最终结果的path
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: HeaderSelector? = null

        fun getInstance(mActivity: AppCompatActivity): HeaderSelector {
            if (instance == null) {
                instance = HeaderSelector(mActivity)
            } else {
                instance?.setToDefault()
            }
            return instance as HeaderSelector
        }
    }

    /**
     * 回到默认状态
     */
    fun setToDefault() {
        instance?.enableClip = false
        instance?.enableZip = false
        instance?.clipMode = ClipImageActivity.TYPE_CIRCLE
        instance?.onProcessFinishListener = null
    }

    /**
     * 选图返回
     * TODO:请在调用的Activtiy的onActivityResult方法中调用此方法并原样传入参数
     */
    fun onHeaderResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_OPEN_CAMERA -> {
                    //照相返回,进裁剪
                    if (enableClip) {
                        gotoClipActivity(imgUriOri)
                    } else {
                        if (onProcessFinishListener != null) {
                            onProcessFinishListener?.onProcessFinish(
                                ImageUtils.getRealPathFromUri(mActivity, imgUriOri!!)
                            )
                        }
                    }

                }
                REQUEST_SYSTEM_PIC -> {
                    //选图返回,进裁剪
                    val uri = data!!.data
                    imgPathOri = ImageUtils.getRealPathFromUri(mActivity, uri)
                    if (enableClip) {
                        gotoClipActivity(uri)
                    } else {
                        if (onProcessFinishListener != null) {
                            onProcessFinishListener?.onProcessFinish(imgPathOri!!)
                        }
                    }
                }
                REQUEST_CROP_PHOTO -> {
                    //裁剪完成,上传
                    imgPath = data!!.getStringExtra("path")
                    if (onProcessFinishListener != null) {
                        onProcessFinishListener?.onProcessFinish(imgPath)
                    }
                }
            }
        }
    }

    /**
     * 打开裁图界面
     */
    fun gotoClipActivity(uri: Uri?) {
        if (uri == null) {
            return
        }
        val intent = Intent()
        intent.setClass(mActivity, ClipImageActivity::class.java)
        intent.putExtra("type", clipMode)
        intent.putExtra("path", imgPathOri)
        intent.data = uri
//        Logger.d("uri:  $uri")
        mActivity.startActivityForResult(intent, REQUEST_CROP_PHOTO)
    }

}