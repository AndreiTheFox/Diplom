package ru.fox.diplom.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.fox.diplom.R
import ru.fox.diplom.activity.NewPostFragment.Companion.textArg
import ru.fox.diplom.adapter.OnInteractionListener
import ru.fox.diplom.adapter.PostViewHolder
import ru.fox.diplom.databinding.FragmentPostBinding
import ru.fox.diplom.dto.Post
import ru.fox.diplom.viewmodel.FeedItemViewModel
@AndroidEntryPoint
class PostFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostBinding.inflate(
            inflater,
            container,
            false
        )
        val viewModel: FeedItemViewModel by activityViewModels()
        val postId = arguments?.getLong("postId")

        val adapter = PostViewHolder(binding.post, object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(
                    R.id.action_postFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = post.content

                    }
                )
            }

            override fun onLike(post: Post) {
                viewModel.likeById(post)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
                findNavController().navigateUp()
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

//            override fun onVideoClick(post: Post) {
//                val parsedUri = Uri.parse(post.video).toString().trim()
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(parsedUri))
//                startActivity(intent)
//            }
        })
//        viewModel.data.observe(viewLifecycleOwner) { posts ->
//            val post = posts.posts.find { it.id == postId } ?: return@observe
//            adapter.bind(post)
//        }

        return binding.root
    }
}