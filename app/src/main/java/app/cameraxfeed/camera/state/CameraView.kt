package app.cameraxfeed.camera.state

import app.cameraxfeed.feed.state.Post
import app.cameraxfeed.utils.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

@ExperimentalCoroutinesApi
interface CameraView {

    /**
     * Intent to load the current camera state
     *
     * @return A flow that inits the current camera state
     */
    fun initState(): Flow<Event<Boolean>>

    /**
     * Intent to save the media post
     *
     * @return A flow that inits saving the media post
     */
    fun savePost(): Flow<Event<Post?>>

    /**
     * Renders the feed view state
     *
     * @param state The current view state display
     */
    fun render(state: CameraViewState.PostCameraViewState)
}

