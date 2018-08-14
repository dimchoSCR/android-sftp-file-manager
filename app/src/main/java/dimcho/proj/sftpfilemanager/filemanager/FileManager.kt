package dimcho.proj.sftpfilemanager.filemanager

import dimcho.proj.sftpfilemanager.recycler.FileInfo
import java.io.Serializable
import java.util.ArrayList
import java.util.LinkedHashMap

/**
 * Created by dimcho on 11.03.18.
 */
interface FileManager: Serializable {
    val rootDirectoryPath: String
    val filesCache: LinkedHashMap<String, List<FileInfo>>
    var shouldReInitiateDirListing: Boolean

    fun prepare(onPrepared: (fm: FileManager) -> Unit)
    fun setOnFileManagerResultListener(listener: OnFileManagerResultListener)
    fun listDirectory(dirPath: String)
    fun getParentDirectoryPath(dir: String): String
    fun getCurrentlyListedFiles(): ArrayList<FileInfo>

    fun useCachedFolder(dirPath: String): Boolean {
        val cachedDir: List<FileInfo> = filesCache[dirPath] ?: return false

        val files = getCurrentlyListedFiles()
        files.clear()
        files.addAll(cachedDir)

        return true
    }

    fun clearChildrenFromCache(parentPath: String) {

        val parentDirList = filesCache[parentPath] ?:
                        throw NoSuchElementException("Parent directory can not be found in cache!")

        // Clear the cache
        filesCache.clear()

        // Caches the parentList by copying it
        // This way only the child files are cleared
        // and listing the contents of the root directory is avoided
        filesCache[rootDirectoryPath] = parentDirList
    }

    fun clearPendingTaskResultsFromActivity(resultID: Int)
    fun findIdOfTaskToCancel(): String?
    fun cancelTaskWithID(taskID: String)


    fun exit() {
        filesCache.clear()
    }
}