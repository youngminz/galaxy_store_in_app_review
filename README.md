# galaxy_store_in_app_review

This is a library that wraps the [In App Review API](https://seller.samsungapps.com/notice/getNoticeDetail.as?csNoticeID=0000005510) provided by the Samsung Galaxy Store for easy use in Flutter.
Much inspired by [britannio/in_app_review](https://github.com/britannio/in_app_review).

![Galaxy Store In App Review](./screenshots/galaxy_store.png)

## Usage

```dart
import 'package:galaxy_store_in_app_review/galaxy_store_in_app_review.dart';

if (await GalaxyStoreInAppReview.isAvailable()) {
	await GalaxyStoreInAppReview.requestReview();
}
```

## Disclaimer

This is an early project. Please let me know if there is a problem.
