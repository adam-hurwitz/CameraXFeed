package app.cameraxfeed.feed.state

import app.cameraxfeed.utils.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

@ExperimentalCoroutinesApi
class FeedViewIntent (
    val initState: MutableStateFlow<Event<Boolean>> = MutableStateFlow(Event(true)),
    val loadFeed: MutableStateFlow<Event<Boolean?>> = MutableStateFlow(Event(null))
)