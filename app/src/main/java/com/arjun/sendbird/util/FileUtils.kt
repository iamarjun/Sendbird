package com.arjun.sendbird.util

import android.app.DownloadManager
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import java.io.*
import java.text.DecimalFormat
import java.util.*

/**
 * DateUtils related to file handling (for sending / downloading file messages).
 */
object FileUtils {
    fun getFileInfo(context: Context, uri: Uri): Hashtable<String, Any?>? {
        try {
            context.contentResolver.query(uri, null, null, null, null).use { cursor ->
                val mime = context.contentResolver.getType(uri)
                if (cursor != null) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    val value = Hashtable<String, Any?>()
                    if (cursor.moveToFirst()) {
                        var name = cursor.getString(nameIndex)
                        val size = cursor.getLong(sizeIndex).toInt()
                        if (name.isEmpty()) {
                            name = "Temp_" + uri.hashCode() + "." + extractExtension(context, uri)
                        }
                        val file = File(context.cacheDir, name)
                        val inputPFD = context.contentResolver.openFileDescriptor(uri, "r")
                        var fd: FileDescriptor? = null
                        if (inputPFD != null) {
                            fd = inputPFD.fileDescriptor
                        }
                        val inputStream = FileInputStream(fd)
                        val outputStream = FileOutputStream(file)
                        var read: Int
                        val bytes = ByteArray(1024)
                        while (inputStream.read(bytes).also { read = it } != -1) {
                            outputStream.write(bytes, 0, read)
                        }
                        value["path"] = file.absolutePath
                        value["size"] = size
                        value["mime"] = mime
                        value["name"] = name
                    }
                    return value
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(e.localizedMessage, "File not found.")
            return null
        }
        return null
    }

    fun extractExtension(context: Context, uri: Uri): String? {
        val extension: String?
        extension = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            extractExtension(context.contentResolver.getType(uri)!!)
        } else {
            MimeTypeMap.getFileExtensionFromUrl(
                Uri.fromFile(File(uri.path)).toString()
            )
        }
        return extension
    }

    fun extractExtension(mimeType: String): String? {
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(mimeType)
    }

    /**
     * Downloads a file using DownloadManager.
     */
    fun downloadFile(context: Context, url: String?, fileName: String?) {
        val downloadRequest = DownloadManager.Request(Uri.parse(url))
        downloadRequest.setTitle(fileName)

        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            downloadRequest.allowScanningByMediaScanner()
            downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        }
        downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(downloadRequest)
    }

    /**
     * Converts byte value to String.
     */
    fun toReadableFileSize(size: Long): String {
        if (size <= 0) return "0KB"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(
            size / Math.pow(
                1024.0,
                digitGroups.toDouble()
            )
        ) + " " + units[digitGroups]
    }

    @Throws(IOException::class)
    fun saveToFile(file: File, data: String) {
        val tempFile = File.createTempFile("sendbird", "temp")
        val fos = FileOutputStream(tempFile)
        fos.write(data.toByteArray())
        fos.close()
        if (!tempFile.renameTo(file)) {
            throw IOException("Error to rename file to " + file.absolutePath)
        }
    }

    @Throws(IOException::class)
    fun loadFromFile(file: File?): String {
        val stream = FileInputStream(file)
        val reader: Reader = BufferedReader(InputStreamReader(stream))
        val builder = StringBuilder()
        val buffer = CharArray(8192)
        var read: Int
        while (reader.read(buffer, 0, buffer.size).also { read = it } > 0) {
            builder.append(buffer, 0, read)
        }
        return builder.toString()
    }

    fun deleteFile(file: File?) {
        if (file != null && file.exists()) {
            file.delete()
        }
    }
}