import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jf.baksmali.Main
import java.io.File
import java.security.InvalidParameterException
import java.lang.reflect.Type

// 打包命令 ./gradlew assemble
// 产物输出：/build/distributions/xxx.tar
// 产物 cli 执行: sh bin/xx.sh xxx.apk api.json
fun main(args: Array<String>) {

    args.forEach(::println)

    if (args.isEmpty()) {
        throw InvalidParameterException("未配置 apk 路径")
    }

    val apkPath = args[0]
    if (!apkPath.endsWith(".apk")) {
        throw InvalidParameterException("配置为非 apk 文件")
    }

    val apkFile = File(apkPath)
    if (!apkFile.exists()) {
        throw InvalidParameterException("apk 文件不存在")
    }

    val configFile = File(args[1])
    if (!configFile.exists()) {
        throw InvalidParameterException("配置文件不存在")
    }

    val type: Type = object : TypeToken<List<ApiNode>>() {}.type
    val apiCallList: List<ApiNode> = Gson().fromJson(configFile.bufferedReader(), type)

    var startTime = System.currentTimeMillis()
    // 1、 解压 apk 文件到目录
    val apkDir = File("out/apk")
    apkDir.mkdirs()
    println("start unzip apk ....")
    var costTime = System.currentTimeMillis() - startTime
    print("unzip cost ${costTime}ms")
    UnzipUtils.unZipFiles(apkFile, apkDir.absolutePath)
    // 2、 获取所有 .dex 后缀的文件
    val dexList = apkDir.listFiles()?.filter { it.absolutePath.endsWith(".dex") }
    println("end unzip ....")
    println("dex list:")
    dexList?.map { it.absolutePath }?.forEach(::println)
    // 3、 执行 baksmali 生成 smail 文件
    val smailDir = File("out/smail")
    smailDir.mkdirs()
    println("start baksmail...")
    // todo 多线程优化
    dexList?.forEach {
        // java -jar baksmali-2.5.2.jar d -o dir classes.dex
        Main.main(arrayOf("d", "-o", smailDir.absolutePath, it.absolutePath))
    }
    println("end baksmali...")
    costTime = System.currentTimeMillis() - startTime
    println("baksmali cost ${costTime}ms")
    startTime = System.currentTimeMillis()
    // 4、读取 smail 文件
    val resultList = arrayListOf<ApiCallResult>()
    val files = arrayListOf<File>()
    dfsFile(smailDir, files)
    files.forEach { checkApiCall(it,apiCallList, resultList) }
    costTime = System.currentTimeMillis() - startTime
    println("smali cost ${costTime}ms")

    // result output
    val apiFile = File("out", "ApiCall.json")
    if (apiFile.isFile && apiFile.exists()) {
        apiFile.delete()
    }
    apiFile.createNewFile()
    val json = Gson().toJson(resultList)
    apiFile.writeBytes(json.toByteArray())
}

private fun dfsFile(file: File, list: ArrayList<File>) {
    if (file.isDirectory) {
        file.listFiles()?.forEach { dfsFile(it, list) }
    } else {
        list.add(file)
    }
}

private fun checkApiCall(file: File,apiCallList:List<ApiNode> ,resultList: ArrayList<ApiCallResult>) {
    if (!file.absolutePath.endsWith(".smali")) {
        return
    }
    if (file.absolutePath.endsWith("BuildConfig.smali") ||
        file.absolutePath.endsWith("R.smali")
    ) {
        return
    }

    val path = file.absolutePath
    val clazzName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."))
    var method = ""
    val calls = INVOKE.getCalls()
    file.readLines().forEach { l ->
        val line = l.trim()
        // 记录 method
        if (line.startsWith(".method")) {
            method = line.substring(".method".length, line.length).trim()
        }
        calls.forEach { call ->
            if (line.startsWith(call)) {
                // 解析 insn call
                val callClazz = getClassName(line.split("->")[0].trim())
                val callMethod = getMethodName(line.split("->")[1].trim())
                // 判断调用方法是否在 apiCall 中
                val result = apiCallList.find { it.clazz == callClazz }?.method?.find { it == callMethod }
                if (!result.isNullOrEmpty()){
                    println("clazz=$clazzName method=$method call=$callClazz.$callMethod")
                    resultList.add(ApiCallResult(clazzName, method, callClazz, callMethod))
                }
            }
        }
    }
}

private fun getClassName(str: String): String {
    var clazzName = str.substring(str.lastIndexOf(",") + 1, str.length).replace("/", ".").trim()
    // 去描述符
    // [java/util/ArrayList;  数组对象，也有可能是 [[java/util/ArrayList;
    if (clazzName.startsWith("[")) {
        clazzName = clazzName.substring(clazzName.lastIndexOf("[") + 1, clazzName.length)
    }
    // Ljava/util/ArrayList;  对象
    if (clazzName.startsWith("L")) {
        return clazzName.substring(1, clazzName.length - 1)
    }
    return clazzName
}

private fun getMethodName(str: String): String {
    return str.substring(0, str.indexOf("("))
}

enum class INVOKE(val call: String) {
    // invoke-static {p3}, Lcom/xxx/A;->getFlags(I)I
    INVOKE_STATIC("invoke-static"),

    // invoke-virtual {v0}, [Lcom/xx/A;->clone()Ljava/lang/Object;
    INVOKE_VIRTUAL("invoke-virtual"),

    // invoke-direct {v0, v1, v2, v3}, Lcom/xxl/A;-><init>(Ljava/lang/String;ILjava/lang/String;)V
    INVOKE_DIRECT("invoke-direct"),
    INVOKE_SUPER("invoke-super"),
    INVOKE_INTERFACE("invoke-interface");

    companion object {
        fun getCalls(): List<String> {
            return values().map { it.call }.toList()
        }
    }
}