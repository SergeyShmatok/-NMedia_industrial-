package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.adapter.ATTACHMENTS_URL
import ru.netology.nmedia.databinding.FragmentPictureViewingBinding
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.util.loadAttachments

class ImageViewingFragment : Fragment() {

    // private val viewModel: PostViewModel by activityViewModels()

    companion object {
        var Bundle.textArg2: String? by StringArg
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // (requireActivity() as AppActivity).transparentStatusBar(true)

        val binding = FragmentPictureViewingBinding.inflate(
            inflater,
            container,
            false
        )

        val url = arguments?.textArg2

        binding.viewImage.loadAttachments("$ATTACHMENTS_URL${url}")

        binding.backForPicture.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }


}

