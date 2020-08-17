package app.cameraxfeed.feed.database

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.cameraxfeed.feed.state.Post

@Dao
interface FeedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPost(post: Post)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPosts(post: List<Post>)

    @Query("SELECT * FROM feedTable ORDER BY timestamp DESC")
    fun queryPosts(): DataSource.Factory<Int, Post>

}

