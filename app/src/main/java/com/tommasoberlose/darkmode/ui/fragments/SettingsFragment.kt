package com.tommasoberlose.darkmode.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.google.android.material.transition.MaterialSharedAxis
import com.tommasoberlose.darkmode.BuildConfig
import com.tommasoberlose.darkmode.ui.activities.MainActivity
import com.tommasoberlose.darkmode.R
import com.tommasoberlose.darkmode.databinding.SettingsFragmentBinding
import com.tommasoberlose.darkmode.ui.viewmodels.MainViewModel
import com.tommasoberlose.darkmode.utils.openURI
import com.tommasoberlose.darkmode.utils.toast
import kotlinx.android.synthetic.main.settings_fragment.*

class SettingsFragment : Fragment(), PurchasesUpdatedListener {

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
        viewModel.billingClient = BillingClient.newBuilder(requireContext()).enablePendingPurchases().setListener(this).build()
        binding = DataBindingUtil.inflate<SettingsFragmentBinding>(inflater, R.layout.settings_fragment, container, false)

        subscribeUi(viewModel)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        action_back.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }

        action_api.setOnClickListener {
            activity?.openURI("https://sunrise-sunset.org/api")
        }

        action_feedback.setOnClickListener {
            activity?.openURI("https://github.com/tommasoberlose/op-dark-mode")
        }

        action_donate.setOnClickListener {
            viewModel.openConnection()
        }

        app_version.text = "v%s (%s)".format(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }

    private fun subscribeUi(viewModel: MainViewModel) {
        viewModel.products.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                viewModel.purchase(requireActivity(), it.first())
            }
        })
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    viewModel.handlePurchase(purchase)
                    activity?.toast(getString(R.string.thanks))
                }
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // DO nothing
            viewModel.closeConnection()
        } else {
            activity?.toast(getString(R.string.error))
            viewModel.closeConnection()
        }
    }

    override fun onDestroy() {
        viewModel.closeConnection()
        super.onDestroy()
    }
}