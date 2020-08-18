package app.adamhurwitz.cameraxfeed

import app.adamhurwitz.cameraxfeed.utils.mockPostList
import app.cameraxfeed.R.string.feed_load_error_message
import app.cameraxfeed.camera.state.CameraViewState.PostCameraViewState
import app.cameraxfeed.utils.Status.ERROR
import app.cameraxfeed.utils.Status.LOADING
import app.cameraxfeed.utils.Status.SUCCESS
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.stream.Stream

@ExperimentalCoroutinesApi
fun testCaseStream() = Stream.of(
    // Loading
    TestCase(
        status = LOADING,
        expectPostCameraViewState = PostCameraViewState(
            isLoading = true
        ),
        expectFeed = listOf()
    ),
    // Success
    TestCase(
        status = SUCCESS,
        expectPostCameraViewState = PostCameraViewState(
            isLoading = false,
            isSuccess = true
        ),
        expectFeed = mockPostList
    ),
    // Error
    TestCase(
        status = ERROR,
        expectPostCameraViewState = PostCameraViewState(
            isLoading = false,
            error = feed_load_error_message
        ),
        expectFeed = listOf()
    )
)