package com.mikmy.emberdepth.monetize

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object BillingManager {

    private const val TAG = "BillingManager"
    private const val PRODUCT_REMOVE_ADS = "remove_ads"

    private var billingClient: BillingClient? = null

    private val _isAdFree = MutableStateFlow(false)
    val isAdFree: StateFlow<Boolean> = _isAdFree

    private val _removeAdsPrice = MutableStateFlow<String?>(null)
    val removeAdsPrice: StateFlow<String?> = _removeAdsPrice

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    fun init(context: Context) {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                    queryProductDetails()
                }
            }
            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
            }
        })
    }

    private fun queryPurchases() {
        val client = billingClient ?: return
        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasRemoveAds = purchases.any { purchase ->
                    purchase.products.contains(PRODUCT_REMOVE_ADS) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                _isAdFree.value = hasRemoveAds
                AdManager.adFree = hasRemoveAds
            }
        }
    }

    private fun queryProductDetails() {
        val client = billingClient ?: return
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRODUCT_REMOVE_ADS)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        client.queryProductDetailsAsync(params) { _, result ->
            val details = result.productDetailsList.firstOrNull()
            _removeAdsPrice.value = details?.oneTimePurchaseOfferDetails?.formattedPrice
        }
    }

    fun launchPurchase(activity: Activity) {
        val client = billingClient ?: return
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRODUCT_REMOVE_ADS)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        client.queryProductDetailsAsync(params) { _, result ->
            val details = result.productDetailsList.firstOrNull() ?: return@queryProductDetailsAsync
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(details)
                            .build()
                    )
                )
                .build()
            client.launchBillingFlow(activity, flowParams)
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.products.contains(PRODUCT_REMOVE_ADS) &&
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        ) {
            _isAdFree.value = true
            AdManager.adFree = true

            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient?.acknowledgePurchase(params) { result ->
                    if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                        Log.w(TAG, "Failed to acknowledge: ${result.debugMessage}")
                    }
                }
            }
        }
    }
}
