package sync2app.com.syncapplive.additionalSettings.SavedPathIndexList

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import sync2app.com.syncapplive.R
import sync2app.com.syncapplive.databinding.ItemSavedPathsRowsBinding
class SavedPathIndexListAdapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<SavedPathIndexListAdapter.MyViewHolder>() {

    private var userList = mutableListOf<IndexList>()  // use mutableListOf for easy removal

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemSavedPathsRowsBinding.bind(
            LayoutInflater.from(parent.context).inflate(R.layout.item_saved_paths_rows, parent, false)
        )
        return MyViewHolder(binding, parent.context)
    }

    interface OnItemClickListener {
        fun onItemClicked(item: IndexList)
        fun onItemLongClicked(item: IndexList, position: Int)
    }

    inner class MyViewHolder(
        private val binding: ItemSavedPathsRowsBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(currentItem: IndexList, position: Int) {
            // Show number + value
            val title = "${position + 1}. ${currentItem.VALUES}"
            binding.textCloDemoFolder.text = title

            // Handle normal click
            binding.textCloDemoFolder.setOnClickListener {
                listener.onItemClicked(currentItem)
            }

            // Handle long click for delete
            binding.textCloDemoFolder.setOnLongClickListener {
                listener.onItemLongClicked(currentItem, adapterPosition)
                true
            }

            // Handle copy button
            binding.textCopy.setOnClickListener {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("CopiedText", currentItem.VALUES)
                clipboard.setPrimaryClip(clip)

                Toast.makeText(context, "\"${currentItem.VALUES}\" copied!", Toast.LENGTH_SHORT).show()
            }
        }
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
