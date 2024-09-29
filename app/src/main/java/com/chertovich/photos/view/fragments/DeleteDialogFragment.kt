package com.chertovich.photos.view.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.chertovich.photos.R
import com.chertovich.photos.WRONG_INDEX
import com.chertovich.photos.viewmodel.MainViewModel

class DeleteDialogFragment : DialogFragment() {
    private val viewModel: MainViewModel by activityViewModels<MainViewModel>()

    private var index = WRONG_INDEX

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arguments?.let {
            index = it.getInt(KEY_INDEX, WRONG_INDEX)
        }

        return AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.delete_photo))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                if (index != WRONG_INDEX) {
                    viewModel.deletePhoto(index)
                }
            }
            .setNegativeButton(getString(R.string.no), { _, _ ->})
            .create()
    }

    companion object {
        private const val KEY_INDEX = "index"

        fun newInstance(index: Int): DeleteDialogFragment {
            val args = Bundle()
            args.putInt(KEY_INDEX, index)

            val fragment = DeleteDialogFragment()
            fragment.arguments = args

            return fragment
        }
    }
}