package com.dubhe.headerselector

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.dubhe.headerselector.Path.Companion.CAMERA_OPEN_FAIL
import com.dubhe.headerselector.Path.Companion.FILE_SYSTEM_FAIL
import com.dubhe.headerselector.Path.Companion.INTERNET_ERROR
import com.dubhe.headerselector.Path.Companion.REQUEST_CROP_PHOTO
import com.dubhe.headerselector.Path.Companion.REQUEST_OPEN_CAMERA
import com.dubhe.headerselector.Path.Companion.REQUEST_SYSTEM_PIC
import com.dubhe.headerselector.Path.Companion.SERVER_ERROR
import com.dubhe.headerselector.Path.Companion.imgPath
import com.dubhe.headerselector.Path.Companion.imgPathOri
import com.dubhe.headerselector.Path.Companion.imgUriOri
import com.dubhe.wang.ClipImageActivity
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class HeaderSelector(private var mActivity: AppCompatActivity) {

    var enableClip = false //是否裁剪图片
    var enableZip = false//是否压缩图片
    var onProcessFinishListener: OnProcessFinishListener? = null//TODO:这个监听器必须不为空，否则将无法返回最终结果给调用者
    var clipMode = ClipImageActivity.TYPE_CIRCLE//裁图模式，默认圆形
    private var imageSelectDialog = Dialog(mActivity, R.style.BottomDialog)

    interface OnProcessFinishListener {
        fun onProcessFinish(path: String)//返回最终结果的path
    }

    fun setEnableClip(b: Boolean): HeaderSelector {
        this.enableClip = b
        return this
    }

    fun setEnableZip(b: Boolean): HeaderSelector {
        this.enableZip = b
        return this
    }

    fun setOnProcessFinishListener(listener: OnProcessFinishListener): HeaderSelector {
        this.onProcessFinishListener = listener
        return this
    }

    fun setClipMode(mode: Int): HeaderSelector {
        this.clipMode = mode
        return this
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: HeaderSelector? = null

        fun getInstance(mActivity: AppCompatActivity): HeaderSelector {
            if (instance == null || instance?.mActivity == null) {
                instance = HeaderSelector(mActivity)
            }
            return instance as HeaderSelector
        }
    }

    init {
        val window = imageSelectDialog.window
        imageSelectDialog.setContentView(R.layout.image_select_bottom_menu)
        window!!.setGravity(Gravity.BOTTOM)
        //        window.getDecorView().setPadding(0, 0, 0, 0);
        window.setWindowAnimations(R.style.BottomDialog_Animation)
        val lp = window.attributes
        //设置宽
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        //设置高
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = lp
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
     * 显示底部相册相机选择对话框
     */
    fun showImageSelectMenu() {
        imageSelectDialog.show()
        imageSelectDialog.setCanceledOnTouchOutside(true)//点击窗口外消失

        val fromCamera =
                imageSelectDialog.window!!.findViewById<LinearLayout>(R.id.linear_camera)
        val fromAlbum =
                imageSelectDialog.window!!.findViewById<LinearLayout>(R.id.linear_album)

        fromCamera.setOnClickListener(onClickListener)
        fromAlbum.setOnClickListener(onClickListener)
    }

    private var onClickListener = View.OnClickListener { v ->
        when (v!!.id) {
            R.id.linear_camera -> {
                //点击弹出框中的相机按钮
                imageSelectDialog.dismiss()
                openCamera()
                return@OnClickListener
            }
            R.id.linear_album -> {
                //点击弹出框中的相册
                imageSelectDialog.dismiss()
                if (ContextCompat.checkSelfPermission(
                                mActivity,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                            mActivity,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            1
                    )
                } else {
//                    打开系统相册
                    openAlbum()
                }
                return@OnClickListener
            }
        }
    }

    /**
     * 打开相机
     * 7.0中如果需要调用系统(eg:裁剪)/其他应用，必须用FileProvider提供Content Uri，并且将Uri赋予读写的权限
     */
    @SuppressLint("LongLogTag")
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)// 打开相机
        var oriPhotoFile: File? = null
        try {
            oriPhotoFile = createOriImageFile()
        } catch (e: IOException) {
            showErrorMessage(FILE_SYSTEM_FAIL)
            e.printStackTrace()
        }

        if (oriPhotoFile != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                imgUriOri = Uri.fromFile(oriPhotoFile)
            } else {
                imgUriOri = FileProvider.getUriForFile(mActivity, "${mActivity.packageName}.fileProvider", oriPhotoFile)
            }
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUriOri)
            try {
                mActivity.startActivityForResult(intent, REQUEST_OPEN_CAMERA)
            } catch (e: Exception) {
                showErrorMessage(CAMERA_OPEN_FAIL)
                e.printStackTrace()
            }
//            Logger.d("openCamera_imgPathOri:$imgPathOri")
//            Logger.d("openCamera_imgUriOri:" + imgUriOri.toString())
        }
    }

    /**
     * 显示错误信息
     *
     * @param errorCode 错误代码
     */
    private fun showErrorMessage(errorCode: Int) {
        var message: String? = null
        when (errorCode) {
            CAMERA_OPEN_FAIL -> {
                message = "打开相机失败，请允许相机权限"
//                initPermission()
            }
            FILE_SYSTEM_FAIL -> message = "保存照片失败，请允许文件存储权限"
            INTERNET_ERROR -> message = "连接服务器失败，请检查网络"
            SERVER_ERROR -> message = "服务器异常，请联系管理员"
        }
        Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * 创建图片文件
     */
    @Throws(IOException::class)
    private fun createOriImageFile(): File {
        val imgNameOri = "Pic" + SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val pictureDirOri =
                File(mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath + "/OriPicture")
        if (!pictureDirOri.exists()) {
            pictureDirOri.mkdirs()
        }
        val image = File.createTempFile(
                imgNameOri, /* prefix */
                ".jpg", /* suffix */
                pictureDirOri       /* directory */
        )
        imgPathOri = image.absolutePath
        return image
    }

    /**
     * 进入图库
     * 打开图库权限
     */
    private fun openAlbum() {
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        mActivity.startActivityForResult(intent, REQUEST_SYSTEM_PIC)//打开系统相册
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