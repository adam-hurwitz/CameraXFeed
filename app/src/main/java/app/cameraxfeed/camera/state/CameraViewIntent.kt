package app.cameraxfeed.camera.state

import app.cameraxfeed.feed.state.Post
import app.cameraxfeed.utils.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

@ExperimentalCoroutinesApi
class CameraViewIntent (
    val initState: MutableStateFlow<Event<Boolean>> = MutableStateFlow(Event(true)),
    val savePost: MutableStateFlow<Event<Post?>> = MutableStateFlow(Event(null))
)