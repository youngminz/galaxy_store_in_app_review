package kr.youngminz.galaxy_store_in_app_review

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GalaxyStoreInAppReviewPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private var channel: MethodChannel? = null
    private var activity: Activity? = null
    private var applicationContext: Context? = null
    private var deeplinkUri: String? = null

    companion object {
        const val CHANNEL = "galaxy_store_in_app_review"
        const val TAG = "GalaxyStoreInAppReview"
    }

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, CHANNEL)
        channel!!.setMethodCallHandler(this)
        applicationContext = flutterPluginBinding.applicationContext
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel!!.setMethodCallHandler(null)
        applicationContext = null
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "isAvailable" -> {
                mainScope.launch {
                    if (!isGalaxyStoreReviewAPISupported()) {
                        Log.d(TAG, "isAvailable: isGalaxyStoreReviewAPISupported() -> false")
                        result.success(false)
                        return@launch
                    }
                    if (!canWriteReview()) {
                        Log.d(TAG, "isAvailable: canWriteReview() -> false")
                        result.success(false)
                        return@launch
                    }
                    Log.d(TAG, "isAvailable: true")
                    result.success(true)
                }
            }
            "requestReview" -> {
                requestReview()
            }
            else -> result.notImplemented()
        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    // Original source code : https://seller.samsungapps.com/notice/getNoticeDetail.as?csNoticeID=0000005510

    private fun isGalaxyStoreReviewAPISupported(): Boolean {
        /** To request the user to write a review,
         * it is necessary to check whether the Galaxy Store of the device supports the Review API.
         * Rating API is supported from Galaxy Store Client 4.5.22.7 or later.
         * */

        val applicationInfo = applicationContext!!.packageManager.getApplicationInfo("com.sec.android.app.samsungapps", PackageManager.GET_META_DATA)
        val inAppReviewVersion = applicationInfo.metaData.getInt("com.sec.android.app.samsungapps.review.inappReview", 0)
        return inAppReviewVersion > 0
    }

    private suspend fun canWriteReview(): Boolean {
        /** After checking the Galaxy Store version,
         * it requires to check whether the user can write a review.
         * Returns true if all of the conditions below are satisfied.
         *
         * - Logged into Samsung Account
         * - App downloaded or updated from Galaxy Store
         * - No review history within the last year
         */

        // 1. Check your review authority by Galaxy Store package
        val intent = Intent("com.sec.android.app.samsungapps.REQUEST_INAPP_REVIEW_AUTHORITY")
        intent.setPackage("com.sec.android.app.samsungapps")
        intent.putExtra("callerPackage", applicationContext!!.packageName)
        applicationContext!!.sendBroadcast(intent)

        // 2. Get result of authority checking from Galaxy Store package
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.sec.android.app.samsungapps.RESPONSE_INAPP_REVIEW_AUTHORITY")

        return suspendCoroutine { continuation ->
            val broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    applicationContext!!.unregisterReceiver(this)

                    // If true, you have authority to write review
                    val hasAuthority = intent!!.getBooleanExtra("hasAuthority", false)

                    // By using deeplinkUrlForReview, you can open review activity of Galaxy Store
                    deeplinkUri = intent.getStringExtra("deeplinkUri")

                    continuation.resume(hasAuthority)
                }
            }

            applicationContext!!.registerReceiver(broadcastReceiver, intentFilter)
        }
    }

    private fun requestReview() {
        /** If the user's status is confirmed through the above process,
         * a review writing screen is provided to the user. */

        if (deeplinkUri == null) {
            Log.e(TAG, "Before calling the requestReview, it is required to call the isAvailable and check if true is returned.")
            return
        }

        val intent = Intent()
        intent.data = Uri.parse(deeplinkUri)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        activity!!.startActivity(intent)
    }
}
