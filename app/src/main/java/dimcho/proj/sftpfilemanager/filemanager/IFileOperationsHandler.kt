package dimcho.proj.sftpfilemanager.filemanager

import java.io.Serializable

/**
 * Created by dimcho on 16.03.18.
 */
interface OnFileManagerResultListener: Serializable {
    fun onFilesListed()
}