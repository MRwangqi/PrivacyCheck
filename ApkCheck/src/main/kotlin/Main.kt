import org.jf.baksmali.Main
import java.io.File
import java.security.InvalidParameterException

// 打包命令 ./gradlew assemble
// 产物输出：/build/distributions/xxx.tar
// 产物 cli 执行: sh bin/xx.sh
fun main(args: Array<String>) {

//    if (args.isNullOrEmpty()) {
//        throw InvalidParameterException("未配置 apk 路径")
//    }
//
//    val apkPath = args[0]
//    if (!apkPath.endsWith(".apk")) {
//        throw InvalidParameterException("配置为非 apk 文件")
//    }

//    todo
    val apkPath = "/Volumes/codelang/tuya/appshell/bugfix/TuyaSmart_AppShell/build/outputs/apk/international/debug/ThingSmartAppShell-international-debug.apk"

    val apkFile = File(apkPath)
    if (!apkFile.exists()) {
        throw InvalidParameterException("apk 文件不存在")
    }

    val startTime = System.currentTimeMillis()
    // 1、 解压 apk 文件到目录
    val apkDir = File("out/apk")
    apkDir.mkdirs()
    println("start unzip apk ....")
    UnzipUtils.unZipFiles(apkFile,apkDir.absolutePath)
    // 2、 获取所有 .dex 后缀的文件
    val dexList = apkDir.listFiles()?.filter { it.absolutePath.endsWith(".dex") }
    println("end unzip ....")
    println("dex list:")
    dexList?.map { it.absolutePath }?.forEach(::println)
    // 3、 执行 baksmali 生成 smail 文件
    val smailDir = File("out/smail")
    smailDir.mkdirs()
    println("start baksmail:")
    // todo 多线程优化
    dexList?.forEach {
        // java -jar baksmali-2.5.2.jar d -o dir classes.dex
        Main.main(arrayOf("d","-o",smailDir.absolutePath, it.absolutePath))
    }
    println("end baksmail:")
    // todo 4、读取 smail 文件
    val outDir = smailDir.parentFile

    val costTime = System.currentTimeMillis() - startTime
    print("cost ${costTime}ms")


}