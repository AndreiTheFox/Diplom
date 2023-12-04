package ru.fox.diplom.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.fox.diplom.BuildConfig
import ru.fox.diplom.databinding.FragmentImageBinding
import ru.fox.diplom.util.glideDownloadFullImage
@AndroidEntryPoint
class ImageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentImageBinding.inflate(
            inflater,
            container,
            false
        )
        val serverPathUrl = "${BuildConfig.BASE_URL}"
        val attachmentsUrl = "${serverPathUrl}/media"
        val attachmentUrl = arguments?.getString("attachUrl")
        val downloadAttachUrl = "${attachmentsUrl}/${attachmentUrl}"
        glideDownloadFullImage(downloadAttachUrl, binding.imageAttachment)
        binding.navBack.setOnClickListener{
            findNavController().navigateUp()
        }
        return binding.root
    }
}
