# GesturePasswordView
一个手势密码的轻量级实现，简单，可靠。

</br></br>
实现效果
-----

![](https://github.com/Thuantanon/PasswordView/blob/master/simple/simple.gif)

</br></br>
Gradle方式导入
-----

>1.在工程的build.gradle文件中加入：

```Java
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

>2.在module的build.gradle中加入：

```Java
dependencies {
	compile 'com.github.Thuantanon:GesturePasswordView:1.02'
}
```

</br></br>
使用方式：
-----

>1.在xml中使用：

```Java
<com.cxh.passwordview.PasswordView
        android:id="@+id/password_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        />
```

>>用自定义属性：
```Java
<!-- 默认内圆颜色 -->
<attr name="gesture_default_color" format="color"/>
<!-- 选中时外圆颜色 -->
<attr name="gesture_selected_circle_outer_color" format="color"/>
<!-- 选中时内圆颜色 -->
<attr name="gesture_selected_circle_inner_color" format="color"/>
<!-- 错误时外圆颜色 -->
<attr name="gesture_error_circle_outer_color" format="color"/>
<!-- 错误时内圆颜色 -->
<attr name="gesture_error_circle_inner_color" format="color"/>
<!-- 内圆半径 -->
<attr name="gesture_circle_inner_radius" format="dimension"/>
<!-- 外圆半径 -->
<attr name="gesture_circle_outer_radius" format="dimension"/>
<!-- 连接线粗细 -->
<attr name="gesture_line_stroke" format="dimension"/>
```


>2.在Activity中使用：

```Java
mPasswordView = findViewById(R.id.password_view);
   mPasswordView.setListener(new PasswordView.OnFinishedListener() {
      @Override
      public void selected(char ch) {
         // 每个字符选中回调
      }
      @Override
      public void onResult(String password) {
         if(password.length() < 4){
            mPasswordView.setPasswordError(true);
         }
       }
  });
```
</br></br>
