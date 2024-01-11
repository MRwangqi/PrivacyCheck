idea 创建的 Console Application 项目


## 一、使用

### 执行命令：
在 [release](./release) 目录下有一份已经打包好的工具，下载 ApkCheck-1.0.0.tar 解压到当前目录，进入 bin 目录，执行如下命令：
> ./ApkCheck /user/xx/xxx.apk /user/xx/privacy_api.json

- .apk 后缀为自己需要分析的 apk 文件绝对路径
- privacy_api.json 为隐私合规配置文件，可下载 [PrivacyCheck/privacy_api.json](../privacy_api.json) 文件使用

## 二、项目说明

### 打包:
- 打包命令 ./gradlew assemble
- 产物输出：/build/distributions/xxx.tar
- 解压产物，执行 cli 命令:  sh bin/xx.sh /user/xx/xxx.apk /user/xx/privacy_api.json

### 应用执行步骤:
1. 解压 apk 文件，找到 .dex 结尾的文件
2. 执行 baksmali ，将 dex 文件转成 smali 文件
3. 解析 smali 文件，检查 privacy_api.json 的调用情况

### 产出文件
执行命令后会在当前目录生成 out/ApiCall.json 文件，示例内容：
```json
{
  "android.location.Location_getLatitude": [
    {
      "clazz": "Landroidx.appcompat.app.TwilightManager;",
      "method": "private updateState(Landroid/location/Location;)V"
    }
  ]
}
```
该 json 文件描述的是，在 TwilightManager 类的 updateState 方法有调用 Location.getLatitude 这个隐私 Api