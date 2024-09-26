package com.chertovich.photos.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.chertovich.photos.R
import com.chertovich.photos.databinding.FragmentAuthBinding
import com.chertovich.photos.view.adapters.AuthFragmentStateAdapter
import com.chertovich.photos.view.adapters.ERROR_WRONG_TAB_INDEX
import com.chertovich.photos.view.adapters.TAB_LOGIN
import com.chertovich.photos.view.adapters.TAB_REGISTER
import com.chertovich.photos.viewmodel.MainViewModel
import com.google.android.material.tabs.TabLayoutMediator

private const val LOGIN_TAB_INDEX = 0

class AuthFragment : Fragment() {
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AuthFragmentStateAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                TAB_LOGIN -> getString(R.string.login)
                TAB_REGISTER -> getString(R.string.register)
                else -> throw Exception(ERROR_WRONG_TAB_INDEX)
            }
        }.attach()

        viewModel.goToLoginLiveData.observe(viewLifecycleOwner) {
            binding.tabLayout.getTabAt(LOGIN_TAB_INDEX)?.select()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}