package com.dubhe.simple

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.dubhe.headerselector.HeaderSelector
import com.dubhe.headerselector.R
import com.dubhe.wang.ClipImageActivity
import kotlinx.android.synthetic.main.activity_main.*

@SuppressLint("Registered")
class MainActivity_kotlin : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initHeaderSelector()
        img.setOnClickListener {
            HeaderSelector.getInstance(this)
                .setEnableClip(checkClip.isChecked)//是否裁剪图片
            when (radioGroup.checkedRadioButtonId) {
                R.id.radioCir -> {
                    HeaderSelector.getInstance(this).setClipMode(ClipImageActivity.TYPE_CIRCLE)
                }
                R.id.radioRec -> {
                    HeaderSelector.getInstance(this).setClipMode(ClipImageActivity.TYPE_RECTANGLE)
                }
            }
            HeaderSelector.getInstance(this).showImageSelectMenu()
        }
    }

    private fun initHeaderSelector() {
        HeaderSelector.getInstance(this)
            .setEnableClip(true)//是否裁剪图片
            .setClipMode(ClipImageActivity.TYPE_CIRCLE)//裁图模式 TYPE_CIRCLE圆形 TYPE_RECTANGLE矩形
            .setOnProcessFinishListener(object :
                HeaderSelector.OnProcessFinishListener {
                //完成所有操作后返回最终结果的path
                //TODO:不set将会导致无法拿到返回结果
                @SuppressLint("CheckResult")
                override fun onProcessFinish(path: String) {
                    //TODO:拿到path进行逻辑操作
                    Glide.with(img).load(path).into(img)
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        HeaderSelector.getInstance(this)
            .onHeaderResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        HeaderSelector.getInstance(this).clear()
    }
}
