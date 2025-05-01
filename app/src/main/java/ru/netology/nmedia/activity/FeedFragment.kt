package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.ImageViewingFragment.Companion.textArg2
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.viewLifecycle
import ru.netology.nmedia.util.viewLifecycleScope
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    private val authViewModel by viewModels<AuthViewModel>()

    lateinit var appActivity: AppActivity

    private fun takeAppActivity(): AppActivity {

        appActivity = if (::appActivity.isInitialized) appActivity
        else (requireActivity() as AppActivity)

        return appActivity

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {

                    if (authViewModel.isAuthenticated) {
                    if (!post.likedByMe) viewModel.likeById(post.id)
                    else viewModel.removeLike(post.id)
                }

                else {
                    dialogBuilder(forLikes = true)
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

            override fun openPhoto(post: Post) {

                findNavController().navigate(R.id.action_feedFragment_to_imageViewingFragment,
                    Bundle().apply { textArg2 = post.attachment?.url })
            }

        })

        binding.list.adapter = adapter

            viewModel.data.flowWithLifecycle(viewLifecycle).onEach { pagingData ->
                adapter.submitData(pagingData)
                binding.emptyText.isVisible = adapter.itemCount == 0



            }.launchIn(viewLifecycleScope)


        viewModel.dataState.flowWithLifecycle(viewLifecycle).onEach { stateModel ->
            binding.progress.isVisible = stateModel.loading
            binding.swiperefresh.isRefreshing = stateModel.refreshing
            if (stateModel.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry_loading) {
                        viewModel.loadPosts()
                    }.show()
            }

            if (stateModel.likeError) {
                 viewModel.toastFun()
                viewModel.cleanModel()
            }

            if (!stateModel.postIsDeleted) {
                 viewModel.toastFun()
                viewModel.cleanModel()
            }

        }.launchIn(viewLifecycleScope)



        viewModel.newPostData.flowWithLifecycle(viewLifecycle).onEach { posts ->
            binding.extendedFab.isVisible = posts?.isNotEmpty() ?: false
            // binding.extendedFab.visibility =
            // if (it.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE

        }.launchIn(viewLifecycleScope)

        binding.extendedFab.setOnClickListener {
            viewModel.newPostsIsVisible()
            binding.list.smoothScrollToPosition(0)

        }


//        viewLifecycleOwner.lifecycleScope.launch {
//            adapter.loadStateFlow.collect {
//
//                viewModel.newerCount(
//                    adapter.snapshot().items.firstOrNull()?.id ?: 0L
//                )
//                    .flowWithLifecycle(viewLifecycle).onEach { count ->
//                        println(count)
//                        binding.extendedFab.text = getString(R.string.extended_fab_text)
//                            .format("$count")
//                    }.launchIn(viewLifecycleScope)
//            }
//
//        }



        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
            adapter.loadStateFlow.collectLatest {
                binding.swiperefresh.isRefreshing = it.refresh is LoadState.Loading
                            || it.prepend is LoadState.Loading
                            || it.append is LoadState.Loading

                // Показ "рефреша" при изменении состояний загрузки PagingData
            }
            }
        }


        authViewModel.state.flowWithLifecycle(viewLifecycle).onEach {
            adapter.refresh() // Обновление списка при (раз)авторизации

        }.launchIn(viewLifecycleScope)


        binding.swiperefresh.setOnRefreshListener {
            viewModel.toastFun(true)
            viewModel.cleanNewPost()
            // viewModel.refreshing()
            adapter.refresh()
            binding.list.smoothScrollToPosition(0)
        }


        binding.fab.setOnClickListener {
            if (authViewModel.isAuthenticated) findNavController()
                .navigate(R.id.action_feedFragment_to_newPostFragment)
            else {

                dialogBuilder(forPosts = true)

            }

        }

        binding.list.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {


                    if (dy > 0) { // Пользователь прокручивает вниз

                        takeAppActivity().supportActionBar?.hide()
                        binding.fab.visibility = View.INVISIBLE
                        binding.extendedFab.isVisible = false

                    } else { // Пользователь прокручивает вверх

                        takeAppActivity().supportActionBar?.show()
                        binding.fab.visibility = View.VISIBLE
                        binding.extendedFab.isVisible =
                            viewModel.newPostData.value?.isNotEmpty() ?: false
                    }

                    super.onScrolled(recyclerView, dx, dy)

                }

            }
        )




        return binding.root
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        fun colorSetter(resId: Int) = AppCompatResources.getDrawable(requireContext(), resId)
        // "Слушатель" навигации
        findNavController().addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.imageViewingFragment -> {
                    takeAppActivity().apply {
                        supportActionBar?.setBackgroundDrawable(colorSetter(R.color.black))
                        supportActionBar?.hide()
                        hideStatusBar(true)
                    }
                }

                R.id.application_login_fragment -> {
                    takeAppActivity().apply {
                        supportActionBar?.hide()

                    }

                }
                else -> {
                    takeAppActivity().apply {
                        supportActionBar?.setBackgroundDrawable(colorSetter(R.color.colorPrimary))
                        supportActionBar?.show()
                    }
                }
            }
        }
        super.onCreate(savedInstanceState)
    }



    fun dialogBuilder(forPosts: Boolean = false, forLikes: Boolean = false) {

        val writePosts = "Чтобы иметь возможность писать посты, войдите в NMedia."

        val putLikes = "Чтобы иметь возможность ставить лайки, войдите в NMedia."

        val standardPhrase = "Чтобы иметь возможность пользоваться всеми функциями, войдите в NMedia."

        val phrase = when {
            forPosts -> writePosts
            forLikes -> putLikes
            else -> standardPhrase
        }

        val dialogBuilder = AlertDialog.Builder(requireActivity())

        dialogBuilder.setMessage(phrase)
            .setCancelable(false) // Если установить значение false, то пользователь
//          не сможет закрыть диалоговое окно, например, нажатием в любой точке экрана.
//          В таком случае пользователь должен нажать одну из предоставленных опций.

            .setPositiveButton(getString(R.string.entry)) { dialog, _ ->
                findNavController()
                    .navigate(R.id.action_feedFragment_to_application_login_fragment)
                dialog.cancel()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
        val alert = dialogBuilder.create()

        alert.setTitle("Вход в NMedia")

        alert.setIcon(R.drawable.ic_netology_48dp)

        alert.show()
    }

}
