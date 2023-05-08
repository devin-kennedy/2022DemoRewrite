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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Bitmap
import android.widget.TextView
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import java.math.RoundingMode
import java.text.DecimalFormat
import androidx.camera.core.internal.utils.ImageUtil
import androidx.core.graphics.toRectF
import com.example.a2022demorewrite.databinding.ActivityMainBinding
import com.shubham0204.ml.handdetection.BitmapUtils
import kotlinx.coroutines.*
import org.tensorflow.lite.task.gms.vision.detector.Detection


class FrameAnalyzer(
    handDetectionModel: TfliteObjectDetectorV2,
    handClassifierModel: HandDetectionModel,
    private val viewHeight: Float,
    private val viewWidth: Float,
    private val boundingBoxOverlay: BoundingBoxOverlay,
    private val resultTextView: TextView,
    private val context: Context,
    private val binding: ActivityMainBinding): ImageAnalysis.Analyzer {

    private val classifier = handClassifierModel.objectDetector
    private val objectDetector = handDetectionModel
    private var _scaleX = 0f
    private var _scaleY = 0f
    private var isFrameProcessing = false

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

    private fun drawOutput(results: MutableList<Detection>? = mutableListOf(), textOutput: List<String>) {
        resultTextView.text = textOutput.toString()
        val outBoxes = mutableListOf<RectF>()
        if (results != null) {
            for (prediction in results){
                outBoxes.add(prediction.boundingBox)
            }
        }
        boundingBoxOverlay.results = outBoxes
        boundingBoxOverlay.invalidate()
    }


    @SuppressLint("UnsafeOptInUsageError", "RestrictedApi")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            //val imageBitmap = ImageProxyTool().ImageProxyToBitmap(imageProxy.image)
            val imageBitmap = BitmapUtils.imageToBitmap(
                imageProxy.image!!,
                imageProxy.imageInfo.rotationDegrees,
                true
            )
            _scaleY = viewHeight / imageProxy.width.toFloat()
            _scaleX = viewWidth / imageProxy.height.toFloat()
            println(mediaImage.format.toString())

            runDetection(imageBitmap, imageProxy)
            imageProxy.close()
        }
    }

    private fun runClassification(imageBitmap: Bitmap, imageProxy: ImageProxy){

    }

    @SuppressLint("RestrictedApi")
    private fun runDetection(imageBitmap: Bitmap, imageProxy: ImageProxy){
        val objectDetectionPredictions = objectDetector.detect(imageBitmap, imageProxy.imageInfo.rotationDegrees)
        println(objectDetectionPredictions)
        if (objectDetectionPredictions == null) {
            println("Predictions is null")
        } else {
            println("Predictions is not null")
        }
        println("Above is predictions")

        val boxes = mutableListOf<RectF>()
        val classes = mutableListOf<String>()

        if (objectDetectionPredictions != null) {
            for (prediction in objectDetectionPredictions){
                boxes.add(prediction.boundingBox)
                val croppingImage = ImageUtil.yuvImageToJpegByteArray(
                    imageProxy, imageProxy.cropRect, 100)
                val fullBitmap = BitmapFactory.decodeByteArray(croppingImage, 0, croppingImage.size)
                val matrix = Matrix()
                matrix.postRotate(90f)
                val scaledBitmap = Bitmap.createScaledBitmap(fullBitmap, fullBitmap.width, fullBitmap.height, true)
                val rotatedFullBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
                val rect = prediction.boundingBox
                val croppingBitmap = Bitmap.createBitmap(
                    rotatedFullBitmap,
                    rect.left.toInt(),
                    rect.top.toInt(),
                    rect.width().toInt(),
                    rect.height().toInt()
                )
                val croppedInputImage = InputImage.fromBitmap(croppingBitmap, 0)
        //                classifier
        //                    .process(croppedInputImage)
        //                    .addOnFailureListener { e ->
        //                        e.printStackTrace()
        //                        print("Above failure on classification")
        //                    }
        //                    .addOnSuccessListener { classificationResult ->
        //                        for (classPrediction in classificationResult[0].labels){
        //                            classes.add(classPrediction.toString())
        //                        }
        //                    }
                    val text = listOf<String>("")
                    drawOutput(objectDetectionPredictions, text)
                }
        }

    }
}
