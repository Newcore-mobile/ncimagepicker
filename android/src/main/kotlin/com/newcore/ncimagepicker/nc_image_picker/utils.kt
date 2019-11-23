package com.newcore.ncimagepicker.nc_image_picker

import android.graphics.BitmapFactory
import android.net.Uri
import android.content.Context
import android.provider.MediaStore
import android.database.Cursor
import android.graphics.Bitmap
import android.text.TextUtils
import java.io.*
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*


fun uriToFilePath(context: Context, uri: Uri?): String? {
    if(uri == null){
        return null
    }
    return if ("file" == uri.scheme) {
        uri.path
    } else {
        filenameFromUri(context, uri)
    }
}

fun filenameFromUri(context: Context, uri: Uri): String? {
    var filePath = getFilePathFromCursor(context, uri)
    if (filePath.isNullOrBlank()) {
        filePath = getFilePathFromInputStream(context, uri)
    }
    return filePath
}

private fun getFilePathFromCursor(context: Context, uri: Uri): String? {
    var filePath: String? = null
    var cursor: Cursor? = null
    try {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        cursor = context.contentResolver.query(uri, filePathColumn, null, null, null)
        cursor!!.moveToFirst()
        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
        filePath = cursor.getString(columnIndex)
        cursor.close()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        cursor?.close()
    }
    return filePath
}

private fun getFilePathFromInputStream(context: Context, uri: Uri): String {
    var filePath = ""
    var inputStream: InputStream? = null
    try {
        inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream, null, getBitMapOptions(context, uri))
        inputStream!!.close()
        filePath = context.externalCacheDir?.absolutePath + md5(uri.toString())
        if(bitmap != null){
            saveBitmap(bitmap, File(filePath))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        if (inputStream != null) {
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
    return filePath
}

fun getBitMapOptions(context: Context, uri: Uri): BitmapFactory.Options {

    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    val stream = context.contentResolver.openInputStream(uri)
    BitmapFactory.decodeStream(stream, null, options)
    stream!!.close()
    var width = options.outWidth
    var height = options.outHeight
    if (width > height) {
        val temp = width
        width = height
        height = temp
    }
    val sampleRatio = Math.max(width / 900, height / 1600)
    options = BitmapFactory.Options()
    options.inSampleSize = sampleRatio
    return options
}

fun saveBitmap(bitmap: Bitmap, file: File): Boolean {
    var fos: FileOutputStream? = null
    try {
        file.createNewFile()
        fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        if (fos != null) {
            try {
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    return true
}
fun saveBitmap(bitmap: Bitmap, fileName: String, path: String): Boolean {
    val file = File(path)
    if (!file.exists()) {
        file.mkdir()
    }
    fileName.isNullOrBlank()
    val imageFile = File(file, fileName)
    return saveBitmap(bitmap, imageFile)
}

fun md5(value: String): String {
    val instance: MessageDigest = MessageDigest.getInstance("MD5")
    //对字符串加密，返回字节数组
    val digest:ByteArray = instance.digest(value.toByteArray())
    var sb : StringBuffer = StringBuffer()
    for (b in digest) {
        //获取低八位有效值
        var i :Int = b.toInt() and 0xff
        //将整数转化为16进制
        var hexString = Integer.toHexString(i)
        if (hexString.length < 2) {
            //如果是一位的话，补0
            hexString = "0" + hexString
        }
        sb.append(hexString)
    }
    return sb.toString()
}
