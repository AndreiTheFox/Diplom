package ru.fox.diplom.activity


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.fox.diplom.R
import ru.fox.diplom.activity.NewPostFragment.Companion.textArg
import ru.fox.diplom.adapter.FeedAdapter
import ru.fox.diplom.adapter.OnInteractionListener
import ru.fox.diplom.adapter.PagingLoadStateAdapter
import ru.fox.diplom.auth.AppAuth
import ru.fox.diplom.databinding.FragmentFeedBinding
import ru.fox.diplom.dialogs.LogoutDialog
import ru.fox.diplom.dto.Post
import ru.fox.diplom.viewmodel.AuthViewModel
import ru.fox.diplom.viewmodel.FeedItemViewModel
import javax.inject.Inject


@AndroidEntryPoint
class FeedFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: FeedItemViewModel by activityViewModels()
    val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        super.onCreate(savedInstanceState)

        val binding = FragmentFeedBinding.inflate(
            inflater,
            container,
            false
        )

        //Menu code
        authViewModel.state.observe(viewLifecycleOwner) {
            requireActivity().invalidateOptionsMenu()
        }
        val adapter = FeedAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = post.content
                    }
                )
            }

            override fun onPostClick(post: Post) {
                if (authViewModel.authorized) {
                    findNavController().navigate(
                        R.id.action_feedFragment_to_postFragment,
                        Bundle().apply {
                            putLong("postId", post.id)
                        }
                    )
                } else {
                    findNavController().navigate(R.id.action_feedFragment_to_loginFragment)
                }
            }

            override fun onImageClick(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_imageFragment,
                    Bundle().apply {
                        putString("attachUrl", post.attachment?.url)
                    }
                )
            }

            override fun onLike(post: Post) {
                if (authViewModel.authorized) {
                    viewModel.likeById(post)
                } else {
                    findNavController().navigate(R.id.action_feedFragment_to_loginFragment)
                }
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
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
        })

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.auth_menu, menu)
                    menu.setGroupVisible(R.id.registered, authViewModel.authorized)
                    menu.setGroupVisible(R.id.unregistered, !authViewModel.authorized)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.login -> {
                            findNavController()
                                .navigate(
                                    R.id.action_feedFragment_to_loginFragment
                                )
                            true
                        }

                        R.id.loguot -> {
                            LogoutDialog(appAuth).show(
                                parentFragmentManager, LogoutDialog.TAG
                            )
                            true
                        }

                        R.id.register -> {
                            findNavController()
                                .navigate(
                                    R.id.action_feedFragment_to_registerFragment
                                )
                            true
                        }

                        else -> false
                    }

            },
            viewLifecycleOwner
        )
        //End of Menu code

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PagingLoadStateAdapter { adapter.retry() },
            footer = PagingLoadStateAdapter { adapter.retry() },
        )

//        viewModel.dataState.observe(viewLifecycleOwner) { state ->
//            binding.progress.isVisible = state.loading
//            binding.swiperefresh.isRefreshing = state.refreshing
//            if (state.error) {
//                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
//                    .setAction(R.string.retry) { viewModel.loadPosts() }
//                    .show()
//            }
//        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest(adapter::submitData)
            }
        }

        @Suppress("DEPRECATION")
        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }
        @Suppress("DEPRECATION")
        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                val isListEmpty = state.refresh is LoadState.NotLoading && adapter.itemCount == 0
                // show empty list
                binding.emptyText.isVisible = isListEmpty

                // Only show the list if refresh succeeds.
                binding.list.isVisible = !isListEmpty

                // Show the retry state if initial load or refresh fails.
                binding.retry.isVisible = state.source.refresh is LoadState.Error

                binding.swiperefresh.isRefreshing = state.refresh is LoadState.Loading
                // Toast on any error, regardless of whether it came from RemoteMediator or PagingSource
                val errorState = state.source.append as? LoadState.Error
                    ?: state.source.prepend as? LoadState.Error
                    ?: state.append as? LoadState.Error
                    ?: state.prepend as? LoadState.Error
                errorState?.let {
                    Toast.makeText(
                        parentFragment?.context,
                        "\uD83D\uDE28 Wooops ${it.error}",
                        Toast.LENGTH_LONG
                    ).show()

                }
            }
        }

//        viewModel.data.observe(viewLifecycleOwner) { state ->
//            adapter.submitList(state.posts)
//            binding.emptyText.isVisible = state.empty
//        }

        binding.fab.setOnClickListener {
            if (authViewModel.authorized) {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            } else {
                findNavController().navigate(R.id.action_feedFragment_to_loginFragment)
            }
        }

        binding.retry.setOnClickListener {
            adapter.retry()
        }
        binding.swiperefresh.setOnRefreshListener {
            adapter.refresh()
        }
        authViewModel.state.observe(viewLifecycleOwner) {
            adapter.refresh()
        }

//        viewModel.newPostsCount.observe(viewLifecycleOwner) {
//            if (it > 0) {
//                binding.loadNewPosts.visibility = View.VISIBLE
//                val buttonText = getString(R.string.new_posts) + "$it"
//                binding.loadNewPosts.text = buttonText
//            } else {
//                binding.loadNewPosts.visibility = View.GONE
//            }
//        }

        //Плавное прокручивание ленты постов при добавлении свежих загруженных постов в holder адаптера после нажатия пользователя
//        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
//            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//                if (positionStart == 0) {
//                    binding.list.smoothScrollToPosition(0)
//                }
//            }
//        })

        //Загрузить свежие посты
//        binding.loadNewPosts.setOnClickListener {
//            viewModel.updateFeed()
//            binding.loadNewPosts.visibility = View.GONE
//        }

        return binding.root
    }
}//Конец Main