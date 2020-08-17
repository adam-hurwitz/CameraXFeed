package app.cameraxfeed.dependencyinjection

import android.app.Application
import android.content.Context.MODE_PRIVATE
import androidx.room.Room
import app.cameraxfeed.feed.database.FeedDatabase
import app.cameraxfeed.utils.CAMERAXFEED_DATABASE_NAME
import app.cameraxfeed.utils.CAMERAXFEED_SHARED_PREF_NAME
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class Module(private val app: Application) {

    @Singleton
    @Provides
    fun providesSharedPreferences() =
        app.getSharedPreferences(CAMERAXFEED_SHARED_PREF_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun providesDao() = Room.databaseBuilder(
        app,
        FeedDatabase::class.java,
        CAMERAXFEED_DATABASE_NAME
    ).build().feedDao()

}
