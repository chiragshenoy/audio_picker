package com.audio_picker.audio_picker

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.io.File.separator
import android.os.Build
import android.provider.MediaStore
import android.provider.DocumentsContract
import android.content.ContentUris
import android.text.TextUtils
import android.annotation.TargetApi
import android.database.Cursor


class AudioPickerPlugin : FlutterPlugin, MethodCallHandler {

    private val PERM_CODE = AudioPickerPlugin::class.java.hashCode() + 50 and 0x0000ffff
    private val permission = READ_EXTERNAL_STORAGE

    companion object {
        private var instance: Registrar? = null
        private val REQUEST_CODE = AudioPickerPlugin::class.java.hashCode() + 43 and 0x0000ffff
        private var result: Result? = null
        private const val TAG = "AudioPicker"

        override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPluginBinding) {
            val channel = MethodChannel(flutterPluginBinding.binaryMessenger, "audio_picker")
            channel.setMethodCallHandler(this)
            instance = registrar

            instance?.addActivityResultListener(object : PluginRegistry.ActivityResultListener {
                override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {

                    if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {

                        Thread(Runnable {
                            if (data != null) {
                                if (data.data != null) {
                                    val uri = data.data
                                    Log.i(TAG, "[SingleFilePick] File URI:" + uri!!.toString())
                                    val fullPath = instance?.context()?.let { getPath(uri, it) }

                                    if (fullPath != null) {
                                        Log.i(TAG, "Absolute file path:$fullPath")
                                        runOnUiThread(fullPath, true)
                                    } else {
                                        runOnUiThread("Failed to retrieve path.", false)
                                    }
                                } else {
                                    runOnUiThread("Unknown activity error, please fill an issue.", false)
                                }
                            } else {
                                runOnUiThread("Unknown activity error, please fill an issue.", false)
                            }
                        }).start()
                        return true

                    } else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {
                        result?.success(null)
                        return true
                    } else if (requestCode == REQUEST_CODE) {
                        result?.error(TAG, "Unknown activity error, please fill an issue.", null)
                    }
                    return false
                }
            })

        }

        private fun runOnUiThread(o: Any?, success: Boolean) {
            instance?.activity()?.runOnUiThread {
                when {
                    success -> result?.success(o as String)
                    o != null -> result?.error(TAG, o as String?, null)
                    else -> result?.notImplemented()
                }
            }
        }

        fun getPath(uri: Uri, context: Context): String? {
            val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
            return when {
                isKitKat -> getForApi19(context, uri)
                "file".equals(uri.scheme!!, ignoreCase = true) -> uri.path
                else -> null
            }
        }

        @TargetApi(19)
        private fun getForApi19(context: Context, uri: Uri): String? {
            Log.e(TAG, "Getting for API 19 or above$uri")
            if (DocumentsContract.isDocumentUri(context, uri)) {
                Log.e(TAG, "Document URI")
                if (isExternalStorageDocument(uri)) {
                    Log.e(TAG, "External Document URI")
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        Log.e(TAG, "Primary External Document URI")
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }
                } else if (isDownloadsDocument(uri)) {
                    Log.e(TAG, "Downloads External Document URI")
                    var id = DocumentsContract.getDocumentId(uri)

                    if (!TextUtils.isEmpty(id)) {
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:".toRegex(), "")
                        }
                        val contentUriPrefixesToTry = arrayOf("content://downloads/public_downloads", "content://downloads/my_downloads", "content://downloads/all_downloads")
                        if (id.contains(":")) {
                            id = id.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                        }
                        for (contentUriPrefix in contentUriPrefixesToTry) {
                            val contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), java.lang.Long.valueOf(id))
                            try {
                                val path = getDataColumn(context, contentUri, null, null)
                                if (path != null) {
                                    return path
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Something went wrong while retrieving document path: $e")
                            }

                        }

                    }
                } else if (isMediaDocument(uri)) {
                    Log.e(TAG, "Media Document URI")
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]

                    var contentUri: Uri? = null
                    if ("audio" == type) {
                        Log.i(TAG, "Audio Media Document URI")
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }

                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])

                    contentUri?.let {
                        return getDataColumn(context, contentUri, selection, selectionArgs)

                    }
                }
            } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {
                Log.e(TAG, "NO DOCUMENT URI - CONTENT: " + uri.path!!)
            } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
                Log.e(TAG, "No DOCUMENT URI - FILE: " + uri.path!!)
                return uri.path
            }
            return null
        }

        private fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        private fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        private fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        private fun getDataColumn(context: Context, uri: Uri, selection: String?,
                                  selectionArgs: Array<String>?): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)
            try {
                cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(index)
                }
            } catch (ex: Exception) {
            } finally {
                cursor?.close()
            }
            return null
        }
    }
    
    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "pick_audio") {
            AudioPickerPlugin.result = result
            openAudioPicker()
        } else {
            result.notImplemented()
        }
    }

    private fun openAudioPicker() {
        val intent: Intent

        if (checkPermission()) {

            intent = Intent(Intent.ACTION_GET_CONTENT)
            val uri = Uri.parse(Environment.getExternalStorageDirectory().path + separator)
            intent.setDataAndType(uri, "audio/*")
            intent.type = "audio/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            instance?.activity()?.let {
                if (intent.resolveActivity(it.packageManager) != null) {
                    instance?.activity()?.startActivityForResult(intent, REQUEST_CODE)
                } else {
                    Log.e(TAG, "Can't find a valid activity to handle the request. Make sure you've a file explorer installed.")
                    result?.error(TAG, "Can't handle the provided file type.", null)
                }
            }
        } else {
            requestPermission()
        }
    }

    private fun checkPermission(): Boolean {
        instance?.activity()?.let {
            Log.i(TAG, "Checking permission: $permission")
            return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(it, permission)
        }
        return false
    }

    private fun requestPermission() {
        instance?.activity()?.let {
            Log.i(TAG, "Requesting permission: $permission")
            ActivityCompat.requestPermissions(it, arrayOf(permission), PERM_CODE)
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

}