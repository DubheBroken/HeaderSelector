# HeaderSelector
【Android】弹框图片选择器，支持裁剪圆形矩形和矩形裁剪  

### 配置
`Android 7.0`以上，在`manifest`的`application`下(和`activity`同级)加入以下代码

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileProvider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/provider_paths" />
</provider>
```
并在`res`下新建`xml`目录，新建`provider_paths.xml`文件，内容如下：  
其中的`com.dubhe.headerselector`建议替换为你自己的项目名称。
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <files-path
        name="images"
        path="Android/data/com.dubhe.headerselector/files/Pictures/OriPicture/" />
    <external-path
        name="images"
        path="Android/data/com.dubhe.headerselector/files/Pictures/OriPicture/" />
    <external-files-path
        name="images"
        path="files/Pictures/OriPicture" />
</paths>
```

### 初始化
支持链式调用
```java
ImageSelector.getInstance(mActivity)//初始化图片选择器对象，参数是Activity或Fragment对象
              .setEnableClip(true)//是否裁剪图片
              .setClipMode(ClipImageActivity.TYPE_CIRCLE)//裁图模式 TYPE_CIRCLE圆形 TYPE_RECTANGLE矩形
              .setOnProcessFinishListener(new HeaderSelector.OnProcessFinishListener() {
                  //完成所有操作后返回最终结果的path
                  //TODO:不set将会导致无法拿到返回结果
                  @Override
                  public void onProcessFinish(String path) {
                       //TODO:拿到path进行逻辑操作
                  }
              });
```

### 接收回调
重写`onActivityResult`方法来接收回调数据，写在调用的`Fragment`和`Activity`中。
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    ImageSelector.getInstance(mActivity).onHeaderResult(requestCode, resultCode, data);
}
``` 

### 启动选择器
最终选择/裁剪后得到的图片`path`将从`onProcessFinish`方法中返回
```java
ImageSelector.getInstance(mActivity).showImageSelectMenu();//弹出图片选择器菜单
```

### 回收
在调用的`Activity`或`Fragment`的`onDestroy`方法中调用以下方法避免报空并回收资源
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    ImageSelector.getInstance(this).clear();
}
```

### 注意事项
裁剪器的圆形矩形仅为裁剪框的形状，仅便于用户定位，并不会把图片内容变成圆形。
**如果要显示圆形的图片请使用图片加载框架的相关功能**
例：Glide 4.9加载圆形图片
```java
 Glide.with(context).load(url)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(imageView);
```

### 添加自定义菜单
若业务需要在点击一个按钮时除了视频和图片以外显示其他的选项（例如文字和图片），可以直接在ImageSelector中添加。
```java
ImageSelector.getInstance(mActivity)//初始化图片选择器对象，参数是Activity或Fragment对象
              .setOnProcessFinishListener(new HeaderSelector.OnProcessFinishListener() {
                  //完成所有操作后返回最终结果的path
                  //TODO:不set将会导致无法拿到返回结果
                  @Override
                  public void onProcessFinish(String path) {
                       //TODO:拿到path进行逻辑操作
                  }
              });
              .addItem(ImgTextOptionModel(R.drawable.vec_video_file, "视频"))//第一个参数是图标，第二个参数是文本标签。会添加到底部
              .setOnCustomItemClickListener { text, position ->
                    //此处会返回文本标签和位置下标两个参数用于定位，建议使用文本标签进行判断
                    when (text) {
                        "视频" -> {
                            //TODO：进行选择视频的操作
                        }
                    }
                }
```
