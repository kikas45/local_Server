package smarty.com.excel.additionalSettings.SavedPathIndexList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IndexViewModel(application: Application): AndroidViewModel(application) {

    val readAllData: LiveData<List<IndexList>>
    private val repository: IndexRepository

    init {
        val indexDao = IndexDatabase.getDatabase(
            application
        ).indexDao()
        repository = IndexRepository(indexDao)
        readAllData = repository.readAllData
    }

    fun addUser(indexList: IndexList){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addUser(indexList)
        }
    }

    fun updateUser(indexList: IndexList){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(indexList)
        }
    }

    fun deleteUser(indexList: IndexList){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUser(indexList)
        }
    }


    fun deleteAllUsers(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllUsers()
        }
    }

}