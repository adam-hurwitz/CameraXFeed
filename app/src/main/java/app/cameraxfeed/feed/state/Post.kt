package app.cameraxfeed.feed.state

import androidx.room.Entity
import app.cameraxfeed.utils.CAMERAXFEED_TABLE_NAME

@Entity(tableName = CAMERAXFEED_TABLE_NAME, primaryKeys = arrayOf("username", "postImage"))
data class Post(
    val username: String = "",
    val profileImage: Int,
    val text: String = "",
    val timestamp: String,
    val postImage: String
)
