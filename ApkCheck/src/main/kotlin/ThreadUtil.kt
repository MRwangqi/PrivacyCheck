import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

object ThreadUtil {

    /**
     * 多线程分段处理集合
     */
    @JvmStatic
    fun <T> batchProcessList(list: List<T>, subCallback: (List<T>) -> Unit) {
        val length: Int = list.size
        var cpuNum = Runtime.getRuntime().availableProcessors() * 2
        cpuNum = if (length > cpuNum) {
            cpuNum
        } else {
            1
        }

        val tl = length / cpuNum
        val size = if (length % cpuNum == 0){
            cpuNum
        }else {cpuNum + 1}

        val countDownLatch = CountDownLatch(size)

        for (i in 0 until size) {
            val start = i * tl
            var end = (i + 1) * tl
            if (end > length) {
                end = length
            }
            thread {
                val subList: List<T> = list.subList(start, end)
                subCallback.invoke(subList)
                countDownLatch.countDown()
            }
        }
        countDownLatch.await()
    }

}