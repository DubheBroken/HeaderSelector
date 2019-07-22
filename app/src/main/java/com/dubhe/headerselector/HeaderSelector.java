package com.dubhe.headerselector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.os.Build.VERSION;
import android.text.Layout;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.dubhe.wang.ClipImageActivity;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;

import kotlin.Metadata;
import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.Intrinsics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static android.app.Activity.RESULT_OK;

public class HeaderSelector {
    private boolean enableClip;
    private OnProcessFinishListener onProcessFinishListener;
    private int clipMode;
    private Dialog imageSelectDialog;
    private AppCompatActivity mActivity;
    private static HeaderSelector instance;

    public boolean getEnableClip() {
        return this.enableClip;
    }

    public HeaderSelector.OnProcessFinishListener getOnProcessFinishListener() {
        return this.onProcessFinishListener;
    }

    public int getClipMode() {
        return this.clipMode;
    }

    public HeaderSelector setEnableClip(boolean b) {
        this.enableClip = b;
        return this;
    }

    public HeaderSelector setOnProcessFinishListener(@NotNull HeaderSelector.OnProcessFinishListener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        this.onProcessFinishListener = listener;
        return this;
    }

    public HeaderSelector setClipMode(int mode) {
        this.clipMode = mode;
        return this;
    }

    public final void clear() {
        if (instance != null) {
            instance.enableClip = false;
            instance.clipMode = 1;
            instance.onProcessFinishListener = null;
        }
        instance = null;
    }

    public final void showImageSelectMenu() {
        imageSelectDialog.show();
        imageSelectDialog.setCanceledOnTouchOutside(true);//点击窗口外消失

        LinearLayout fromCamera = imageSelectDialog.getWindow().findViewById(R.id.linear_camera);
        LinearLayout fromAlbum = imageSelectDialog.getWindow().findViewById(R.id.linear_album);

        fromCamera.setOnClickListener(onClickListener);
        fromAlbum.setOnClickListener(onClickListener);
    }

    @SuppressLint({"LongLogTag"})
    private final void openCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File oriPhotoFile = (File) null;

        try {
            oriPhotoFile = this.createOriImageFile();
        } catch (IOException var5) {
            this.showErrorMessage(Path.FILE_SYSTEM_FAIL);
            var5.printStackTrace();
        }

        if (oriPhotoFile != null) {
            if (VERSION.SDK_INT < 24) {
                Path.imgUriOri = Uri.fromFile(oriPhotoFile);
            } else {
                Path.imgUriOri = (FileProvider.getUriForFile(this.mActivity, this.mActivity.getPackageName() + ".fileProvider", oriPhotoFile));
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra("output", Path.imgUriOri);
            try {
                this.mActivity.startActivityForResult(intent, Path.REQUEST_OPEN_CAMERA);
            } catch (Exception var4) {
                this.showErrorMessage(Path.CAMERA_OPEN_FAIL);
                var4.printStackTrace();
            }
        }

    }

    private void showErrorMessage(int errorCode) {
        String message = (String) null;
        if (errorCode == Path.CAMERA_OPEN_FAIL) {
            message = "打开相机失败，请允许相机权限";
        } else if (errorCode == Path.FILE_SYSTEM_FAIL) {
            message = "保存照片失败，请允许文件存储权限";
        } else if (errorCode == Path.INTERNET_ERROR) {
            message = "连接服务器失败，请检查网络";
        } else if (errorCode == Path.SERVER_ERROR) {
            message = "服务器异常，请联系管理员";
        }
        Toast.makeText(this.mActivity, message, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SimpleDateFormat")
    private File createOriImageFile() throws IOException {
        String imgNameOri = "Pic" + (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
        File var10002 = this.mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File pictureDirOri = new File(Intrinsics.stringPlus(var10002 != null ? var10002.getAbsolutePath() : null, "/OriPicture"));
        if (!pictureDirOri.exists()) {
            pictureDirOri.mkdirs();
        }

        File image = File.createTempFile(imgNameOri, ".jpg", pictureDirOri);
        Intrinsics.checkExpressionValueIsNotNull(image, "image");
        Path.imgPathOri = image.getAbsolutePath();
        return image;
    }

    private final void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        this.mActivity.startActivityForResult(intent, Path.REQUEST_SYSTEM_PIC);
    }

    public void onHeaderResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Path.REQUEST_OPEN_CAMERA: {
                    //相机拍照返回
                    if (this.enableClip) {
                        this.gotoClipActivity(Path.imgUriOri);
                    } else if (this.onProcessFinishListener != null) {
                        onProcessFinishListener.onProcessFinish(ImageUtils.getRealPathFromUri(mActivity, Path.imgUriOri));
                    }
                    break;
                }
                case Path.REQUEST_SYSTEM_PIC: {
                    //相册选图返回
                    if (data != null) {
                        Uri uri = data.getData();
                        Path.imgPathOri = (ImageUtils.getRealPathFromUri(this.mActivity, uri));
                        if (this.enableClip) {
                            this.gotoClipActivity(uri);
                        } else if (this.onProcessFinishListener != null) {
                            onProcessFinishListener.onProcessFinish(Path.imgPathOri);
                        }
                    } else {
                        Toast.makeText(mActivity, "获取图片失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case Path.REQUEST_CROP_PHOTO: {
                    //裁图返回
                    if (data != null) {
                        Path.imgPath = data.getStringExtra("path");
                        if (this.onProcessFinishListener != null) {
                            onProcessFinishListener.onProcessFinish(Path.imgPath);
                        }
                    } else {
                        Toast.makeText(mActivity, "获取图片失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
        }
    }

    public final void gotoClipActivity(@Nullable Uri uri) {
        if (uri != null) {
            Intent intent = new Intent();
            intent.setClass(this.mActivity, ClipImageActivity.class);
            intent.putExtra("type", this.clipMode);
            intent.putExtra("path", Path.imgPathOri);
            intent.setData(uri);
            this.mActivity.startActivityForResult(intent, Path.REQUEST_CROP_PHOTO);
        }
    }

    public HeaderSelector(@NotNull AppCompatActivity mActivity) {
        imageSelectDialog = new Dialog(mActivity, R.style.BottomDialog);
        Window window = imageSelectDialog.getWindow();
        imageSelectDialog.setContentView(R.layout.image_select_bottom_menu);
        window.setGravity(Gravity.BOTTOM);
        //        window.getDecorView().setPadding(0, 0, 0, 0);
        window.setWindowAnimations(R.style.BottomDialog_Animation);
        LayoutParams lp = window.getAttributes();
        //设置宽
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        //设置高
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        this.mActivity = mActivity;
    }

    private OnClickListener onClickListener = v -> {
        switch (v.getId()) {
            case R.id.linear_camera: {
                //点击弹出框中的相机按钮
                imageSelectDialog.dismiss();
                openCamera();
                break;
            }
            case R.id.linear_album: {
                //点击弹出框中的相册
                imageSelectDialog.dismiss();
                if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
//                    打开系统相册
                    openAlbum();
                }
                break;
            }
        }
    };

    public static HeaderSelector getInstance(@NotNull AppCompatActivity mActivity) {
        if (instance == null || instance.mActivity == null) {
            instance = new HeaderSelector(mActivity);
        }
        return instance;
    }

    public interface OnProcessFinishListener {
        void onProcessFinish(String path);
    }

}
