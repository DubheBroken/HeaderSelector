package com.dubhe.simple;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.dubhe.headerselector.R;
import com.dubhe.imageselector.ClipImageActivity;
import com.dubhe.imageselector.ImageSelector;

@SuppressLint("Registered")
public class MainActivity_java extends AppCompatActivity {

    ImageView img;
    CheckBox checkClip;
    RadioGroup radioGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img = findViewById(R.id.img);
        checkClip = findViewById(R.id.checkClip);
        radioGroup = findViewById(R.id.radioGroup);
        initImageSelector();
        img.setOnClickListener(v -> {
            ImageSelector.getInstance(this)
                    .setEnableClip(checkClip.isChecked());//是否裁剪图片
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.radioCir: {
                    ImageSelector.getInstance(this).setClipMode(ClipImageActivity.TYPE_CIRCLE);
                    break;
                }
                case R.id.radioRec: {
                    ImageSelector.getInstance(this).setClipMode(ClipImageActivity.TYPE_RECTANGLE);
                    break;
                }
            }
            ImageSelector.getInstance(this).showImageSelectMenu();//显示图片选择器
        });
    }

    private void initImageSelector() {
        ImageSelector.getInstance(this)
                .setEnableClip(true)//是否裁剪图片
                .setClipMode(ClipImageActivity.TYPE_CIRCLE)//裁图模式 TYPE_CIRCLE圆形 TYPE_RECTANGLE矩形
                .setOnProcessFinishListener(new ImageSelector.OnProcessFinishListener() {
                    //完成所有操作后返回最终结果的path
                    //TODO:不set将会导致无法拿到返回结果
                    @Override
                    public void onProcessFinish(String path) {
                        //TODO:拿到path进行逻辑操作
                        Glide.with(img).load(path).into(img);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageSelector.getInstance(this).onHeaderResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageSelector.getInstance(this).clear();
    }

}
