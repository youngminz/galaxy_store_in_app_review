# galaxy_store_in_app_review

[![pub package](https://img.shields.io/pub/v/galaxy_store_in_app_review.svg)](https://pub.dartlang.org/packages/galaxy_store_in_app_review)

This is a library that wraps the [Galaxy Store Review Broadcast](https://developer.samsung.com/galaxy-store/customer-review/galaxy-store-review-broadcast.html) provided by the Samsung Galaxy Store for easy use in Flutter.
Much inspired by [britannio/in_app_review](https://github.com/britannio/in_app_review).

![Galaxy Store In App Review](https://raw.githubusercontent.com/youngminz/galaxy_store_in_app_review/main/screenshots/galaxy_store.png)

# Android App Configuration

Due to package visibility changes in Android 11 (API level 30) and above, apps can no longer query package information from other installed apps by default. To enable the Galaxy Store in-app review functionality, you must explicitly declare the Galaxy Store package in your AndroidManifest.xml.

Add the following configuration to your `AndroidManifest.xml`:

```xml
<manifest>
  <queries>
    <package android:name="com.sec.android.app.samsungapps" />
  </queries>
</manifest>
```

# Usage

## `requestReview()`

```dart
import 'package:galaxy_store_in_app_review/galaxy_store_in_app_review.dart';

if (await GalaxyStoreInAppReview.isAvailable()) {
    GalaxyStoreInAppReview.requestReview();
}
```

## `openStoreListing()`

```dart
import 'package:galaxy_store_in_app_review/galaxy_store_in_app_review.dart';

GalaxyStoreInAppReview.openStoreListing();
```

# Disclaimer

This is an early project. Please let me know if there is a problem.
