package com.dubhe.headerselector

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.dubhe.wang.ClipImageActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initHeaderSelector()
        img.setOnClickListener {
            HeaderSelector.getInstance(this).showImageSelectMenu()
        }
    }

    private fun initHeaderSelector() {
        HeaderSelector.getInstance(this)
                .setEnableClip(true)//是否裁剪图片
                .setEnableZip(true)//是否压缩图片 TODO:用鲁班压缩图片
                .setClipMode(ClipImageActivity.TYPE_CIRCLE)//裁图模式 TYPE_CIRCLE圆形 TYPE_RECTANGLE矩形
                .setOnProcessFinishListener(object : HeaderSelector.OnProcessFinishListener {
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
        HeaderSelector.getInstance(this).onHeaderResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        HeaderSelector.getInstance(this).setToDefault()
    }
}
