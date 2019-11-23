# nc_image_picker

> Flutter 图片选择器，同时支持Android & iOS.
>
> Android 图片选择使用的是[FishBun](https://github.com/sangcomz/FishBun)
>
> iOS 图片选择使用的是[Gallery](https://github.com/hyperoslo/Gallery)

## Example

选择图片

```dart
///maxCount: 最多选择的图片数
///enableCamera: 是否同时支持拍照
NCImagePicker.pickImages(maxCount: 2, enableCamera: true)
    .then((results) {
    print("PickImage: $results");
}).catchError((err) {
    Scaffold.of(context).showSnackBar(SnackBar(
        content: Text(err.toString()),
    ));
});
```

拍照

```dart
NCImagePicker.takePicture().then((results) {
    print("PickImage: $results");
}).catchError((err) {
    Scaffold.of(context).showSnackBar(SnackBar(
        content: Text(err.toString()),
    ));
});
```



# License

```
Licensed under the MIT License

Copyright (c) 2019 YoungChan

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

```

