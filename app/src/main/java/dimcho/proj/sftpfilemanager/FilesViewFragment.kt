package dimcho.proj.sftpfilemanager

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import com.jcraft.jsch.UserInfo
import dimcho.proj.sftpfilemanager.filemanager.*
import dimcho.proj.sftpfilemanager.recycler.FileInfo
import dimcho.proj.sftpfilemanager.recycler.FilesAdapter
import dimcho.proj.sftpfilemanager.recycler.ListItemClickListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.files_view_fragment.*
import kotlinx.android.synthetic.main.files_view_fragment.view.*
import java.util.*

/**
 * Created by dimcho on 04.03.18.
 */

const val FILES_VIEW_FRAGMENT_TAG = "FilesView"
private val stateStack: Stack<Pair<Int, Int>> = Stack()
private lateinit var fileManager: FileManager

class FilesViewFragment: Fragment(), ListItemClickListener,
        OnFileManagerResultListener {

    private lateinit var animation: LayoutAnimationController
    private lateinit var rootStoragePath: String
    private lateinit var currentDirPath: String
    private var prevScrollOffset: Int = 0
    private var prevItemPos: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO remove UserInfo implementation from fragment
        if(savedInstanceState == null) {
            val fmType = arguments.getStringArray("Test")
            fileManager = when (fmType[0]) {
                "Local" -> LocalFileManager(fmType[2])

                else -> {
                    val userInfo = object : UserInfo {
                        override fun promptPassphrase(message: String?): Boolean {
                            return false
                        }

                        override fun getPassphrase(): String? {
                            return null
                        }

                        override fun getPassword(): String {
                            return fmType[1]
                        }

                        override fun promptYesNo(message: String?): Boolean {
                            return true
                        }

                        override fun showMessage(message: String?) {}

                        override fun promptPassword(message: String?): Boolean {
                            return true
                        }
                    }

                    RemoteFileManager(userInfo)
                }
            }
        }

        rootStoragePath = fileManager.rootDirectoryPath
    }

    private fun setUpRecyclerView(rootView: View) {
        val recycler = rootView.recyclerView
        // Improves performance
        recycler.setHasFixedSize(true)

        // Sets the layout manager
        val layoutManager = LinearLayoutManager(context)
        recycler.layoutManager = layoutManager

        // Sets an item decoration
        val dividerItemDecoration = DividerItemDecoration(context,
                layoutManager.orientation)
        recycler.addItemDecoration(dividerItemDecoration)

        recycler.adapter = FilesAdapter(fileManager.getCurrentlyListedFiles(), this)

        // Sets up the recycler view's layout animation
        animation = AnimationUtils.loadLayoutAnimation(context, R.anim.rv_animation_fall)
        recycler.layoutAnimation = animation
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {

        Log.wtf("Test", "Fragment OnCreateView")
        val filesView = inflater!!.inflate(R.layout.files_view_fragment,
                container, false)

        // Hide the activity fab
        activity.fab.isEnabled = false
        activity.fab.hide()
        activity.fab.visibility = View.INVISIBLE

        if(savedInstanceState != null) {
            val progressState: Int = savedInstanceState.getInt("ProgressState")
            filesView.ltSwipeToRefresh.isRefreshing =
                    savedInstanceState.getBoolean("PullToRefresh")

            // If the cache is not empty and there are no files in the file manager
            // then the directory is empty
            if(fileManager.getCurrentlyListedFiles().isEmpty() && progressState != View.VISIBLE) {
                filesView.tvEmpty.visibility = View.VISIBLE
            }

            // If cache is still empty and cache has not been cleared then the files are not loaded yet
            // So show progress indicator
            if(progressState == View.VISIBLE) {
                filesView.progressBar.visibility = View.VISIBLE
            }

            currentDirPath = savedInstanceState.getString("CurrentDirectory")
            prevItemPos = savedInstanceState.getInt("PrevItemPos")
            prevScrollOffset = savedInstanceState.getInt("PrevScrollOffset")
        } else {
            currentDirPath = rootStoragePath
        }

        setUpRecyclerView(filesView)

        // Set up swipe to refresh
        val ltRefresh = filesView.ltSwipeToRefresh
        ltRefresh.setColorSchemeResources(R.color.colorAccent)
        ltRefresh.setOnRefreshListener {
            prevItemPos = 0
            prevScrollOffset = 0

            fileManager.listDirectory(currentDirPath)
        }

        return filesView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fileManager.setOnFileManagerResultListener(this)

        // Initializes the fm and lists all files when fm is ready and
        if(savedInstanceState == null) {
            ltSwipeToRefresh.isEnabled = false
            progressBar.visibility = View.VISIBLE
            fileManager.prepare { fm -> fm.listDirectory(currentDirPath); }
        }
    }

    override fun onFilesListed() {
        ltSwipeToRefresh.isEnabled = true
        ltSwipeToRefresh.isRefreshing = false
//        recyclerView.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        recyclerView.startLayoutAnimation()
        recyclerView.adapter.notifyDataSetChanged()

        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
        linearLayoutManager.scrollToPositionWithOffset(prevItemPos, prevScrollOffset)

        if(fileManager.getCurrentlyListedFiles().isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
        }
    }

    override fun onListItemClick(clickedItemIndex: Int) {
        val clickedFileInfo: FileInfo = fileManager.getCurrentlyListedFiles()[clickedItemIndex]

        if (clickedFileInfo.isDirectory){
            progressBar.visibility = View.VISIBLE

            // Acquire current list item's position and offset
            // Store the data in a stack
            // Then list the new directory
            val childAtTopOfList:View = recyclerView.getChildAt(0)
            prevItemPos = recyclerView.getChildAdapterPosition(childAtTopOfList)
            prevScrollOffset = childAtTopOfList.top
            stateStack.add(Pair(prevItemPos, prevScrollOffset))

            // Reset position variables
            prevItemPos = 0
            prevScrollOffset = 0

            val prevDirPath = currentDirPath
            currentDirPath = clickedFileInfo.absolutePath

            // TODO add cache limit
            // If there are cache entries load and the requested item is in the cache display it
            if(fileManager.useCachedFolder(currentDirPath)) {
                Log.wtf("Test", "From cache")
                onFilesListed()
            } else {
                Log.wtf("Test", "No cache")

                // Clear accumulated cache when the user navigates
                // to a directory for the first time from the storage root
                if(fileManager.filesCache.size > 1 && prevDirPath == rootStoragePath) {
                    // Clears only the children of the root Directory
                    fileManager.clearChildrenFromCache(rootStoragePath)
                    Log.wtf("Test", "Clearing cache")
                }

                // Do not invoke if listing the root Directory
                fileManager.listDirectory(clickedFileInfo.absolutePath)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        Log.wtf("Test", "saving state")
        outState.putString("CurrentDirectory", currentDirPath)
        outState.putInt("PrevItemPos", prevItemPos)
        outState.putInt("PrevScrollOffset", prevScrollOffset)
        outState.putInt("ProgressState", progressBar.visibility)
        outState.putBoolean("PullToRefresh", ltSwipeToRefresh.isRefreshing)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        Log.wtf("Test", "On fragment destroy view")
        // Show the activity fab
        activity.fab.isEnabled = true
        activity.fab.show()
    }

    // Called from activity onBackPressed()
    fun onBackPressed() {
        if(currentDirPath != rootStoragePath) {
            recyclerView.stopScroll()

            // Stop the result of a currently running task
            val idOFTaskToCancel = fileManager.findIdOfTaskToCancel()
            if(idOFTaskToCancel != null) {
                fileManager.cancelTaskWithID(idOFTaskToCancel)
                fileManager.clearPendingTaskResultsFromActivity(
                        RemoteFileHandlerThread.RESULT_LISTED_DIRECTORY)
            }

            // Restore the lists previous state
            val (prevItemPos, prevScrollOffset) = stateStack.pop()
            this.prevItemPos = prevItemPos
            this.prevScrollOffset = prevScrollOffset

            // Gets the parent directory of the currentDirectory
            // Then updates the currentDirPath reference
            currentDirPath = fileManager.getParentDirectoryPath(currentDirPath)

            // Use a cached entry or list the directory if cache is empty
            if(fileManager.useCachedFolder(currentDirPath)) {
                Log.wtf("Test", "From cache")
                onFilesListed()
            } else {
                Log.wtf("Test", "No cache")
                progressBar.visibility = View.VISIBLE
                fileManager.listDirectory(currentDirPath)
            }

        } else {
            fragmentManager.popBackStack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if(this.isRemoving) {
            Log.wtf("Test", "Fragment removing. Thread destroying")
            fileManager.exit()
        }

        Log.wtf("Test", "On fragment destroy")
    }
}