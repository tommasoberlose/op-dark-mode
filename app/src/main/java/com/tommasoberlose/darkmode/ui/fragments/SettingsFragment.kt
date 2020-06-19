package com.tommasoberlose.darkmode.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.material.transition.MaterialSharedAxis
import com.tommasoberlose.darkmode.BuildConfig
import com.tommasoberlose.darkmode.ui.activities.MainActivity
import com.tommasoberlose.darkmode.R
import com.tommasoberlose.darkmode.databinding.SettingsFragmentBinding
import com.tommasoberlose.darkmode.ui.viewmodels.MainViewModel
import com.tommasoberlose.darkmode.utils.openURI
import kotlinx.android.synthetic.main.settings_fragment.*

class SettingsFragment : Fragment() {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: SettingsFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        viewModel = ViewModelProvider(activity as MainActivity).get(MainViewModel::class.java)
        binding = DataBindingUtil.inflate<SettingsFragmentBinding>(inflater, R.layout.settings_fragment, container, false)

        subscribeUi(viewModel)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        addListener()

        action_back.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }

        app_version.text = "v%s (%s)".format(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }

    private fun subscribeUi(viewModel: MainViewModel) {

    }

    private fun addListener() {
        action_api.setOnClickListener {
            activity?.openURI("https://sunrise-sunset.org/api")
        }

        action_feedback.setOnClickListener {
            activity?.openURI("")
        }
    }
}