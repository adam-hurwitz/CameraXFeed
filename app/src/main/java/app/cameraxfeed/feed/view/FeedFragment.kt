package app.cameraxfeed.feed.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.App
import app.cameraxfeed.R.id.nav_root
import app.cameraxfeed.databinding.FragmentFeedBinding
import app.cameraxfeed.dependencyinjection.Component
import app.cameraxfeed.feed.state.FeedView
import app.cameraxfeed.feed.state.FeedViewIntent
import app.cameraxfeed.feed.state.FeedViewState.PostFeedViewState
import app.cameraxfeed.utils.Event
import app.cameraxfeed.viewmodel.FeedViewModel
import app.cameraxfeed.viewmodel.navGraphSavedStateViewModels
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import kotlinx.android.synthetic.main.activity_main.main
import kotlinx.android.synthetic.main.fragment_feed.recyclerView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull

@ExperimentalCoroutinesApi
class FeedFragment : Fragment(), FeedView {

    private lateinit var component: Component
    private lateinit var binding: FragmentFeedBinding
    private lateinit var viewModel: FeedViewModel
    private lateinit var adapter: FeedAdapter
    private val intent = FeedViewIntent()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        component = (context.applicationContext as App).component
        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val feedViewModel: FeedViewModel by navGraphSavedStateViewModels(nav_root) { handle ->
            component.feedViewModelFactory().create()
        }
        this.viewModel = feedViewModel
        binding = FragmentFeedBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapters()
        viewModel.bindFeedViewIntents(this)
        if (savedInstanceState == null)
            intent.loadFeed.value = Event(true)
    }

    override fun initState() = intent.initState

    override fun loadFeed() = intent.loadFeed.filterNotNull()

    override fun render(state: PostFeedViewState) {
        state.feed?.let {
            if (state.isLoading) {
                binding.progressbar.visibility = VISIBLE
            }
            if (state.feed.isNotEmpty()) {
                binding.progressbar.visibility = GONE
                adapter.submitList(state.feed)
                binding.feedEmpty.feedEmptyContainer.visibility = GONE
            } else binding.feedEmpty.feedEmptyContainer.visibility = VISIBLE
            state.error?.let {
                binding.progressbar.visibility = GONE
                Snackbar.make(requireActivity().main, getString(state.error), LENGTH_LONG).show()
            }
        }
    }

    private fun initAdapters() {
        adapter = FeedAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }
}