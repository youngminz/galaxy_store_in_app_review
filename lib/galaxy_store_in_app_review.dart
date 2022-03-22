import 'dart:async';
import 'dart:io' show Platform;

import 'package:flutter/services.dart';

class GalaxyStoreInAppReview {
  static const MethodChannel _channel =
      MethodChannel('galaxy_store_in_app_review');

  /// Checks if the device is able to show a Galaxy Store review dialog.
  static Future<bool> isAvailable(
      {Duration timeout = const Duration(seconds: 10)}) async {
    // Galaxy Store is available only on Android devices.
    if (!Platform.isAndroid) return false;

    // A timeout may occur due to network issues.
    // In this case, a TimeoutException is thrown.
    try {
      return await _channel.invokeMethod("isAvailable").timeout(timeout);
    } on TimeoutException {
      return false;
    }
  }

  /// Attempts to show the review dialog. It's recommended to first check if
  /// the device supports this feature via [isAvailable].
  static Future<void> requestReview() async {
    // Galaxy Store is available only on Android devices.
    if (!Platform.isAndroid) return;

    await _channel.invokeMethod("requestReview");
  }
}
