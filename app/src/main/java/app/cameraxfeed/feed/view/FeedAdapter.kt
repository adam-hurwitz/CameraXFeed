package app.cameraxfeed.feed.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.cameraxfeed.databinding.PostCellBinding
import app.cameraxfeed.feed.state.Post
import app.cameraxfeed.utils.setPostImage
import app.cameraxfeed.utils.setProfileImage

val DIFF_UTIL = object : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post) =
        (oldItem.username + oldItem.imageString) == (newItem.username + newItem.imageString)

    override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
}

class FeedAdapter : PagedListAdapter<Post, FeedAdapter.ViewHolder>(DIFF_UTIL) {

    inner class ViewHolder(private val binding: PostCellBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.username.text = post.username
            binding.profileImage.setProfileImage(post.profileImage)
            binding.postImage.setPostImage(post.imageString)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(PostCellBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

}
