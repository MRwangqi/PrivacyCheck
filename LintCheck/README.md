## 接入方式

添加 maven 源：
```json
dependencyResolutionManagement {
    repositories {
        maven { url "https://raw.githubusercontent.com/MRwangqi/Maven/main" }
        //...
     }
}
```

在 module 中添加依赖：
> implementation 'com.github.MRwangqi:privacy-lint:1.0.0'

执行 lint 检查：
> ./gradlew lint

报告产出：
> app/build/reports/lint-results.html