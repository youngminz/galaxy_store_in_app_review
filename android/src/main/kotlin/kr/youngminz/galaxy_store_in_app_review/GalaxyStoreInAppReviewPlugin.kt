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

class GalaxyStoreInAppReviewPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "isAvailable" -> {
                val targetPackage: String? = call.argument("targetPackage")
                isAvailable(targetPackage, result)
            }
            "requestReview" -> {
                requestReview()
            }
            "openStoreListing" -> {
                val targetPackage: String? = call.argument("targetPackage")
                openStoreListing(targetPackage)
            }
            else -> result.notImplemented()
        }
    }

    // Documentation: https://developer.samsung.com/galaxy-store/customer-review/galaxy-store-review-broadcast.html

    private fun isAvailable(targetPackageFromFlutter: String?, result: Result) {
        val targetPackage = targetPackageFromFlutter ?: applicationContext.packageName

        Log.d(TAG, "Starting availability check for $targetPackage")

        // Your app should be installed in your device to check a review authority
        try {
            applicationContext.packageManager.getPackageInfo(targetPackage, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d(TAG, "$targetPackage is not installed in your device")

            result.success(false)
            return
        }

        // 1. Check whether if the GalaxyStore currently installed in your device has in-app review function
        try {
            val applicationInfo = applicationContext.packageManager.getApplicationInfo("com.sec.android.app.samsungapps", PackageManager.GET_META_DATA)
            val inAppReviewVersion = applicationInfo.metaData.getInt("com.sec.android.app.samsungapps.review.inappReview", 0)
            if (inAppReviewVersion == 0) {
                Log.d(TAG, "GalaxyStore does not support in-app review function. Please update the GalaxyStore to the latest version.")

                result.success(false)
                return
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d(TAG, "GalaxyStore is not installed in your device")
        }

        // 2. Check review authority by using GalaxyStore
        val intent = Intent("com.sec.android.app.samsungapps.REQUEST_INAPP_REVIEW_AUTHORITY")
        intent.setPackage("com.sec.android.app.samsungapps")
        intent.putExtra("callerPackage", applicationContext.packageName)
        applicationContext.sendBroadcast(intent)

        Log.d(TAG, "Checking review authority from GalaxyStore server...")

        // 3. Receive result of review authority checking from GalaxyStore
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.sec.android.app.samsungapps.RESPONSE_INAPP_REVIEW_AUTHORITY")
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                applicationContext.unregisterReceiver(this)
                Log.d(TAG, "Authority checked. Got response")

                // If hasAuthority is true, you have authority to write review.
                // Now you can call WriteReview Activity which is imported in GalaxyStore
                val hasAuthority = intent!!.getBooleanExtra("hasAuthority", false)

                val errorCode = intent.getIntExtra("errorCode", 0)
                if (errorCode > 0) {
                    /* Something went wrong while check the authority
                   1000 : A mandatory parameter to check user status is not available
                   2000 : Server error
                   4002 : Content is not available in Galaxy Store for the user
                   5000 : The user is not logged in to a Samsung Account on the device
                   100015 : Repeated request for authorization happens within 10 seconds  */

                    Log.d(TAG, "hasAuthority : $hasAuthority, errorCode : $errorCode")
                } else {
                    // By using deeplinkUrlForReview, you can open review activity of Galaxy Store
                    deeplinkUri = intent.getStringExtra("deeplinkUri")

                    Log.d(TAG, "hasAuthority : $hasAuthority, deeplinkUri : $deeplinkUri")
                }

                result.success(hasAuthority)
            }
        }
        applicationContext.registerReceiver(broadcastReceiver, intentFilter)
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
        activity.startActivity(intent)
    }

    private fun openStoreListing(targetPackageFromFlutter: String?) {
        val targetPackage = targetPackageFromFlutter ?: applicationContext.packageName

        val intent = Intent()
        intent.data = Uri.parse("samsungapps://ProductDetail/$targetPackage")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        activity.startActivity(intent)
    }

    // Variables, constants and override functions

    private lateinit var channel: MethodChannel
    private lateinit var activity: Activity
    private lateinit var applicationContext: Context
    private var deeplinkUri: String? = null

    companion object {
        const val CHANNEL = "galaxy_store_in_app_review"
        const val TAG = "GalaxyStoreInAppReview"
    }

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, CHANNEL)
        channel.setMethodCallHandler(this)
        applicationContext = flutterPluginBinding.applicationContext
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {

    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {

    }
}
