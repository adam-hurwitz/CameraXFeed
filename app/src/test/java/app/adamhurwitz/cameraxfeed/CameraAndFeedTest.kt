package app.cameraxfeed

import android.util.Log
import app.adamhurwitz.cameraxfeed.TestCase
import app.adamhurwitz.cameraxfeed.TestExtension
import app.adamhurwitz.cameraxfeed.testCaseStream
import app.adamhurwitz.cameraxfeed.utils.MOCK_IMAGE_STRING
import app.adamhurwitz.cameraxfeed.utils.MOCK_TIMESTAMP
import app.adamhurwitz.cameraxfeed.utils.asPagedList
import app.adamhurwitz.cameraxfeed.utils.mockInsertPost
import app.adamhurwitz.cameraxfeed.utils.mockPost
import app.adamhurwitz.cameraxfeed.utils.mockPostList
import app.adamhurwitz.cameraxfeed.utils.mockQueryPosts
import app.cameraxfeed.R.string.feed_load_error_message
import app.cameraxfeed.R.string.save_post_error_message
import app.cameraxfeed.camera.state.CameraView
import app.cameraxfeed.camera.state.CameraViewIntent
import app.cameraxfeed.camera.state.CameraViewState
import app.cameraxfeed.feed.database.FeedRepository
import app.cameraxfeed.feed.state.FeedView
import app.cameraxfeed.feed.state.FeedViewIntent
import app.cameraxfeed.feed.state.FeedViewState
import app.cameraxfeed.feed.state.Post
import app.cameraxfeed.utils.Event
import app.cameraxfeed.utils.MOCK_PROFILE_IMAGE
import app.cameraxfeed.utils.MOCK_USERNAME
import app.cameraxfeed.viewmodel.FeedViewModel
import io.mockk.coEvery
import io.mockk.mockkClass
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@ExperimentalCoroutinesApi
@ExtendWith(TestExtension::class)
class CameraAndFeedTest(
    val testCoroutineDispatcher: TestCoroutineDispatcher,
    val testCoroutineScope: TestCoroutineScope
) {

    private fun TestCaseStream() = testCaseStream()
    val cameraViewIntent = CameraViewIntent()
    val feedViewIntent = FeedViewIntent()
    private val repository = mockkClass(FeedRepository::class)

    @ParameterizedTest
    @MethodSource("TestCaseStream")
    fun FeedView(testCase: TestCase) = testCoroutineDispatcher.runBlockingTest {
        mockComponents(testCase)
        val viewModel = FeedViewModel(
            coroutineScopeProvider = testCoroutineScope,
            repository = repository
        )
        viewModel.bindCameraViewIntents(object: CameraView {
            override fun initState() = cameraViewIntent.initState
            override fun savePost() = cameraViewIntent.savePost
            override fun render(state: CameraViewState.PostCameraViewState) {
                if (state.isLoading || (state.isSuccess != null && state.isSuccess!!)) {
                    assertThat(state.error).isEqualTo(null)
                } else {
                    assertThat(state.error).isEqualTo(save_post_error_message)
                }
            }
        })
        cameraViewIntent.savePost.value = Event(
            Post(
                username = MOCK_USERNAME,
                profileImage = MOCK_PROFILE_IMAGE,
                timestamp = MOCK_TIMESTAMP,
                postImage = MOCK_IMAGE_STRING
            )
        )

        viewModel.bindFeedViewIntents(object: FeedView {
            override fun initState() = feedViewIntent.initState
            override fun loadFeed() = feedViewIntent.loadFeed
            override fun render(state: FeedViewState.PostFeedViewState) {
                if (state.isLoading) {
                    assertThat(state.feed).isNull()
                }
                state.feed?.let {
                    if (state.feed!!.isNotEmpty())
                        assertThat(state.feed).isEqualTo(mockPostList.asPagedList())
                }
                state.error?.let{
                    assertThat(state.error!!).isEqualTo(feed_load_error_message)
                }
            }

        })
        feedViewIntent.loadFeed.value = Event(true)
    }

    private fun mockComponents(testCase: TestCase) {
        mockkStatic(Log::class)
        coEvery { Log.v(any(), any()) } returns 0
        coEvery { repository.insertPost(mockPost) } returns mockInsertPost(testCase.status)
        coEvery { repository.queryPosts() } returns mockQueryPosts(testCase.status, mockPostList)
    }
}