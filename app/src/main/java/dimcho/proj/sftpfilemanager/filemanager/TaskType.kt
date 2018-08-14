package dimcho.proj.sftpfilemanager.filemanager

import java.text.SimpleDateFormat
import java.util.*

enum class TaskType(val value: String) {
    LIST("List"), COPY("Copy");

    fun constructTaskId(taskWhat: String): String {
        val now = Date()
        val dateFormatter =
                SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())

        return "$value:$taskWhat:${dateFormatter.format(now)}"
    }
}