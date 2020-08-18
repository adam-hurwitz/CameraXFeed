package app.cameraxfeed.feed.state

import androidx.paging.PagedList

sealed class FeedViewState {
    data class PostFeedViewState(
        val isLoading: Boolean,
        val feed: PagedList<Post>? = null,
        val error: Int? = null
     ) : FeedViewState()
}