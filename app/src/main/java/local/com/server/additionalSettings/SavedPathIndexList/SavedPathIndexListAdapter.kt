package local.com.server.additionalSettings

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import local.com.server.R
import local.com.server.additionalSettings.SavedPathIndexList.IndexList
import local.com.server.additionalSettings.utils.Constants
import local.com.server.databinding.ItemSavedPathsRowsBinding

class SavedPathIndexListAdapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<SavedPathIndexListAdapter.MyViewHolder>() {

    private var userList = mutableListOf<IndexList>()  // use mutableListOf for easy updates

    interface OnItemClickListener {
        fun onItemClicked(item: IndexList)
        fun onItemLongClicked(item: IndexList, position: Int)
        fun onLaunchClicked(item: IndexList) // <-- new for Launch button
    }

    inner class MyViewHolder(
        private val binding: ItemSavedPathsRowsBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        private val sharedPreferences = context.applicationContext
            .getSharedPreferences(Constants.SAVE_PORT_VALUES, Context.MODE_PRIVATE)

        fun bind(currentItem: IndexList, position: Int) {
            val localUrl = sharedPreferences.getString(Constants.localUrl, "").orEmpty()
            val title = "${position + 1}. $localUrl${currentItem.VALUES}"
            val titleCopy = "$localUrl${currentItem.VALUES}"
            binding.textCloDemoFolder.text = title

            // Normal click
            binding.textCloDemoFolder.setOnClickListener {
                listener.onItemClicked(currentItem)
            }

            // Long click
            binding.textCloDemoFolder.setOnLongClickListener {
                listener.onItemLongClicked(currentItem, adapterPosition)
                true
            }

            // Copy button
            binding.textCopy.setOnClickListener {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("CopiedText", titleCopy)
                clipboard.setPrimaryClip(clip)

                Toast.makeText(context, "\"$titleCopy\" copied!", Toast.LENGTH_SHORT).show()
            }

            // Launch button
            binding.textLaunchFile.setOnClickListener {
                listener.onLaunchClicked(currentItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemSavedPathsRowsBinding.bind(
            LayoutInflater.from(parent.context).inflate(R.layout.item_saved_paths_rows, parent, false)
        )
        return MyViewHolder(binding, parent.context)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(userList[position], position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(indexUser: List<IndexList>) {
        this.userList = indexUser.toMutableList()
        notifyDataSetChanged()
    }
}
