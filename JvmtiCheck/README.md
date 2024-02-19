## 接入方式

只适用 debuggable 的应用

### 1、添加 maven 源：

```groovy
dependencyResolutionManagement {
  repositories {
     maven { url "https://raw.githubusercontent.com/MRwangqi/Maven/main"}
  }
}
```

### 2、添加依赖：

> implementation 'com.github.MRwangqi:jvmticheck:1.0.0'

### 3、初始化：

```kotlin
JvmtiHelper.init(application)
```


### 4、监听隐私 api 的调用情况
目前只实现了 logcat 输出隐私 api 的堆栈，效果如下：
```
I  ========== find MethodEntry 线程名main class=androidx/core/app/ActivityCompat 方法名=requestPermissions(Landroid/app/Activity;[Ljava/lang/String;I)V =======
W  java.lang.Throwable: privacy api
W  	at androidx.core.app.ActivityCompat.requestPermissions(ActivityCompat.java:518)
W  	at com.codelang.test.ApiCallDemo.method(ApiCallDemo.kt:13)
W  	at com.codelang.privacycheck.MainActivity.onCreate$lambda$1(MainActivity.kt:22)
W  	at com.codelang.privacycheck.MainActivity.$r8$lambda$lF9vVhekfpOGNSSUWZEaHZtqAXs(Unknown Source:0)
W  	at com.codelang.privacycheck.MainActivity$$ExternalSyntheticLambda0.onClick(Unknown Source:2)
W  	at android.view.View.performClick(View.java:7750)
W  	at com.google.android.material.button.MaterialButton.performClick(MaterialButton.java:1218)
W  	at android.view.View.performClickInternal(View.java:7727)
W  	at android.view.View.access$3700(View.java:861)
W  	at android.view.View$PerformClick.run(View.java:29143)
W  	at android.os.Handler.handleCallback(Handler.java:938)
W  	at android.os.Handler.dispatchMessage(Handler.java:99)
W  	at android.os.Looper.loopOnce(Looper.java:210)
W  	at android.os.Looper.loop(Looper.java:299)
W  	at android.app.ActivityThread.main(ActivityThread.java:8292)
W  	at java.lang.reflect.Method.invoke(Native Method)
W  	at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:556)
W  	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1045)
```
后面会考虑使用 mmap 输出到文件(待做)

### 5、添加混淆规则：

> -keep class com.codelang.jvmticheck.bean.**{*;}

### 6、隐私 api 更新

该模块在 assets 目录默认内置了一份隐私合规 api 的 privacy_api.json 文件，如果未来隐私 api
文件需要更新，可以自己重新创建一份 privacy_api.json 文件，然后放到 app 模块的 assets
目录，项目打包时会自动替换依赖里内置的 privacy_api.json 文件。