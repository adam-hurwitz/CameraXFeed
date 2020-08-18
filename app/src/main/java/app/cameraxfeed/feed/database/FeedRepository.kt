package app.cameraxfeed.feed.database

import android.util.Log
import androidx.lifecycle.asFlow
import androidx.paging.PagedList
import androidx.paging.toLiveData
import app.cameraxfeed.feed.state.Post
import app.cameraxfeed.utils.PAGE_SIZE
import app.cameraxfeed.utils.Resource
import app.cameraxfeed.utils.Status
import app.cameraxfeed.utils.Status.ERROR
import app.cameraxfeed.utils.Status.LOADING
import app.cameraxfeed.utils.Status.SUCCESS
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
class FeedRepository @Inject constructor(private val dao: FeedDao) {
    private val LOG_TAG = FeedRepository::class.simpleName
    fun insertPost(post: Post) = flow<Resource<Status>> {
        emit(Resource.loading(LOADING))
        try {
            dao.insertPost(post)
            emit(Resource.success(SUCCESS))
        } catch (error: Exception) {
            Log.e(LOG_TAG, "loadFeed error: ${error.localizedMessage}")
            emit(Resource.error("loadFeed error: ${error.localizedMessage}", ERROR))
        }
    }

    fun queryPosts() = flow<Resource<PagedList<Post>>> {
        emit(Resource.loading(null))
        try {
            val posts = dao.queryPosts().toLiveData(PAGE_SIZE).asFlow()
            posts.collect {
                emit(Resource.success(it))
            }
        } catch (error: Exception) {
            Log.e(LOG_TAG, "loadFeed error: ${error.localizedMessage}")
        }
    }

}