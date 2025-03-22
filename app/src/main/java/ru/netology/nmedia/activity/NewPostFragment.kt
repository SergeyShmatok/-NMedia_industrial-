package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class NewPostFragment : Fragment() {

    companion object {
        var Bundle.textArg1: String? by StringArg
        const val MAX_PHOTO_SIZE_PX = 2048
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

        arguments?.textArg1
            ?.let(binding.edit::setText)

//--------------------------------------------------------------------------------------------------

        requireActivity().addMenuProvider( // Принимает MenuProvider и LifecycleOwner
            object : MenuProvider {   // интерфейс, который включает всё необходимое для работы с menu
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.new_post, menu)
                // первым параметром передаёт XML меню, вторым объект меню
                // после вызова inflate заполнит данными объект "menu"
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                   if (menuItem.itemId == R.id.newPost) { // проверяем по id, что было 'нажато'
                       viewModel.changeContent(binding.edit.text.toString()) // обработка нажатия
                       viewModel.save() // -/-
                       AndroidUtils.hideKeyboard(requireView()) // -/-
                       true
                   } else
                       false

            }, viewLifecycleOwner) // - если не указать не будут убираться пункты меню (?)

//--------------------------------------------------------------------------------------------------

        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.dataState.observe(viewLifecycleOwner) { stateModel ->
            if (!stateModel.postIsAdded) {
                viewModel.toastFun()
                viewModel.cleanModel()
            }
        }

        viewModel.photo.observe(viewLifecycleOwner) { photo ->
            if (photo == null) {
                binding.previewContainer.isGone = true
                return@observe
            }

            binding.previewContainer.isVisible = true
            binding.preview.setImageURI(photo.uri)
        }
//--------------------------------------------------------------------------------------------------

        val photoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == ImagePicker.RESULT_ERROR) {
                viewModel.toastFun(pickError = true)
                return@registerForActivityResult
            }

         val result = it.data?.data ?: return@registerForActivityResult // если код ответа не ошибка,
             // то (всё равно) что-то должно сохраниться.

             // Метод getData() позволяет получить данные, с которыми работает намерение. Возвращает URI
             // (универсальный идентификатор ресурса) данных, на которые направлено намерение,
             // или значение null.

            viewModel.changePhoto(result, result.toFile())
                // toFile() создаёт файл из указанного Uri. Обратите внимание, что это вызовет исключение
                // IllegalArgumentException при вызове на Uri, в котором отсутствует схема файла.
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .crop()
                .cameraOnly()
                .maxResultSize(MAX_PHOTO_SIZE_PX, MAX_PHOTO_SIZE_PX)
                .createIntent(photoLauncher::launch)
                        // photoLauncher.launch(it)
        }


        binding.pickPhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .crop()
                .galleryOnly()
                .maxResultSize(MAX_PHOTO_SIZE_PX, MAX_PHOTO_SIZE_PX)
                .createIntent(photoLauncher::launch)
        }

        binding.deleteButton.setOnClickListener {
            viewModel.removePhoto()
        }


        return binding.root
    }
}
