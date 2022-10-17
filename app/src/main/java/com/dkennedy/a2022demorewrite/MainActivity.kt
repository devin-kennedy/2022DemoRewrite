//   Copyright 2022 Devin Kennedy
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.dkennedy.a2022demorewrite

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.preference.PreferenceManager
import com.example.a2022demorewrite.databinding.ActivityMainBinding
import java.lang.Exception
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsButton: Button
    private lateinit var reloadButton: Button
    private lateinit var previewView: PreviewView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var boundingBoxOverlay: BoundingBoxOverlay
    private lateinit var resultTextView: TextView
    private lateinit var modelTextView: TextView
    private lateinit var camRes: String
    private lateinit var def_preferences: SharedPreferences
    private var cameraSelector: CameraSelector? = null
    private var lensFacing =  CameraSelector.LENS_FACING_BACK
    private var toClassify = mutableListOf<String>("asl_from_keras_pretrained_test0", "asl_pretrained_test2")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
        setContentView(binding.root)
        previewView = binding.previewView
        boundingBoxOverlay = binding.boundingBoxView
        resultTextView = binding.resultTextView
        modelTextView = binding.modelTextView
        settingsButton = binding.SetButton
        reloadButton = binding.reloadButton
        def_preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val model = def_preferences.getString("modelSelector", "3080-200E-3").toString()
        camRes = def_preferences.getString("cameraRes", "1280 720").toString()
        modelTextView.text = model
        val camResList = camRes.split("\\s".toRegex()).toTypedArray()

        if (allPermissionsGranted()) {
            start_camera(model, camResList[0].toInt(), camResList[1].toInt())
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        settingsButton.setOnClickListener { startSettings() }
        reloadButton.setOnClickListener { startActivity(intent) }

    }

    private fun startSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun start_camera(modelName: String, camWidth: Int, camHeight: Int) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        //val handDetectionModel = HandDetectionModel(modelName)
        val handDetectionModel = SignClassifierModel(modelName)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            /*
            val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(Size(camWidth, camHeight))
                    .build().apply {
                        setAnalyzer(
                            Executors.newSingleThreadExecutor(),
                            FrameAnalyzer(
                                handDetectionModel,
                                previewView.height.toFloat(),
                                previewView.width.toFloat(),
                                boundingBoxOverlay,
                                resultTextView
                            )
                        )
                    }
            */
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(Size(camWidth, camHeight))
                .build().apply {
                    setAnalyzer(
                        Executors.newSingleThreadExecutor(),
                        FrameAnalyzerClassify(
                            handDetectionModel,
                            resultTextView
                        )
                    )
                }



            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner, cameraSelector, imageAnalyzer, preview)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "2022DemoRewrite"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}