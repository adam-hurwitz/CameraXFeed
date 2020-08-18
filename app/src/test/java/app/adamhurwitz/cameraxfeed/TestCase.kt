package app.adamhurwitz.cameraxfeed

import app.cameraxfeed.camera.state.CameraViewState
import app.cameraxfeed.feed.state.Post
import app.cameraxfeed.utils.Status

data class TestCase(
    val status: Status,
    val expectPostCameraViewState: CameraViewState.PostCameraViewState,
    val expectFeed: List<Post>
)

