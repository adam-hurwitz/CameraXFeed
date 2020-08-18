package app.adamhurwitz.cameraxfeed.utils

import app.cameraxfeed.feed.state.Post
import app.cameraxfeed.utils.MOCK_PROFILE_IMAGE
import app.cameraxfeed.utils.MOCK_USERNAME
import app.cameraxfeed.utils.Resource.Companion.error
import app.cameraxfeed.utils.Resource.Companion.loading
import app.cameraxfeed.utils.Resource.Companion.success
import app.cameraxfeed.utils.Status
import app.cameraxfeed.utils.Status.ERROR
import app.cameraxfeed.utils.Status.LOADING
import app.cameraxfeed.utils.Status.SUCCESS
import kotlinx.coroutines.flow.flow

const val MOCK_TIMESTAMP = "2020-08-18-10-00-00"
const val MOCK_IMAGE_STRING = "https://www.someHost.com/someName.jpg"

val mockPost = Post(
    username = MOCK_USERNAME,
    profileImage = MOCK_PROFILE_IMAGE,
    timestamp = MOCK_TIMESTAMP,
    postImage = MOCK_IMAGE_STRING
)

val mockPostList = listOf(
    mockPost
)

fun mockInsertPost(status: Status) = flow {
    when (status) {
        LOADING -> emit(loading(LOADING))
        SUCCESS -> emit(success(SUCCESS))
        ERROR -> emit(error("", ERROR))
    }
}

fun mockQueryPosts(status: Status, mockFeedList: List<Post>) = flow {
    when (status) {
        LOADING -> emit(loading(null))
        SUCCESS -> emit(success(mockFeedList.asPagedList()))
        ERROR -> emit(error("", null))
    }
}