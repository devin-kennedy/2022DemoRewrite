package com.dkennedy.a2022demorewrite

import android.content.Context
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions

data class HandDetectionModel(private val modelName: String) {
    private val localModel = LocalModel.Builder()
        .setAssetFilePath("custom_models/${modelName}.tflite")
        .build()

    private val customObjectDetectorOptions = CustomObjectDetectorOptions.Builder(localModel)
        .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
        .enableClassification()
        .setClassificationConfidenceThreshold(0.4f)
        .enableMultipleObjects()
        .build()

    val objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)

}