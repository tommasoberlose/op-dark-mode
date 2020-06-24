package com.tommasoberlose.darkmode.ui.viewmodels

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import com.chibatching.kotpref.livedata.asLiveData
import com.tommasoberlose.darkmode.global.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    var isSecurePermissionGranted: MutableLiveData<Boolean> = MutableLiveData(true)

    val isTileAdded = Preferences.asLiveData(Preferences::isTileAdded)
    val automaticMode = Preferences.asLiveData(Preferences::automaticMode)

    // Custom time range
    val startTime = Preferences.asLiveData(Preferences::startTime)
    val endTime = Preferences.asLiveData(Preferences::endTime)

    // Sunset and sunrise
    val sunriseTime = Preferences.asLiveData(Preferences::sunriseTime)
    val sunsetTime = Preferences.asLiveData(Preferences::sunsetTime)
    val location = Preferences.asLiveData(Preferences::location)

    // Billing
    lateinit var billingClient: BillingClient
    val products: MutableLiveData<List<SkuDetails>> = MutableLiveData(emptyList())

    fun openConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    val params = SkuDetailsParams.newBuilder()
                    params.setSkusList(listOf("coffee")).setType(
                        BillingClient.SkuType.INAPP)
                    viewModelScope.launch(Dispatchers.IO) {
                        val skuDetailsList = billingClient.querySkuDetails(params.build()).skuDetailsList
                        withContext(Dispatchers.Main) {
                            products.value = skuDetailsList
                        }
                    }
                }
            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    fun purchase(activity: Activity, product: SkuDetails) {
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(product)
            .build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    fun handlePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            viewModelScope.launch(Dispatchers.IO) {
                val token = purchase.purchaseToken
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(token)
                billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())

                val consumeParams =
                    ConsumeParams.newBuilder()
                        .setPurchaseToken(token)
                        .build()
                billingClient.consumePurchase(consumeParams)
                closeConnection()
            }
        }
    }

    fun closeConnection() {
        billingClient.endConnection()
    }
}