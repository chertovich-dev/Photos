package com.chertovich.photos.view.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.chertovich.photos.data.Photo
import com.chertovich.photos.data.PhotoState
import com.chertovich.photos.databinding.FragmentPhotosBinding
import com.chertovich.photos.view.adapters.OnPhotosRecyclerListener
import com.chertovich.photos.view.adapters.PhotosRecyclerAdapter
import com.chertovich.photos.view.log
import com.chertovich.photos.viewmodel.MainViewModel

private const val COL_COUNT = 3

class PhotosFragment : Fragment(), OnPhotosRecyclerListener {
    private var _binding: FragmentPhotosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadPhotos()

        binding.recyclerView.layoutManager = GridLayoutManager(context, COL_COUNT)

        viewModel.photosLiveData.observe(viewLifecycleOwner) { photos ->
            binding.recyclerView.adapter = PhotosRecyclerAdapter(photos, this)
        }

        viewModel.refreshPhotosLiveData.observe(viewLifecycleOwner) { photos ->
            for ((index, photo) in photos.withIndex()) {
                if (photo.state == PhotoState.REFRESH) {
                    binding.recyclerView.adapter?.notifyItemChanged(index)
                }
            }
        }

        viewModel.photosChangedLiveData.observe(viewLifecycleOwner) {
            binding.recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSelectPhoto(index: Int) {
        viewModel.showPhoto(index)
    }

    override fun onLoadPhoto(index: Int) {
        viewModel.loadPhoto(index)
    }
}