package com.chertovich.photos.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.chertovich.photos.databinding.FragmentMapBinding
import com.chertovich.photos.viewmodel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

private const val ZOOM = 15f

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(com.chertovich.photos.R.id.fragmentMap)
                as SupportMapFragment?

        mapFragment?.getMapAsync(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        viewModel.photosLiveData.observe(viewLifecycleOwner) { photos ->
            var firstLatLng: LatLng? = null

            for (photo in photos) {
                val lat = photo.image.lat
                val lng = photo.image.lng

                if (lat != 0.0 && lng != 0.0) {
                    val latLng = LatLng(lat, lng)

                    if (firstLatLng == null) {
                        firstLatLng = latLng
                    }

                    googleMap.addMarker(
                        MarkerOptions()
                        .position(latLng))
                }
            }

            if (firstLatLng != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, ZOOM))
            }
        }
    }
}