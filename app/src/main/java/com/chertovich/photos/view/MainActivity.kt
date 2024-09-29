package com.chertovich.photos.view

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.chertovich.photos.FIRST_INDEX
import com.chertovich.photos.R
import com.chertovich.photos.databinding.ActivityMainBinding
import com.chertovich.photos.dateToServerDate
import com.chertovich.photos.view.fragments.DeleteDialogFragment
import com.chertovich.photos.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.Date

private const val FILE_NAME = "photo.jpg"

fun log(text: String) {
    Log.i("_photos", text)
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    private var uri: Uri? = null

    private fun sendMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private val photoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
        if (result) {
            uri?.let { uri ->
                viewModel.savePhoto(uri)
            }
        }
    }

    private fun takePhoto() {
        try {
            val file = File(filesDir, FILE_NAME)
            uri = FileProvider.getUriForFile(this, getString(R.string.authorities), file)
            photoLauncher.launch(uri)
        } catch (e: Exception) {
            sendMessage(getString(R.string.cannot_make_photo))
        }
    }

    private val requestLocationPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) {
        requestLocationPermissions()
    }

    private fun requestLocationPermissions() {
        requestLocationPermissionsLauncher.launch(
            getLocationPermissions()
        )
    }

    private fun getLocationPermissions(): Array<String> {
        return arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_photos, R.id.nav_map), drawerLayout)

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        viewModel.authorizedLiveData.observe(this) { userData ->
            val authorized = userData != null

            if (authorized) {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                binding.appBarMain.fab.visibility = View.VISIBLE

                val textView = binding.navView.getHeaderView(FIRST_INDEX).findViewById<TextView>(R.id.textViewTitle)
                textView?.text = userData?.login

                requestLocationPermissions()
            } else {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                binding.appBarMain.fab.visibility = View.INVISIBLE
            }

            supportActionBar?.setDisplayHomeAsUpEnabled(authorized)
        }

        viewModel.messageLiveData.observe(this) { message ->
            sendMessage(message)
        }

        viewModel.navLiveData.observe(this) { nav ->
            navController.navigate(nav.action)
        }

        viewModel.deleteDialogLiveData.observe(this) { index ->
            val deleteDialogFragment: DeleteDialogFragment = DeleteDialogFragment.newInstance(index)
            deleteDialogFragment.show(supportFragmentManager, null)
        }

        binding.appBarMain.fab.setOnClickListener {
            takePhoto()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}