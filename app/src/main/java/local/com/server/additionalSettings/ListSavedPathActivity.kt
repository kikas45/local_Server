package local.com.server.additionalSettings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import local.com.server.additionalSettings.SavedPathIndexList.IndexList
import local.com.server.additionalSettings.SavedPathIndexList.IndexViewModel
import local.com.server.additionalSettings.utils.Constants
import local.com.server.databinding.ActivityListSavedPathBinding

class ListSavedPathActivity : AppCompatActivity(),
    SavedPathIndexListAdapter.OnItemClickListener {

    private val mUserViewModel by viewModels<IndexViewModel>()

    private lateinit var binding: ActivityListSavedPathBinding

    private val adapter by lazy {
        SavedPathIndexListAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListSavedPathBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpRecyclerView()

        // Close button -> go back to DE_MO_202100
        binding.closeBs.setOnClickListener {
            navigateBack()
        }

        // Clear all data
        binding.textClearAllData.setOnClickListener {
            mUserViewModel.deleteAllUsers()
        }
    }

    private fun setUpRecyclerView() {
        lifecycleScope.launch {
            delay(500) // small delay to ensure LiveData has data
            binding.apply {
                recyclerSavedDownload.adapter = adapter
                recyclerSavedDownload.layoutManager = LinearLayoutManager(applicationContext)

                mUserViewModel.readAllData.observe(this@ListSavedPathActivity, Observer { user ->
                    adapter.setData(user)
                    if (user.isNotEmpty()) {
                        textErrorText.visibility = View.GONE
                        textClearAllData.visibility = View.VISIBLE
                    } else {
                        textClearAllData.visibility = View.GONE
                        textErrorText.visibility = View.VISIBLE
                    }
                })
            }
        }
    }

    // Handle short click on item
    override fun onItemClicked(item: IndexList) {
        // (Optional) You can define some action when item row itself is tapped
        Toast.makeText(this, "Clicked: ${item.VALUES}", Toast.LENGTH_SHORT).show()
    }

    // Handle long click -> show delete dialog
    override fun onItemLongClicked(item: IndexList, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Path")
            .setMessage("Do you want to delete \"${item.VALUES}\"?")
            .setPositiveButton("Yes") { _, _ ->
                mUserViewModel.deleteUser(item)
                Toast.makeText(this, "Deleted: ${item.VALUES}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    // Handle launch button click
    override fun onLaunchClicked(item: IndexList) {
        val localUrl = getSharedPreferences(Constants.SAVE_PORT_VALUES, MODE_PRIVATE)
            .getString(Constants.localUrl, "").orEmpty()

        val intent = Intent(this, DemoWebActivity::class.java).apply {
            putExtra("url_launch", "$localUrl/${item.VALUES}")
        }
        startActivity(intent)
    }

    @Deprecated("Use OnBackPressedDispatcher instead")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        navigateBack()
    }


    private fun navigateBack() {
        startActivity(Intent(applicationContext, SeverEngineActivity::class.java))
        finish()
    }
}
