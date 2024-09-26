package com.chertovich.photos.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.chertovich.photos.data.RegData
import com.chertovich.photos.databinding.FragmentRegisterBinding
import com.chertovich.photos.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSingUp.setOnClickListener {
            val login =  binding.editTextLogin.text.toString()
            val password = binding.editTextPassword.text.toString()
            val passwordConfirm = binding.editTextPasswordConfirm.text.toString()
            viewModel.register(login, password, passwordConfirm)
        }

        viewModel.clearRegDataLiveData.observe(viewLifecycleOwner) {
            binding.editTextLogin.text.clear()
            binding.editTextPassword.text.clear()
            binding.editTextPasswordConfirm.text.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}