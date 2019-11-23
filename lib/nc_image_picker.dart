///
///Author: YoungChan
///LastEditors: YoungChan
///Description: NCImagePicker plugin
///Date: 2019-04-28 15:18:48
///LastEditTime: 2019-07-31 10:35:29
///
import 'dart:async';

import 'package:flutter/services.dart';

class NCImagePicker {
  static const MethodChannel _channel = const MethodChannel('nc_image_picker');

  ///选择相册
  static Future<List<NCPickedImageModel>> pickImages(
      {int maxCount = 8, bool enableCamera = false}) async {
    var list = <NCPickedImageModel>[];
    var errObj;
    var arg = {'maxCount': maxCount, 'enableCamera': enableCamera};
    var result =
        await _channel.invokeMethod<List>('pickImages', arg).catchError((err) {
      errObj = err;
    });
    if (errObj != null) {
      return Future.error("未选择图片");
    } else {
      print("Pick images result: $result");
      result?.forEach((m) {
        list.add(NCPickedImageModel(
            localPath: m['localPath'],
            name: m['name'],
            size: m['size'],
            mimeType: m['mimeType']));
      });
      return Future.value(list);
    }
  }

  ///拍照
  static Future<NCPickedImageModel> takePicture() async {
    var errObj;
    var result =
        await _channel.invokeMethod<Map>('takePicture').catchError((err) {
      errObj = err;
    });
    if (errObj != null) {
      return Future.error("未选择图片");
    } else {
      if (result != null) {
        print("Take picture result: $result");
        return Future.value(NCPickedImageModel(
            localPath: result['localPath'],
            name: result['name'],
            size: result['size'],
            mimeType: result['mimeType']));
      } else {
        return Future.error("未选择图片");
      }
    }
  }
}

class NCPickedImageModel {
  String localPath;
  String name;
  int size;
  String mimeType;

  NCPickedImageModel({this.localPath, this.name, this.size, this.mimeType});
}
