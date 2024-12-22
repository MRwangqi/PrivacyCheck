import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

object UnzipUtils {
    /**
     * 解压到指定目录
     * @param zipPath
     * @param descDir
     */
    @Throws(IOException::class)
    fun unZipFiles(zipPath: String, descDir: String) {
        unZipFiles(File(zipPath), descDir)
    }

    /**
     * 解压文件到指定目录
     * @param zipFile
     * @param descDir
     */
    @Throws(IOException::class)
    fun unZipFiles(zipFile: File, descDir: String) {
        val pathFile = File(descDir)
        if (!pathFile.exists()) {
            pathFile.mkdirs()
        }
        val zip = ZipFile(zipFile)
        val entries: Enumeration<*> = zip.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement() as ZipEntry
            val zipEntryName = entry.name
            val `in` = zip.getInputStream(entry)
            val outPath = (descDir +File.separator+ zipEntryName).replace("\\*".toRegex(), "/")
            //判断路径是否存在,不存在则创建文件路径
            val file = File(outPath).parentFile
            if (!file.exists()) {
                file.mkdirs()
            }
            //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
            if (File(outPath).isDirectory) {
                continue
            }
            //输出文件路径信息
            val out: OutputStream = FileOutputStream(outPath)
            val buf1 = ByteArray(1024)
            var len: Int
            while (`in`.read(buf1).also { len = it } > 0) {
                out.write(buf1, 0, len)
            }
            `in`.close()
            out.close()
        }
    }
}