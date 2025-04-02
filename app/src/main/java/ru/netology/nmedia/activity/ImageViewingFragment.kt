package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            (requireActivity() as AppActivity).transparentAppBar(false)
            findNavController().navigateUp()
        }


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



        // Настройка анимации при клике
        binding.apply {

            (requireActivity() as AppActivity).apply {

                windowInsetsController().systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

                ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, windowInsets ->
                    if (windowInsets.isVisible(WindowInsetsCompat.Type.statusBars()))
                    {
                        binding.root.setOnClickListener {
                            backForPicture.visibility = View.INVISIBLE
                            oneOfOne.visibility = View.INVISIBLE
                            windowInsetsController().hide(WindowInsetsCompat.Type.statusBars()) }

                    } else {
                        binding.root.setOnClickListener {
                            backForPicture.visibility = View.VISIBLE
                            oneOfOne.visibility = View.VISIBLE
                            windowInsetsController().show(WindowInsetsCompat.Type.statusBars()) }
                    }

                    ViewCompat.onApplyWindowInsets(view, windowInsets)
                }


            }

        }

        return binding.root
    }


}


//           - Настройка для кнопки "назад" в AppBar -

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//            // setDisplayHomeAsUpEnabled(true)
//            // setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
//            // title = resources.getString(R.string._1_of_1)
//
//        }
//    }
//




