package com.dkennedy.a2022demorewrite

import android.annotation.SuppressLint
import android.graphics.Rect
import android.graphics.RectF
import android.widget.TextView
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

class FrameAnalyzer(
    handDetectionModel: HandDetectionModel,
    private val viewHeight: Float,
    private val viewWidth: Float,
    private val boundingBoxOverlay: BoundingBoxOverlay,
    private val resultTextView: TextView): ImageAnalysis.Analyzer {

    private val objectDetector = handDetectionModel.objectDetector
    private var _scaleX = 0f
    private var _scaleY = 0f

    private fun translateX(x: Float): Float = x * _scaleX
    private fun translateY(y: Float): Float = y * _scaleY

    private fun translateRect(rect: Rect) = RectF(
        translateX(rect.left.toFloat()),
        translateY(rect.top.toFloat()),
        translateX(rect.right.toFloat()),
        translateY(rect.bottom.toFloat())
    )

    private fun getPercent(confidence: Float): String {
        val df = DecimalFormat("##.##")
        df.roundingMode = RoundingMode.CEILING
        val rounded = df.format(confidence * 100).toFloat()
        return "${rounded}%"
    }

    private fun drawOutput(results: List<RectF>, textOutput: List<String>) {
        resultTextView.text = textOutput.toString()
        boundingBoxOverlay.results = results
        boundingBoxOverlay.invalidate()
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            _scaleY = viewHeight / imageProxy.width.toFloat()
            _scaleX = viewWidth / imageProxy.height.toFloat()

            objectDetector
                .process(image)
                .addOnFailureListener { e ->
                    println("Failure on object detection: ${e}}")
                }
                .addOnSuccessListener { results ->
                    val boxes = mutableListOf<RectF>()
                    val outputs = mutableListOf<String>()
                    for (detectedObject in results) {
                        if (detectedObject.labels.size >= 1) {
                            boxes.add(translateRect(detectedObject.boundingBox))
                            outputs.add(detectedObject.labels[0].text + " " + getPercent(detectedObject.labels[0].confidence))
                        }
                    }
                    drawOutput(
                        boxes,
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