package com.chertovich.photos.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.chertovich.photos.databinding.FragmentPhotoBinding
import com.chertovich.photos.serverDateToDate
import com.chertovich.photos.setPhotoToImageView
import com.chertovich.photos.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date

private const val format = "dd.MM.yyyy HH:mm"

class PhotoFragment : Fragment() {
    private var _binding: FragmentPhotoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels<MainViewModel>()

    private val dateFormat = SimpleDateFormat(format)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.photoLiveData.observe(viewLifecycleOwner) { photo ->
            setPhotoToImageView(binding.imageView, photo)
            val date = Date(serverDateToDate(photo.image.date))
            binding.textView.text = dateFormat.format(date)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}