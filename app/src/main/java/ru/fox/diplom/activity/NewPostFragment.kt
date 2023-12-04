package ru.fox.diplom.activity

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import ru.fox.diplom.R
import ru.fox.diplom.databinding.FragmentNewPostBinding
import ru.fox.diplom.util.AndroidUtils
import ru.fox.diplom.util.StringArg
import ru.fox.diplom.viewmodel.FeedItemViewModel

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: FeedItemViewModel by activityViewModels()

    private val photoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                ImagePicker.RESULT_ERROR -> {
                    val errorRes = ImagePicker.getError(it.data)
                    Toast.makeText(this.activity, errorRes, Toast.LENGTH_SHORT).show()
                }

                Activity.RESULT_OK -> {
                    val uri = it.data?.data
                    viewModel.setPhoto(uri, uri?.toFile())
                }
            }
        }

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

//        viewModel.postCreated.observe(viewLifecycleOwner) {
//            viewModel.loadPosts()
//            findNavController().navigateUp()
//        }

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.save_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.save -> {
                            if (binding.edit.text.toString().isNotBlank()) {
                                viewModel.changeContent(binding.edit.text.toString())
                                viewModel.save()
                                AndroidUtils.hideKeyboard(requireView())
                                true
                            } else {
                                findNavController().navigateUp()
                                false
                            }
                        }

                        else -> false
                    }
            },
            viewLifecycleOwner
        )
        viewModel.photo.observe(viewLifecycleOwner) { photo ->
            if (photo != null) {
                binding.photoContainer.visibility = View.VISIBLE
                binding.photo.setImageURI(photo.uri)
            } else {
                binding.photoContainer.visibility = View.GONE
                return@observe
            }
        }
        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .createIntent(photoLauncher::launch)
            binding.removeAttachment.visibility = View.VISIBLE
        }
        binding.takePhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .cameraOnly()
                .crop()
                .compress(2048)
                .createIntent(photoLauncher::launch)
            binding.removeAttachment.visibility = View.VISIBLE
        }
        binding.removeAttachment.setOnClickListener {
            viewModel.clearPhoto()
            binding.photoContainer.visibility = View.GONE
        }
        return binding.root
    }
}