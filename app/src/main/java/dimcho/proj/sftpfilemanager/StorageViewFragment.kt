package dimcho.proj.sftpfilemanager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import dimcho.proj.sftpfilemanager.recycler.ListItemClickListener
import dimcho.proj.sftpfilemanager.recycler.StorageEntryAdapter
import dimcho.proj.sftpfilemanager.recycler.StorageInfo
import kotlinx.android.synthetic.main.storage_view_fragment.*

import kotlinx.android.synthetic.main.storage_view_fragment.view.*
import java.io.File
import java.lang.Exception
import java.util.ArrayList

/**
 * Created by dimcho on 04.03.18.
 */
const val STORAGE_VIEW_FRAGMENT_TAG = "StorageView"
private const val STORAGE_PERMISSION_REQUEST_CODE = 1
private const val DIALOG_TAG = "SshDialog"

private const val EXTERNAL_STORAGE_LIST_INDEX = 1

class StorageViewFragment: Fragment(), PassInputDialogFragment.OnDialogResultListener,
        ListItemClickListener {

    private val storageList: MutableList<StorageInfo> = ArrayList()
    private lateinit var toast: Toast
    private lateinit var rootStoragePath: String
    private var sdCardPath: String = ""
    private var permissionsGranted: Boolean = false

    private fun askStoragePermission() {
        if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_REQUEST_CODE)
        } else {
            // Permission has already been granted
//            openFilesViewFragment("Local", null)
            permissionsGranted = true
        }
    }

    private fun openFilesViewFragment(targetFg: String, password: String? = null) {
        if(!permissionsGranted && password == null) {
            toast.run {
                setText("Restart the app and grant the storage permission to access storage!")
                duration = Toast.LENGTH_SHORT
                show()
            }

            return
        }

        val bundle = Bundle()
        bundle.putStringArray("Test", arrayOf(targetFg, password, rootStoragePath))

        val filesViewFragment = FilesViewFragment()
        filesViewFragment.arguments = bundle

        fragmentManager.beginTransaction()
                .addToBackStack(STORAGE_VIEW_FRAGMENT_TAG)
                .replace(R.id.fgContainer, filesViewFragment, FILES_VIEW_FRAGMENT_TAG)
//                .hide(this)
//                .add(R.id.fgContainer,filesViewFragment , FILES_VIEW_FRAGMENT_TAG)
                .commit()
    }

    private fun searchForSdCard(onSdFound: (sdCardPath: String) -> Unit, onSdNotFound: () -> Unit = {}) {
        val internalStorageDir: File = Environment.getExternalStorageDirectory()
        val internalStorageDirPath: String = internalStorageDir.absolutePath
        val subDirectoryCnt: Int = internalStorageDirPath.split(File.separatorChar).size - 1

        // On some android devices the path to sdcard is storage/sdcard
        // On newer devices its /storage/emulated/0
        // Newer devices /mnt/media_rw
        val storageDir: File = when(subDirectoryCnt) {
            3 -> internalStorageDir.parentFile.parentFile
            2 -> internalStorageDir.parentFile

            else -> throw Exception("Wrong number of sub directories: $subDirectoryCnt")
        }

        val filteredStorageDirs = storageDir.listFiles{ _, name ->
            name != "self" && name != "emulated" && name != "usbotg"
                    && !name.startsWith("sdcard", true)
        }

        if(filteredStorageDirs.size > 1) {
            // TODO clear log
            for (file in filteredStorageDirs) {
                Log.e("Test", file.name)
            }
            throw Exception("Can not find SD card path! Too many directories!")
        }


        if(!filteredStorageDirs.isEmpty()) {
            onSdFound(filteredStorageDirs[0].absolutePath)
        } else {
            // Attempt to find sd card for older devices which have only sdcard1 folder
            val filteredSdCards = storageDir.listFiles(){ _, name ->
                name.matches(Regex("sdcard[1-9]+"))
            }

            if(filteredSdCards.size == 1) {
                onSdFound(filteredSdCards[0].absolutePath)
            } else {
                onSdNotFound()
            }
        }
    }

    private fun addSdCardToStorageList(sdPath: String) {
        if(sdCardPath.isEmpty()) {
            val externalStorageInfo =
                    StorageInfo(R.mipmap.ic_sd_storage_black_36dp, "SD card")

            // Inserts the external storage card as the second element of the list view
            // The ArrayList shifts forward the previous element at position 1
            storageList.add(EXTERNAL_STORAGE_LIST_INDEX, externalStorageInfo)
            sdCardPath = sdPath

            Log.wtf("Test", sdCardPath)
        }
    }

    override fun onListItemClick(clickedItemIndex: Int) {
        val storageInfo = storageList[clickedItemIndex]

        val onAnimationDelayedAction = Runnable {
            when(storageInfo.storageItemText) {
                "Internal Storage" -> {
                    rootStoragePath = Environment.getExternalStorageDirectory().absolutePath
                    openFilesViewFragment("Local")
                }

                "SD card" -> {
                    rootStoragePath = sdCardPath
                    openFilesViewFragment("Local")
                }

                else -> {
//                    PassInputDialogFragment.create()
                    // TODO use shared prefs
                    rootStoragePath = "/export/content/downloads"

                    val passwordPromptDialog = PassInputDialogFragment()
//                    passwordPromptDialog.message = "Enter password for none@192.168.1.240"
//                    passwordPromptDialog.customViewResId = R.layout.layout_password_dialog
//                    passwordPromptDialog.setOnDialogResultListener(this)
                    passwordPromptDialog.show(fragmentManager, DIALOG_TAG)
                }
            }
        }

        // Adds animation delay so the click ripple effect is visible
        ViewCompat.postOnAnimationDelayed(lvStorage, onAnimationDelayedAction,140)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        askStoragePermission()
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Add predefined elements for the internal storage
        val internalStorageInfo =
                StorageInfo(R.mipmap.ic_storage_black_36dp, "Internal Storage")
        storageList.add(internalStorageInfo)

        // Inserts a StorageInfo object for the SFTP server
        // Temporary
        val sftpStorageInfo =
                StorageInfo(R.mipmap.ic_cloud_black_36dp, "Sftp server")

        storageList.add(sftpStorageInfo)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val fgView = inflater!!.inflate(R.layout.storage_view_fragment,
                container, false)

        val lvStorage = fgView.lvStorage
        lvStorage.adapter = StorageEntryAdapter(storageList, this)

        return fgView
    }

    override fun onResume() {
        // TODO on sd mounted/unmounted
        super.onResume()

        searchForSdCard(::addSdCardToStorageList){
            if(!sdCardPath.isEmpty()) {
                storageList.removeAt(EXTERNAL_STORAGE_LIST_INDEX)
                sdCardPath = ""
            }
        }

        (lvStorage.adapter as StorageEntryAdapter).notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        view?.lvStorage?.onItemClickListener = null
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {

        when (requestCode) {
            STORAGE_PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                    openFilesViewFragment("Local")
                    permissionsGranted = true
                } else {
                    toast = Toast.makeText(context,
                            "Permission denied, functionality disabled!", Toast.LENGTH_LONG)
                    toast.show()

                }
            }
        }
    }

    override fun onReceiveResult(resultCode: Int, result: String) {
        when(resultCode) {
            RESULT_DIALOG_OK -> {} // TODO unused

            RESULT_DIALOG_CANCEL, RESULT_PASSWORD_CANCEL -> Toast.makeText(context,
                    "Connection canceled!", Toast.LENGTH_LONG).show()

            RESULT_PASSWORD_SET -> {
                openFilesViewFragment("Remote", result)
            }
        }
    }
}