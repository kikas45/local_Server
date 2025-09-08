package smarty.com.excel.additionalSettings.fileMnager
import android.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class FileManagerAdapter(private val context: Context, private val values: List<String>) :
    ArrayAdapter<String?>(context, R.layout.simple_list_item_1, values) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.simple_list_item_1, parent, false)
        }
        val textView = convertView!!.findViewById<TextView>(R.id.text1)
        textView.text = values[position]

        // Change the text color to blue
        textView.setTextColor(context.resources.getColor(R.color.holo_blue_dark))
        return convertView
    }
}
