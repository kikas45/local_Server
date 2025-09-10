package local.com.server.additionalSettings.SavedPathIndexList

import androidx.lifecycle.LiveData

class IndexRepository(private val userDao: IndexDao) {

    val readAllData: LiveData<List<IndexList>> = userDao.readAllData()

    suspend fun addUser(indexList: IndexList) {
        userDao.addUser(indexList)
    }

    suspend fun updateUser(indexList: IndexList) {
        userDao.updateUser(indexList)
    }

    suspend fun deleteUser(indexList: IndexList) {
        userDao.deleteUser(indexList)
    }

    suspend fun deleteAllUsers() {
        userDao.deleteAllUsers()
    }

}