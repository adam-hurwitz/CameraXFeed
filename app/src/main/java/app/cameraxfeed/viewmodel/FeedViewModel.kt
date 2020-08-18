package app.cameraxfeed.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import app.cameraxfeed.R.string.feed_load_error_message
import app.cameraxfeed.R.string.save_post_error_message
import app.cameraxfeed.camera.state.CameraView
import app.cameraxfeed.camera.state.CameraViewState.PostCameraViewState
import app.cameraxfeed.feed.database.FeedRepository
import app.cameraxfeed.feed.state.FeedView
import app.cameraxfeed.feed.state.FeedViewState.PostFeedViewState
import app.cameraxfeed.feed.state.Post
import app.cameraxfeed.utils.Event
import app.cameraxfeed.utils.Status.ERROR
import app.cameraxfeed.utils.Status.LOADING
import app.cameraxfeed.utils.Status.SUCCESS
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

@ExperimentalCoroutinesApi
class FeedViewModel @AssistedInject constructor(
    @Assisted private val coroutineScopeProvider: CoroutineScope?,
    private val repository: FeedRepository
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
        repository.insertPost(post).onEach {
            when (it.status) {
                LOADING -> cameraState.value = Event(PostCameraViewState(isLoading = true))
                SUCCESS -> Log.v(LOG_TAG, "Create post success")
                ERROR -> cameraState.value = Event(
                    PostCameraViewState(
                        isLoading = false,
                        error = save_post_error_message
                    )
                )
            }
        }.launchIn(coroutineScope)
    }

    private fun loadFeed() {
        repository.queryPosts().onEach {
            when (it.status) {
                LOADING -> feedState.value = PostFeedViewState(isLoading = true)
                SUCCESS -> {
                    feedState.value = PostFeedViewState(
                        isLoading = false,
                        feed = it.data
                    )
                }
                ERROR -> feedState.value = PostFeedViewState(
                    isLoading = false,
                    error = feed_load_error_message
                )
            }
        }.launchIn(coroutineScope)
    }
}
