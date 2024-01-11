## 项目配置
配置 maven 源与依赖
```groovy
buildscript {
    repositories {
        maven { url "https://raw.githubusercontent.com/MRwangqi/Maven/main" }
    }
    dependencies {
        classpath "com.github.MRwangqi:depCheckPlugin:1.0.0"
    }
}
```
apply 插件：
```groovy
plugins {
    id 'com.android.application'
    // 配置插件
    id 'depCheck'
}

// 配置隐私合规文件的绝对路径
depConfig {
    path = project.rootDir.absolutePath + File.separator + "privacy_api.json"
}
```

执行命令：
> ./gradlew depCheck -Pbuild=debug // -Pbuild=${build variant}


产物生成路径：
> app/build/ApiCall.json

产物说明:
```json
{
  "android.location.Location_getLatitude": [
    {
      "clazz": "androidx/appcompat/app/TwilightManager",
      "method": "updateState",
      "dep": "androidx.appcompat:appcompat:1.6.1"
    }
  ]
}
```
该 json 文件描述的是，在 appcompat 依赖中， TwilightManager 类的 updateState 方法有调用 Location.getLatitude 这个隐私 Api
