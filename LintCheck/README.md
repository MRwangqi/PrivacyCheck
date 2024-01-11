## 接入方式

### 1、添加 maven 源：
```groovy
dependencyResolutionManagement {
    repositories {
        maven { url "https://raw.githubusercontent.com/MRwangqi/Maven/main" }
        //...
     }
}
```

### 2、在 module 中添加依赖：
> implementation 'com.github.MRwangqi:privacy-lint:1.0.0'

### 3、添加  privacy_api.json
将 privacy_api.json 放置到项目根目录，例如 [PrivacyCheck/privacy_api.json](../privacy_api.json)

### 4、执行 lint 检查：
> ./gradlew lint

### 5、报告产出：
> app/build/reports/lint-results.html