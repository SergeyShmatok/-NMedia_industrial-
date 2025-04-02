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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.ImageViewingFragment.Companion.textArg2
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel


class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    private val authViewModel by viewModels<AuthViewModel>()

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

        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts) // это про передачу постов в рисайклер
            binding.emptyText.isVisible = state.empty // --//--
        }

        viewModel.dataState.observe(viewLifecycleOwner) { stateModel ->
            binding.progress.isVisible =
                stateModel.loading // а это про отображение элементов на "верхнем" уровне (view), модели mvvm.
            binding.swiperefresh.isRefreshing = stateModel.refreshing // --//--
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

        }

        viewModel.newerCount.observe(viewLifecycleOwner) {
            println(it)
            binding.extendedFab.text = getString(R.string.extended_fab_text)
                .format("$it")
        }

        viewModel.newPostData.observe(viewLifecycleOwner) { posts ->
            binding.extendedFab.isVisible = !posts.isNullOrEmpty()
            // binding.extendedFab.visibility =
            // if (it.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
        }

        binding.extendedFab.setOnClickListener {
            viewModel.newPostsIsVisible()
            binding.list.smoothScrollToPosition(0)

        }

        binding.swiperefresh.setOnRefreshListener {
            viewModel.toastFun(true)
            viewModel.cleanNewPost()
            viewModel.refreshing()
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
                        // binding.appBarLayout.visibility = View.GONE

                        (requireActivity() as AppActivity).supportActionBar?.hide()
                        binding.fab.visibility = View.INVISIBLE
                        binding.extendedFab.isVisible = false

                    } else { // Пользователь прокручивает вверх
                        // binding.appBarLayout.visibility = View.VISIBLE

                        (requireActivity() as AppActivity).supportActionBar?.show()
                        binding.fab.visibility = View.VISIBLE
                        binding.extendedFab.isVisible =
                            !viewModel.newPostData.value.isNullOrEmpty()
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
                    (requireActivity() as AppActivity).apply {
                        supportActionBar?.setBackgroundDrawable(colorSetter(R.color.black))
                        changeStatusBarColor(getString(R.color.black))
                        supportActionBar?.hide()
                    }
                }

                R.id.application_login_fragment -> {
                    (requireActivity() as AppActivity).apply {
                        changeStatusBarColor(getString(R.color.stormy_sky))
                        supportActionBar?.hide()
                        // windowInsetsController().hide(WindowInsetsCompat.Type.statusBars())
                    }

                }
                else -> {
                    (requireActivity() as AppActivity).apply {
                        supportActionBar?.setBackgroundDrawable(colorSetter(R.color.colorPrimary))
                        changeStatusBarColor(getString(R.color.colorPrimaryDark))
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

        dialogBuilder.setMessage(
            phrase +
                "" +
                " Хотите войти?")
            .setCancelable(false) // Если установить значение false, то пользователь
//          не сможет закрыть диалоговое окно, например, нажатием в любой точке экрана.
//          В таком случае пользователь должен нажать одну из предоставленных опций.

            .setPositiveButton(getString(R.string.proceed)) { dialog, _ ->
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
