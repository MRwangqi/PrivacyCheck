## 配置

```
plugins {
    id 'com.android.application'
    // 配置插件
    id 'depCheck'
}

// 配置文件路径
depConfig {
    path = rootDir.absolutePath + File.separator + "api.json"
}
```

执行命令：
> ./gradlew depCheck -Pbuild=debug


生成文件：
> app/build/ApiCall.json