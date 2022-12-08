package com.raja.cameragallerydemo

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.raja.cameragallerydemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    val REQUEST_CODE_PICK = 2

    val REQUEST_IMAGE_CAPTURE = 1
    private var imageUri: Uri? = null

    private lateinit var binding: ActivityMainBinding

    // You can put this line in constant.
    val storagePermission = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA
    )

    private val permissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
            granted.entries.forEach {
                when (it.value) {
                    true -> {
                        // Call whatever you want to do when someone allow the permission.
                    }
                    false -> {
                        showPermissionSettingsAlert(this)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        initUI()

    }

    private fun initUI() {

        binding.btnCamera.setOnClickListener {
            if (!checkPermissionStorage(this)) {
                permissions.launch(
                    storagePermission
                )
            } else {
                dispatchTakePictureIntent()
            }
        }
        binding.btnGallery.setOnClickListener {
            if (!checkPermissionStorage(this)) {
                permissions.launch(
                    storagePermission
                )
            } else {
                openGalleryForImage()
            }
        }


    }


    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.ivProfileImage.setImageBitmap(imageBitmap)
        }
        if (requestCode == REQUEST_CODE_PICK && resultCode == RESULT_OK) {

            imageUri = data?.data
            binding.ivProfileImage.setImageURI(imageUri) // handle chosen image

        }

    }

    // You can put this in AppUtil.
    private fun checkPermissionStorage(context: Context): Boolean {
        val resultGallery =
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        val resultCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        return resultGallery == PackageManager.PERMISSION_GRANTED && resultCamera == PackageManager.PERMISSION_GRANTED


    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PICK)
    }

    private fun showPermissionSettingsAlert(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Grant Permission")
        builder.setMessage("You have rejected the Storage permission for the application. As it is absolutely necessary for the app to perform you need to enable it in the settings of your device. Please select \"Go to settings\" to go to application settings in your device.")
        builder.setPositiveButton("Allow") { dialog, which ->
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }
        builder.setNeutralButton("Deny") { dialog, which ->

            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
}