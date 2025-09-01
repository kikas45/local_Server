package sync2app.com.syncapplive.additionalSettings.SavedPathIndexList

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface IndexDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUser(indexList: IndexList)

    @Update
    suspend fun updateUser(indexList: IndexList)

    @Delete
    suspend fun deleteUser(indexList: IndexList)

    @Query("DELETE FROM index_database")
    suspend fun deleteAllUsers()

    @Query("SELECT * FROM index_database ORDER BY id DESC")
    fun readAllData(): LiveData<List<IndexList>>
}
