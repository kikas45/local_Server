package smarty.com.excel.additionalSettings.SavedPathIndexList

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "index_database", indices = [Index(value = ["VALUES"], unique = true)])
data class IndexList(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var VALUES: String
)
