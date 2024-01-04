idea 创建的 Console Application 项目

### 打包:
- 打包命令 ./gradlew assemble
- 产物输出：/build/distributions/xxx.tar
- 解压产物，执行 cli 命令:  sh bin/xx.sh /user/xx/xxx.apk /user/xx/api.json

### 执行步骤:
1. 解压 apk 文件，找到 .dex 结尾的文件
2. 执行 baksmail ，将 dex 文件转成 smail 文件
3. 解析 smail 文件，查找 api.json 的调用情况

### 产出文件
当前目录 out/ApiCall.json，示例内容：
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