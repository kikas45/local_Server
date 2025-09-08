package smarty.com.excel.additionalSettings.SavedPathIndexList

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [IndexList::class], version = 1, exportSchema = false)
abstract class IndexDatabase : RoomDatabase() {

    abstract fun indexDao(): IndexDao

    companion object {
        @Volatile
        private var INSTANCE: IndexDatabase? = null

        fun getDatabase(context: Context): IndexDatabase {
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IndexDatabase::class.java,
                    "index_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }

}