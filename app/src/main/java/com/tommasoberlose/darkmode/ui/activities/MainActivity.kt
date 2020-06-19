package com.tommasoberlose.darkmode.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.tommasoberlose.darkmode.R
import com.tommasoberlose.darkmode.ui.viewmodels.MainViewModel
import com.tommasoberlose.darkmode.utils.isDarkTheme

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private val mainNavController: NavController? by lazy {
        Navigation.findNavController(
            this,
            R.id.content_fragment
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    }
}