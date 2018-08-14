package dimcho.proj.sftpfilemanager.recycler

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import dimcho.proj.sftpfilemanager.R
import kotlinx.android.synthetic.main.rv_file_item.view.*
import java.util.ArrayList

/**
 * Created by dimcho on 06.03.18.
 */
class FilesAdapter(private val files: ArrayList<FileInfo>,
                   private val listener: ListItemClickListener)

    : RecyclerView.Adapter<FilesAdapter.FilesViewHolder>() {

    inner class FilesViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val ivFileIcon: ImageView = itemView.ivFileIcon
        val tvFileName: TextView = itemView.tvFileName
        val tvFileType: TextView = itemView.tvFileType
        val tvLastEdit: TextView = itemView.tvLastEdit

        init {
            itemView.setOnClickListener(this)
        }

        // On recycler view item click
        override fun onClick(v: View?) {
            listener.onListItemClick(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilesViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.rv_file_item, parent, false)

        return FilesViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return files.size
    }

    override fun onBindViewHolder(holder: FilesViewHolder, position: Int) {
        val currentFileInfo = files[position]

        holder.ivFileIcon.setImageResource(currentFileInfo.imgResource)
        holder.tvFileType.text = currentFileInfo.fileType.value
        holder.tvFileName.text = currentFileInfo.name
        holder.tvLastEdit.text = currentFileInfo.lastEdit
    }
}