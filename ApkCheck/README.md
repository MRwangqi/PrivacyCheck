idea 创建的 Console Application 项目

### 打包:
- 打包命令 ./gradlew assemble
- 产物输出：/build/distributions/xxx.tar
- 解压产物，执行 cli 命令: sh bin/xx.sh

### 执行步骤:
1. 解压 apk 文件，找到 .dex 结尾的文件
2. 执行 baksmail ，将 dex 文件转成 smail 文件
3. 解析 smail 文件，查找 api.json 的调用情况