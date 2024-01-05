idea 创建的 Console Application 项目


## 一、使用

### 执行命令：
在 [release](./release) 目录下有一份已经打包好的工具，下载 ApkCheck-1.0.0.tar 解压到当前目录，进入 bin 目录，执行如下命令：
> sh ApkCheck /user/xx/xxx.apk /user/xx/api.json

- .apk 后缀为自己需要分析的 apk 文件绝对路径
- api.json 为黑名单 api 文件，可下载 [PrivacyCheck/api.json](../api.json) 文件使用

## 二、项目说明

### 打包:
- 打包命令 ./gradlew assemble
- 产物输出：/build/distributions/xxx.tar
- 解压产物，执行 cli 命令:  sh bin/xx.sh /user/xx/xxx.apk /user/xx/api.json

### 执行步骤:
1. 解压 apk 文件，找到 .dex 结尾的文件
2. 执行 baksmail ，将 dex 文件转成 smail 文件
3. 解析 smail 文件，查找 api.json 的调用情况

### 产出文件
执行命令后会在当前目录 out/ApiCall.json，示例内容：
```json
[
 {
    "clazz": "TwilightManager",
    "method": "private getLastKnownLocationForProvider(Ljava/lang/String;)Landroid/location/Location;",
    "callClazz": "android.location.LocationManager",
    "callMethod": "getLastKnownLocation"
  }
]
```
- clazz:调用所在的类
- method:调用所在的方法
- callClazz:调用类
- callMethod:调用方法