///
///Author: YoungChan
///LastEditors: YoungChan
///Description: file content
///Date: 2019-04-28 15:21:03
///LastEditTime: 2019-11-23 18:23:51
///
import 'package:flutter/material.dart';

import 'package:nc_image_picker/nc_image_picker.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Builder(
          builder: (context) {
            return Center(
                child: Column(
              mainAxisSize: MainAxisSize.min,
              children: <Widget>[
                RaisedButton(
                  child: Text('Pick Images'),
                  onPressed: () {
                    NCImagePicker.pickImages(maxCount: 2, enableCamera: true)
                        .then((results) {
                      print("PickImage: $results");
                    }).catchError((err) {
                      Scaffold.of(context).showSnackBar(SnackBar(
                        content: Text(err.toString()),
                      ));
                    });
                  },
                ),
                RaisedButton(
                  child: Text('Take Picture'),
                  onPressed: () {
                    NCImagePicker.takePicture().then((results) {
                      print("PickImage: $results");
                    }).catchError((err) {
                      Scaffold.of(context).showSnackBar(SnackBar(
                        content: Text(err.toString()),
                      ));
                    });
                  },
                ),
              ],
            ));
          },
        ),
      ),
    );
  }
}
