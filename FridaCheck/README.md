## 接入方式

### 1、添加 maven 源：

```groovy
dependencyResolutionManagement {
  repositories {
      maven { url "https://raw.githubusercontent.com/MRwangqi/Maven/main"}
  }
}
```

### 2、添加依赖：

> implementation 'com.github.MRwangqi:fridacheck:1.0.0'

### 3、初始化：

```kotlin
FridaManager.init(this, true)
```

### 4、监听隐私 api 的调用情况

```kotlin
FridaManager.registerStackLog(object : com.codelang.fridacheck.StackLogListener {
    override fun onStackLog(stackLog: com.codelang.fridacheck.bean.StackLog) {
        // do something
    }
})
```

### 6、隐私 api 更新

该模块在 assets 目录默认内置了一份隐私合规 api 的 privacy_api.json 文件，如果未来隐私 api
文件需要更新，可以自己重新创建一份 privacy_api.json 文件，然后放到 app 模块的 assets
目录，项目打包时会自动替换依赖里内置的 privacy_api.json 文件。