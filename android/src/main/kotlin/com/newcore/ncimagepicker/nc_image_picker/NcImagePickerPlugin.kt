package com.newcore.ncimagepicker.nc_image_picker

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import com.sangcomz.fishbun.define.Define
import android.app.AlertDialog
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class NcImagePickerPlugin(private val activity: Activity) : MethodCallHandler, PluginRegistry.ActivityResultListener,
        PluginRegistry.RequestPermissionsResultListener {
    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "nc_image_picker")
            val instance = NcImagePickerPlugin(registrar.activity())
            registrar.addActivityResultListener(instance)
            registrar.addRequestPermissionsResultListener(instance)
            channel.setMethodCallHandler(instance)
        }
    }

    private val REQUEST_CODE_GRANT_PERMISSIONS_PICK_IMAGE = 5001
    private val REQUEST_CODE_GRANT_PERMISSIONS_TAKE_PHOTO = 5004
    private val REQUEST_PICK = 5002
    private val TAKE_PICTURE_REQUEST = 5003

    private var pendingResult: Result? = null
    private var fileImagePath: File? = null
    private var maxCount: Int = 8
    private var enableCamera: Boolean = false

    override fun onMethodCall(call: MethodCall, result: Result) {
        pendingResult = result
        if (call.method == "pickImages") {
            val arguments = call.arguments as? Map<String, Any>?
            maxCount = arguments?.get("maxCount") as? Int? ?: 8
            enableCamera = arguments?.get("enableCamera") as? Boolean? ?: false

            //选取相册
            if (checkPermission(REQUEST_CODE_GRANT_PERMISSIONS_PICK_IMAGE)) {
                startGallery(maxCount, enableCamera)
            }
        } else if (call.method == "takePicture") {
            if (checkPermission(REQUEST_CODE_GRANT_PERMISSIONS_TAKE_PHOTO)) {
                //拍照
                takePicture(activity)
            }
        } else {
            result.notImplemented()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == REQUEST_PICK) {
            if (resultCode == Activity.RESULT_OK) {
                val photos = data?.getParcelableArrayListExtra<Uri>(Define.INTENT_PATH)
                if (photos?.isNotEmpty() == true) {
                    val result = ArrayList<HashMap<String, Any>>(photos.size)
                    for (uri in photos) {
                        val map = HashMap<String, Any>()
                        val f = File(uriToFilePath(activity, uri))
                        map["localPath"] = f.absolutePath
                        map["name"] = f.name
                        map["size"] = f.length()
                        map["mimeType"] = when (f.extension) {
                            "jpg" -> "image/jpeg"
                            "png" -> "image/png"
                            else -> "image/*"
                        }
                        result.add(map)
                    }
                    pendingResult?.success(result)
                } else {
                    pendingResult?.error("未选择任何图片", null, null)
                }
            } else {
                pendingResult?.error("未选择任何图片", null, null)
            }
            return true
        } else if (requestCode == TAKE_PICTURE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (fileImagePath != null) {
                    val map = HashMap<String, Any>()
                    map["localPath"] = fileImagePath!!.absolutePath
                    map["name"] = fileImagePath!!.name
                    map["size"] = fileImagePath!!.length()
                    map["mimeType"] = when (fileImagePath!!.extension) {
                        "jpg" -> "image/jpeg"
                        "png" -> "image/png"
                        else -> "image/*"
                    }
                    pendingResult?.success(map)
                } else {
                    pendingResult?.error("未选择任何图片", null, null)
                }
            } else {
                pendingResult?.error("未选择任何图片", null, null)
            }
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?): Boolean {
        if ((requestCode == REQUEST_CODE_GRANT_PERMISSIONS_TAKE_PHOTO || requestCode == REQUEST_CODE_GRANT_PERMISSIONS_PICK_IMAGE) && permissions?.size == 3) {
            if (grantResults!![0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                if (requestCode == REQUEST_CODE_GRANT_PERMISSIONS_PICK_IMAGE) {
                    startGallery(maxCount, enableCamera)
                } else if (requestCode == REQUEST_CODE_GRANT_PERMISSIONS_TAKE_PHOTO) {
                    takePicture(activity)
                }
            } else {
                AlertDialog.Builder(activity)
                        .setTitle("提示")
                        .setMessage("App需要一些权限，请前往设置里授予该权限")
                        .setPositiveButton("设置") { _, _ ->
                            val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            i.data = Uri.fromParts("package", activity.packageName, null)
                            activity.startActivityForResult(i, REQUEST_CODE_GRANT_PERMISSIONS_PICK_IMAGE)
                        }.show()
                return false
            }

            return true
        }
        pendingResult?.error("PERMISSION_DENIED", "为了App正常运行，请授予存储和摄像头使用权限", null)
        return false
    }

    private fun checkPermission(requestCode:Int): Boolean {
        return if (ContextCompat.checkSelfPermission(this.activity,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this.activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this.activity,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                    requestCode)
            false
        } else {
            true
        }
    }

    private fun startGallery(maxCount: Int, enableCamera: Boolean) {

        FishBun.with(activity)
                .setImageAdapter(GlideAdapter())
                .setPickerSpanCount(4)
                .setMaxCount(maxCount)
                .setActionBarColor(Color.parseColor("#ffffff"), Color.parseColor("#ffffff"), true)
                .setActionBarTitleColor(Color.parseColor("#000000"))
                .setAlbumSpanCount(1, 2)
                .setButtonInAlbumActivity(true)
                .setCamera(enableCamera)
                .exceptGif(true)
                .isStartInAllView(true)
                .setReachLimitAutomaticClose(false)
                .setRequestCode(REQUEST_PICK)
                .setHomeAsUpIndicatorDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_arrow_back_black_24dp))
                .setDoneButtonDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_check_black_24dp))
                .setAllViewTitle("所有图片")
                .setActionBarTitle("选择图片")
                .textOnImagesSelectionLimitReached("选择图片已达到最大数量")
                .textOnNothingSelected("还没有任何图片")
                .startAlbum()


    }

    private fun takePicture(context: Activity) {

        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (captureIntent.resolveActivity(context.packageManager) != null) {
            val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA)
            val filename = "${dateFormat.format(Date(System.currentTimeMillis()))}.jpg"
            fileImagePath = File(context.externalCacheDir, filename)
            val mImagePath = fileImagePath!!.absolutePath
            captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImagePath))
            } else {
                val contentValues = ContentValues(1)
                contentValues.put(MediaStore.Images.Media.DATA, mImagePath)
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

            }
            context.startActivityForResult(captureIntent, TAKE_PICTURE_REQUEST)
        } else {
            Toast.makeText(context, "设备相机不可用", Toast.LENGTH_SHORT).show()
        }
    }

}
