
import 'dart:async';

import 'package:flutter/services.dart';

class GalaxyStoreInAppReview {
  static const MethodChannel _channel = MethodChannel('galaxy_store_in_app_review');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}