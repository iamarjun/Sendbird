package com.arjun.sendbird.ui.message

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.util.*

class AttachmentHelper(
    lifecycle: Lifecycle,
    private val context: Context,
    private val registry: ActivityResultRegistry,
    private val onAttachmentPicked: (Uri) -> Unit
) : DefaultLifecycleObserver {

    init {
        lifecycle.addObserver(this)
    }

    private lateinit var getImages: ActivityResultLauncher<String>
    private lateinit var openCamera: ActivityResultLauncher<Uri>
    private lateinit var requestPermissions: ActivityResultLauncher<Array<String>>

    private var hasRequiredPermissions = false

    private val permissions =
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )

    private lateinit var currentPhotoPath: String
    private lateinit var currentPhotoUri: Uri

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = LocalDateTime.now().toString()
        val storageDir: File? =
            context.applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        getImages =
            registry.register(IMAGE_KEY, owner, ActivityResultContracts.GetContent()) {
                if (it == null)
                    return@register

                onAttachmentPicked(it)
            }

        openCamera =
            registry.register(CAMERA_KEY, owner, ActivityResultContracts.TakePicture()) {
                if (it)
                    onAttachmentPicked(currentPhotoUri)
            }

        requestPermissions = registry.register(
            PERMISSION_KEY,
            owner,
            ActivityResultContracts.RequestMultiplePermissions()
        ) { resultsMap ->
            resultsMap.forEach {
                Timber.d("Permission: ${it.key}, granted: ${it.value}")
                if (!it.value)
                    return@register

                hasRequiredPermissions = true
            }
        }
    }

    fun openGallery() {
        checkPermission()
        getImages.launch("image/*")
    }

    fun openCamera() {
        checkPermission()

        val file = try {
            createImageFile()
        } catch (e: Exception) {
            null
        }

        file?.also {
            currentPhotoUri = FileProvider.getUriForFile(
                context,
                "com.arjun.sendbird.fileprovider",
                it
            )
            openCamera.launch(currentPhotoUri)
        }
    }

    private fun checkPermission() {
        requestPermissions.launch(permissions)
    }

    companion object {
        private const val IMAGE_KEY = "image"
        private const val CAMERA_KEY = "camera"
        private const val PERMISSION_KEY = "permission"
    }
}