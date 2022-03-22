import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:galaxy_store_in_app_review/galaxy_store_in_app_review.dart';

void main() {
  const MethodChannel channel = MethodChannel('galaxy_store_in_app_review');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await GalaxyStoreInAppReview.platformVersion, '42');
  });
}
