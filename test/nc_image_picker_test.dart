import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:nc_image_picker/nc_image_picker.dart';

void main() {
  const MethodChannel channel = MethodChannel('nc_image_picker');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    // expect(await NcImagePicker.platformVersion, '42');
  });
}
