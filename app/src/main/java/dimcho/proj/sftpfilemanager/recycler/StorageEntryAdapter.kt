package dimcho.proj.sftpfilemanager.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import dimcho.proj.sftpfilemanager.R

class StorageEntryAdapter(private val storageItems: MutableList<StorageInfo>,
                          private val onItemClickListener: ListItemClickListener)
    : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rootView: View

        if(convertView == null){
            rootView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.rv_storage_ietm, parent, false)

            rootView.setOnClickListener { onItemClickListener.onListItemClick(position) }
        } else {
            rootView = convertView
        }

        val ivStorageIcon = rootView.findViewById<ImageView>(R.id.imageView)
        val tvStorageText = rootView.findViewById<TextView>(R.id.info_text)
        val storageInfo: StorageInfo = getItem(position)

        ivStorageIcon.setImageResource(storageInfo.storageIconResource)
        tvStorageText.text = storageInfo.storageItemText

        return rootView

    }

    override fun getItem(position: Int): StorageInfo {
        return storageItems[position]
    }

    override fun getItemId(position: Int): Long {
        return storageItems[position].hashCode().toLong()
    }

    override fun getCount(): Int {
        return storageItems.size
    }
}