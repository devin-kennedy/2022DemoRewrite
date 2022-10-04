package com.dkennedy.a2022demorewrite

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import java.math.RoundingMode
import java.text.DecimalFormat

class FrameAnalyzerClassify(
    signClassifierModel: SignClassifierModel,
    private val resultTextView: TextView): ImageAnalysis.Analyzer {

    private val imageClassifier = signClassifierModel.labeler

    private fun getPercent(confidence: Float): String {
        val df = DecimalFormat("##.##")
        df.roundingMode = RoundingMode.CEILING
        val rounded = df.format(confidence * 100).toFloat()
        return "${rounded}%"
    }

    private fun drawOutput(textOutput: List<String>) {
        resultTextView.text = textOutput.toString()
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            imageClassifier
                .process(image)
                .addOnFailureListener { e ->
                    println("Failure on image classification: $e")
                }
                .addOnSuccessListener { labels ->
                    val outputs = mutableListOf<String>()
                    for (label in labels) {
                        outputs.add(label.text + " " + getPercent(label.confidence))
                    }
                    drawOutput(
                        outputs
                    )
                }
                .addOnCompleteListener {
                    mediaImage.close()
                    imageProxy.close()
                }
        }
    }
}