package com.dkennedy.a2022demorewrite

import android.content.Context
import android.graphics.Bitmap
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.google.android.gms.tflite.client.TfLiteInitializationOptions
import com.google.android.gms.tflite.gpu.support.TfLiteGpu
import org.tensorflow.lite.Tensor
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.gms.vision.TfLiteVision
import org.tensorflow.lite.task.gms.vision.detector.Detection
import org.tensorflow.lite.task.gms.vision.detector.ObjectDetector

class TfliteObjectDetectorV2(
    val context: Context,
    var currentDelegate: Int = 0
) {
    private var objectDetector: ObjectDetector? = null
    private var gpuSupported = false

    init {
        TfLiteGpu.isGpuDelegateAvailable(context).onSuccessTask { isGpu: Boolean ->
            val optionsBuilder = TfLiteInitializationOptions.builder()
            if (isGpu) {
                optionsBuilder.setEnableGpuDelegateSupport(true)
            }
            TfLiteVision.initialize(context, optionsBuilder.build())
        }
    }

    fun clearObjectDetector() {
        objectDetector = null
    }

    fun setupObjectDetector(){
        if (!TfLiteVision.isInitialized()) {
            print("VISION NOT INITIALIZED YET")
            return
        }

        val optionsBuilder = ObjectDetector.ObjectDetectorOptions.builder()
            .setScoreThreshold(0.5f)
            .setMaxResults(2)

        val baseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(2)

        when (currentDelegate) {
            DELEGATE_CPU -> {

            }
            DELEGATE_GPU -> {
                if (gpuSupported) {
                    baseOptionsBuilder.useGpu()
                } else {
                    print("GPU NOT SUPPORTED ON DEVICE")
                }
            }
        }

        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        val modelName = "custom_models/hand_detection23.tflite"

        try {
            ObjectDetector.createFromFileAndOptions(context, modelName, optionsBuilder.build())
        } catch (e: Exception) {
            print("FAILED TO INITIALIZE MODEL")
            e.printStackTrace()
        }
    }

    fun detect(image: Bitmap, imageRotation: Int): MutableList<Detection>? {
        if (!TfLiteVision.isInitialized()) {
            print("TFLITE NOT INITIALIZED YET")
            return mutableListOf()
        }

        if (objectDetector == null){
            setupObjectDetector()
        }

        val imageProcessor = ImageProcessor.Builder().add(Rot90Op(-imageRotation/90)).build()

        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(image))

        val results = objectDetector?.detect(tensorImage)
        return results
    }

    interface DetectorListener {
        fun onInitalized()
        fun onError(error: String)
        fun onResults(
            results: MutableList<Detection>?,
            imageHeight: Int,
            imageWidth: Int
        )
    }

    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
    }

}