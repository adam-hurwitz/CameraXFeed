package app.cameraxfeed.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.paging.toLiveData
import app.cameraxfeed.R.string.feed_load_error_message
import app.cameraxfeed.R.string.save_post_error_message
import app.cameraxfeed.camera.state.CameraView
import app.cameraxfeed.camera.state.CameraViewState.PostCameraViewState
import app.cameraxfeed.feed.database.FeedDao
import app.cameraxfeed.feed.state.FeedView
import app.cameraxfeed.feed.state.FeedViewState.PostFeedViewState
import app.cameraxfeed.feed.state.Post
import app.cameraxfeed.utils.Event
import app.cameraxfeed.utils.PAGE_SIZE
import app.cameraxfeed.utils.onEachEvent
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class FeedViewModel @AssistedInject constructor(
    @Assisted private val coroutineScopeProvider: CoroutineScope?,
    private val dao: FeedDao
) : ViewModel() {

    @AssistedInject.Factory
    interface Factory {
        fun create(
            coroutineScopeProvider: CoroutineScope? = null
        ): FeedViewModel
    }

    private val LOG_TAG = FeedViewModel::class.java.simpleName
    private val coroutineScope = getViewModelScope(coroutineScopeProvider)
    private val cameraState = MutableStateFlow<Event<PostCameraViewState?>>(Event(null))
    private val feedState = MutableStateFlow<PostFeedViewState?>(null)

    fun bindCameraViewIntents(view: CameraView) {
        view.initState().onEachEvent {
            cameraState.filterNotNull().onEachEvent {
                view.render(it)
            }.launchIn(coroutineScope)
        }.launchIn(coroutineScope)
        view.savePost().onEachEvent {
            createPost(it)
        }.launchIn(coroutineScope)
    }

    fun bindFeedViewIntents(view: FeedView) {
        view.initState().onEachEvent {
            feedState.filterNotNull().collect {
                view.render(it)
            }
        }.launchIn(coroutineScope)
        view.loadFeed().onEachEvent {
            loadFeed()
        }.launchIn(coroutineScope)
    }

    private fun createPost(post: Post) {
        // Loading
        cameraState.value = Event(PostCameraViewState(isLoading = true))
        try {
            // Success
            coroutineScope.launch {
                dao.insertPost(post)
            }
        } catch (error: Exception) {
            // Error
            Log.e(LOG_TAG, "loadFeed error: ${error.localizedMessage}")
            cameraState.value = Event(PostCameraViewState(
                isLoading = false,
                error = save_post_error_message
            ))
        }
    }

    private fun loadFeed() {
        // Loading
        feedState.value = PostFeedViewState(
            isLoading = true,
            feed = null
        )
        try {
            // Success
            val posts = dao.queryPosts().toLiveData(PAGE_SIZE).asFlow()
            posts.onEach {
                feedState.value = PostFeedViewState(
                    isLoading = false,
                    feed = it
                )
            }.launchIn(coroutineScope)
        } catch (error: Exception) {
            // Error
            Log.e(LOG_TAG, "loadFeed error: ${error.localizedMessage}")
            feedState.value = PostFeedViewState(
                isLoading = false,
                feed = null,
                error = feed_load_error_message
            )
        }
    }
}
