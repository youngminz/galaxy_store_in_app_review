import 'package:flutter/material.dart';
import 'package:galaxy_store_in_app_review/galaxy_store_in_app_review.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _packageNameController = TextEditingController();
  bool _isAvailable = false;
  String _debugMessage = "";

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('GalaxyStore Inapp Review Sample'),
        ),
        body: Padding(
          padding: const EdgeInsets.all(15),
          child: Column(
            children: [
              Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: _packageNameController,
                      decoration: const InputDecoration(
                        labelText: "Input your package name",
                      ),
                    ),
                  ),
                  ElevatedButton(
                    onPressed: () async {
                      var targetPackage = _packageNameController.text.isEmpty
                          ? null
                          : _packageNameController.text;
                      await GalaxyStoreInAppReview.openStoreListing(
                          targetPackage: targetPackage);
                    },
                    child: const Text("OPEN STORE"),
                  )
                ],
              ),
              ElevatedButton(
                onPressed: () async {
                  var targetPackage = _packageNameController.text.isEmpty
                      ? null
                      : _packageNameController.text;

                  setState(() {
                    _debugMessage =
                        "Checking availability - ${targetPackage ?? "(Current app)"}";
                  });

                  var isAvailable = await GalaxyStoreInAppReview.isAvailable(
                      targetPackage: targetPackage);

                  setState(() {
                    _debugMessage =
                        "${targetPackage ?? "(Current app)"} is ${isAvailable ? "" : "not "}available for review.";
                    _isAvailable = isAvailable;
                  });
                },
                child: const Text("CHECK REVIEW AUTHORITY"),
              ),
              ElevatedButton(
                onPressed: _isAvailable
                    ? () async {
                        await GalaxyStoreInAppReview.requestReview();
                      }
                    : null,
                child: const Text("OPEN GALAXYSTORE REVIEW POPUP"),
              ),
              Text(_debugMessage)
            ],
          ),
        ),
      ),
    );
  }
}
