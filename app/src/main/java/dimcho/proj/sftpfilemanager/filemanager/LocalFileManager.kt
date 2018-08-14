package dimcho.proj.sftpfilemanager.filemanager

import dimcho.proj.sftpfilemanager.recycler.FileInfo
import dimcho.proj.sftpfilemanager.recycler.AlphaNumericComparator
import dimcho.proj.sftpfilemanager.recycler.getFileInfoFromFile
import java.io.File
import java.io.Serializable
import java.util.*

/**
 * Created by dimcho on 11.03.18.
 */

class LocalFileManager(override val rootDirectoryPath: String): FileManager, Serializable {

    private val files: ArrayList<FileInfo> = ArrayList(30)

    private lateinit var fileManagerResultListener: OnFileManagerResultListener

//    override val rootDirectoryPath: String
//        get() = Environment.getExternalStorageDirectory().absolutePath

    override val filesCache: LinkedHashMap<String, List<FileInfo>> = linkedMapOf()

    override var shouldReInitiateDirListing: Boolean = false

    override fun prepare(onPrepared: (fm: FileManager) -> Unit) {
        onPrepared(this)
    }

    override fun setOnFileManagerResultListener(listener: OnFileManagerResultListener) {
        fileManagerResultListener = listener
    }

    override fun listDirectory(dirPath: String) {
        files.clear()

        val containedFiles = File(dirPath).listFiles()
        for(file in containedFiles) {
            files.add(getFileInfoFromFile(file))
        }

        Collections.sort(files, AlphaNumericComparator())

        // Insert the listed directory in the cache
        if(!files.isEmpty()) {
            filesCache[dirPath] = ArrayList(files)
        }

        fileManagerResultListener.onFilesListed()
    }

    override fun getCurrentlyListedFiles(): ArrayList<FileInfo> {
        return files
    }

    override fun getParentDirectoryPath(dir: String): String {
        return File(dir).parentFile.absolutePath
    }

    override fun clearPendingTaskResultsFromActivity(resultID: Int) { }
    override fun findIdOfTaskToCancel(): String? { return null }
    override fun cancelTaskWithID(taskID: String) { }
}