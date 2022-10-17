package com.dkennedy.a2022demorewrite

import android.content.Context
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import org.tensorflow.lite.support.common.FileUtil
import java.nio.charset.Charset

class SignClassifierModel(private val modelName: String) {
    private val localModel = LocalModel.Builder()
        .setAssetFilePath("custom_models/${modelName}.tflite")
        .build()

    val customImageLabelingOptions = CustomImageLabelerOptions.Builder(localModel)
        .setConfidenceThreshold(0.4f)
        .setMaxResultCount(2)
        .build()

    val labeler = ImageLabeling.getClient(customImageLabelingOptions)
}