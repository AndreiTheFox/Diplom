package ru.fox.diplom.adapter

import android.view.View
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ru.fox.diplom.BuildConfig
import ru.fox.diplom.R
import ru.fox.diplom.databinding.FragmentDetailedPostBinding
import ru.fox.diplom.dto.Post
import ru.fox.diplom.util.counterWrite
import ru.fox.diplom.util.glideDownloadFullImage
import ru.fox.diplom.util.glideDownloadRoundImage

class DetailedPostViewHolder(
    private val binding: FragmentDetailedPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {
    private val serverPathUrl = "${BuildConfig.BASE_URL}"
    private val avatarsPathUrl = "${serverPathUrl}/avatars"
    private val attachmentsUrl = "${serverPathUrl}/media"

    fun bind(post: Post) {
        binding.apply {
            val downloadAvatarUrl = "${avatarsPathUrl}/${post.authorAvatar}"
            if (post.attachment != null) {
                val downloadAttachUrl = "${attachmentsUrl}/${post.attachment.url}"
                glideDownloadFullImage(downloadAttachUrl, binding.attachment)
                binding.attachment.visibility = View.VISIBLE
            } else {
                binding.attachment.visibility = View.GONE
            }
            glideDownloadRoundImage(downloadAvatarUrl, binding.avatar)
            author.text = post.author
            published.text = post.published
            content.text = post.content
            like.text = counterWrite(post.likes)
            like.isChecked = post.likedByMe
            menu.isVisible = post.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                like.isChecked = !like.isChecked
                onInteractionListener.onLike(post)
            }

            attachment.setOnClickListener {
                onInteractionListener.onImageClick(post)
            }

            root.setOnClickListener {
                onInteractionListener.onPostClick(post)
            }
        }
    }
}