package com.mikmy.emberdepth.monetize

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.mikmy.emberdepth.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AdManager {

    private const val TAG = "AdManager"
    private var rewardedAd: RewardedAd? = null

    private val _adReady = MutableStateFlow(false)
    val adReady: StateFlow<Boolean> = _adReady

    var adFree: Boolean = false

    private var sdkStarted = false

    /**
     * Asks for EEA/UK/Swiss ad consent (Google's User Messaging Platform) when it
     * is required, and only then starts the Mobile Ads SDK and loads the first ad.
     * No ad is requested until consent allows it.
     */
    fun start(activity: Activity) {
        val consent = UserMessagingPlatform.getConsentInformation(activity)
        val begin = {
            if (consent.canRequestAds() && !sdkStarted) {
                sdkStarted = true
                MobileAds.initialize(activity.applicationContext)
                loadAd(activity)
            }
        }
        consent.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            { UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { begin() } },
            { error ->
                Log.w(TAG, "Consent info update failed: ${error.message}")
                begin()
            },
        )
        // Consent given in an earlier session lets ads start without waiting.
        begin()
    }

    /** True when the player must be offered a way to change their ad-consent choice. */
    fun privacyOptionsRequired(activity: Activity): Boolean =
        UserMessagingPlatform.getConsentInformation(activity).privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /** Reopens Google's consent form so the player can change their answer. */
    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            if (error != null) Log.w(TAG, "Privacy options form: ${error.message}")
        }
    }

    fun loadAd(activity: Activity) {
        if (adFree) return
        if (!sdkStarted) return
        if (rewardedAd != null) return

        val request = AdRequest.Builder().build()
        RewardedAd.load(activity, BuildConfig.AD_REWARDED_ID, request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    _adReady.value = true
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            rewardedAd = null
                            _adReady.value = false
                            loadAd(activity)
                        }
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            rewardedAd = null
                            _adReady.value = false
                            loadAd(activity)
                        }
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Ad failed to load: ${error.message}")
                    rewardedAd = null
                    _adReady.value = false
                }
            }
        )
    }

    fun showAd(activity: Activity, onReward: () -> Unit, onDismiss: () -> Unit = {}) {
        if (adFree) {
            onReward()
            return
        }
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    _adReady.value = false
                    loadAd(activity)
                    onDismiss()
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    rewardedAd = null
                    _adReady.value = false
                    loadAd(activity)
                    onDismiss()
                }
            }
            ad.show(activity) { onReward() }
        } else {
            onDismiss()
        }
    }
}
