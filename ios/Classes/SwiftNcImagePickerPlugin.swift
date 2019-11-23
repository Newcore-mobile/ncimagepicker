import Flutter
import UIKit
import Photos
import GalleryFork


public class SwiftNcImagePickerPlugin: NSObject, FlutterPlugin, GalleryControllerDelegate {
    enum FlutterCmd {
        case takePicture
        case pickImages
        case none
    }

    var gallery: GalleryController!
    var controller: UIViewController!
    var imageResult: FlutterResult?
    var flutterCmd: FlutterCmd = FlutterCmd.none

    init(contro: UIViewController) {
        self.controller = contro
        super.init()
    }

    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "nc_image_picker", binaryMessenger: registrar.messenger())
        let controller = UIApplication.shared.delegate!.window!!.rootViewController!
        let instance = SwiftNcImagePickerPlugin.init(contro: controller)
        registrar.addMethodCallDelegate(instance, channel: channel)
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        imageResult = result
        if(call.method == "pickImages") {
            let args = call.arguments as? Dictionary<String, Any>
            let maxCount = args?["maxCount"] as? Int ?? 8
            let enableCamera = args?["enableCamera"] as? Bool ?? false
            startGallery(maxCount: maxCount, enableCamera: enableCamera)
        } else if(call.method == "takePicture") {
            takePicture()
        } else {
            result(nil)
        }
    }

    private func takePicture() {
        flutterCmd = FlutterCmd.takePicture
        gallery = GalleryController()
        gallery.delegate = self
        Config.tabsToShow = [.cameraTab]
        Config.Camera.imageLimit = 1
        controller.present(gallery, animated: true, completion: nil)
    }

    private func startGallery(maxCount: Int, enableCamera: Bool) {
        flutterCmd = FlutterCmd.pickImages
        gallery = GalleryController()
        gallery.delegate = self
        if(enableCamera) {
            Config.tabsToShow = [.cameraTab, .imageTab]
        } else {
            Config.tabsToShow = [.imageTab]
        }

        Config.Camera.imageLimit = maxCount
        controller.present(gallery, animated: true, completion: nil)
    }

    public func galleryControllerDidCancel(_ controller: GalleryController) {
        controller.dismiss(animated: true, completion: nil)
        gallery = nil
    }

    public func galleryController(_ controller: GalleryController, didSelectVideo video: Video) {
        controller.dismiss(animated: true, completion: nil)
        gallery = nil

    }

    public func galleryController(_ controller: GalleryController, didSelectImages images: [Image]) {
        controller.dismiss(animated: true, completion: nil)
        gallery = nil
        var imageDic = [[String: Any]]()
        if(!images.isEmpty) {
            let tmpPath = NSTemporaryDirectory()
            Image.resolve(images: images, completion: { [weak self](resolvedImages) in
                for (index, resolvedImage) in resolvedImages.enumerated() {
                    let imageAsset = images[index].asset
                    let imagePath = tmpPath + ProcessInfo().globallyUniqueString + ".jpg"
                    let imgData = resolvedImage?.jpegData(compressionQuality: 1.0)
                    if(FileManager.default.createFile(atPath: imagePath, contents: imgData, attributes: nil)) {
                        print("Saved image: " + imagePath)
                        let resource = PHAssetResource.assetResources(for: imageAsset)[0]

                        var map = [String: Any]()
                        map["localPath"] = imagePath
                        map["name"] = resource.originalFilename
                        map["size"] = imgData!.count
                        map["mimeType"] = "image/jpeg"

                        imageDic.append(map)
                    } else {
                        print("Saved image failed!")
                    }
                }
                print("Picked images: \(imageDic)")

                if(self?.imageResult != nil) {
                    if(imageDic.isEmpty) {
                        self!.imageResult!(nil)
                    } else {
                        if(self?.flutterCmd == FlutterCmd.takePicture) {
                            self!.imageResult!(imageDic[0])
                        } else if(self?.flutterCmd == FlutterCmd.pickImages) {
                            self!.imageResult!(imageDic)
                        } else {
                            self!.imageResult!(nil)
                        }
                    }
                }
            })
        }
    }

    public func galleryController(_ controller: GalleryController, requestLightbox images: [Image]) {
        controller.dismiss(animated: true, completion: nil)
        gallery = nil
    }
}
