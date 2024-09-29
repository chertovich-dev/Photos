package com.chertovich.photos.view.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chertovich.photos.view.fragments.LoginFragment
import com.chertovich.photos.view.fragments.RegisterFragment

private const val TAB_COUNT = 2

const val TAB_LOGIN = 0
const val TAB_REGISTER = 1

const val ERROR_WRONG_TAB_INDEX = "Неверный индекс вкладки"

class AuthFragmentStateAdapter(fragment: Fragment) : FragmentStateAdapter(fragment)  {
    override fun getItemCount(): Int {
        return TAB_COUNT
    }

    override fun createFragment(position: Int): Fragment {
       return when (position) {
           TAB_LOGIN -> LoginFragment()
           TAB_REGISTER -> RegisterFragment()
           else -> throw Exception(ERROR_WRONG_TAB_INDEX)
       }
    }
}