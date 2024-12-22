package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class NewPostFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )

        arguments?.textArg
            ?.let(binding.edit::setText)

        binding.ok.setOnClickListener {
            viewModel.changeContent(binding.edit.text.toString())
            viewModel.save()
            if (viewModel.data.value?.postIsAdded == false) {
                Toast.makeText(requireActivity(), "Пост не добавился," +
                        " попробуйте позже или обратитесь в поддержку", Toast.LENGTH_LONG).show()
            }
            AndroidUtils.hideKeyboard(requireView())
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            //Thread.sleep(200)
            viewModel.loadPosts()
            findNavController().navigateUp()
        }

        return binding.root
    }
}
