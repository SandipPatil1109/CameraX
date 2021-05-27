package com.atos.camerax.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.atos.camerax.R
import com.atos.camerax.view.OpenCamera.Companion.REQUEST_CODE_PERMISSIONS
import com.atos.camerax.view.OpenCamera.Companion.REQUIRED_PERMISSIONS
import kotlinx.android.synthetic.main.activity_open_camera.*
import kotlinx.android.synthetic.main.fragment_camera.*

class OpenCamera : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
         private val REQUIRED_PERMISSIONS =
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_camera)



        btnStartCamera.setOnClickListener {

            if (allPermissionsGranted()) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    OpenCamera.REQUIRED_PERMISSIONS,
                    OpenCamera.REQUEST_CODE_PERMISSIONS
                )
            }
        }
    }

    private fun allPermissionsGranted() = OpenCamera.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            this, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == OpenCamera.REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


}