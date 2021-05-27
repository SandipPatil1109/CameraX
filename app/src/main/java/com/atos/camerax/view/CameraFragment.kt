package com.atos.camerax.view

import android.Manifest
import android.app.usage.ExternalStorageStats
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.*
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.atos.camerax.R
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.*
import java.util.*


class CameraFragment : Fragment() {

    private var lensFacing = CameraX.LensFacing.BACK
    private val TAG = "CameraFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        if (allPermissionsGranted()) {
        texture.post { startCamera() }
//        } else {
//            ActivityCompat.requestPermissions(
//                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
//            )
//        }


        // Every time the provided texture view changes, recompute layout
        texture.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }

    }


    private fun startCamera() {
        val metrics = DisplayMetrics().also { texture.display.getRealMetrics(it) }
        val screenSize = Size(metrics.widthPixels, metrics.heightPixels)
        val screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)

        val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(lensFacing)
            setTargetResolution(screenSize)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(requireActivity().getWindowManager().getDefaultDisplay().rotation)
            setTargetRotation(texture.display.rotation)
        }.build()

        val preview = Preview(previewConfig)
        preview.setOnPreviewOutputUpdateListener {
            texture.setSurfaceTexture(it.surfaceTexture)
            updateTransform()
        }

        // Create configuration object for the image capture use case
        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .apply {
                setLensFacing(lensFacing)
                setTargetAspectRatio(screenAspectRatio)
                setTargetRotation(texture.display.rotation)
                setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY)
            }.build()

        // Build the image capture use case and attach button click listener
        val imageCapture = ImageCapture(imageCaptureConfig)

        //Capture Picture
        btn_take_picture.setOnClickListener {
            captureImage(imageCapture)
        }


        CameraX.bindToLifecycle(requireActivity(), preview, imageCapture)


    }

    private fun updateTransform() {
        val matrix = Matrix()
        val centerX = texture.width / 2f
        val centerY = texture.height / 2f

        val rotationDegrees = when (texture.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)
        texture.setTransform(matrix)
    }


    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS =
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
    }

//    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
//        ContextCompat.checkSelfPermission(
//            requireActivity(), it
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String>, grantResults:
//        IntArray
//    ) {
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            if (allPermissionsGranted()) {
//                texture.post { startCamera() }
//            } else {
//                Toast.makeText(
//                    requireActivity(),
//                    "Permissions not granted by the user.",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//    }


    private fun captureImage(imageCapture: ImageCapture) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "IMG${System.currentTimeMillis()}.jpg"
        )

        if (!file.exists()) {
            try {
                val isCreated = file.createNewFile()

            } catch (e: IOException) {

                Toast.makeText(requireActivity(), "Failed to Create File", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }

        imageCapture.takePicture(file, imageSavedListener)
    }


    private val imageSavedListener = object : ImageCapture.OnImageSavedListener {
        override fun onError(
            error: ImageCapture.UseCaseError,
            message: String, exc: Throwable?
        ) {
            val msg = "Photo capture failed: $message"
            Toast.makeText(requireActivity(), msg, Toast.LENGTH_SHORT).show()

        }

        override fun onImageSaved(file: File) {
            val msg = "Photo capture successfully: ${file.path}"
            Toast.makeText(requireActivity(), msg, Toast.LENGTH_SHORT).show()
            Log.d(TAG, "${file.absolutePath}")
        }
    }
}