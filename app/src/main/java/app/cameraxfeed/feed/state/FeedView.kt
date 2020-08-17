package app.cameraxfeed.feed.state

import app.cameraxfeed.feed.state.FeedViewState.PostFeedViewState
import app.cameraxfeed.utils.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

@ExperimentalCoroutinesApi
interface FeedView {
    /**
     * Intent to load the current feed state
     *
     * @return A flow that inits the current feed state
     */
    fun initState(): Flow<Event<Boolean>>

    /**
     * Intent to load the feed from the database
     *
     * @return A flow that inits loading the feed state from the database
     */
    fun loadFeed(): Flow<Event<Boolean?>>

    /**
     * Renders the feed view state
     *
     * @param state The current view state display
     */
    fun render(state: PostFeedViewState)
}

