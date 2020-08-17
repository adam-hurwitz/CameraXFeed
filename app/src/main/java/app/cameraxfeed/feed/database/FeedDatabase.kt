package app.cameraxfeed.feed.database

import androidx.room.Database
import androidx.room.RoomDatabase
import app.cameraxfeed.feed.state.Post
import app.cameraxfeed.utils.CAMERAXFEED_DATABASE_VERSION

@Database(entities = [Post::class], version = CAMERAXFEED_DATABASE_VERSION)
abstract class FeedDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
}