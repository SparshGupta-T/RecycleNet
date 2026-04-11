package com.example.recyclenet

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

data class Detection(
    val boundingBox: android.graphics.RectF,
    val score: Float,
    val classIndex: Int
)

class ModelRunner(private val context: Context) {
    private var module: Module? = null
    
    val classes = arrayOf("HDPE", "LDPE", "PET", "PP", "PS", "PVC")

    fun loadModel() {
        if (module != null) return
        try {
            val modelPath = assetFilePath(context, "best.ptl")
            module = Module.load(modelPath)
            Log.d("ModelRunner", "Model loaded successfully")
        } catch (e: Exception) {
            Log.e("ModelRunner", "Error loading model", e)
        }
    }

    fun analyzeImage(bitmap: Bitmap): List<Detection> {
        loadModel()
        val localModule = module ?: return emptyList()

        // YOLO standard 640x640
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, true)
        
        // Use normalized values [0, 1] for YOLO
        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            resizedBitmap,
            floatArrayOf(0f, 0f, 0f),
            floatArrayOf(1f, 1f, 1f)
        )

        try {
            val output = localModule.forward(IValue.from(inputTensor))
            val outputTensor = when {
                output.isTensor -> output.toTensor()
                output.isTuple -> output.toTuple()[0].toTensor()
                else -> return emptyList()
            }

            val data = outputTensor.dataAsFloatArray
            val shape = outputTensor.shape()
            Log.d("ModelRunner", "Output shape: ${shape.joinToString("x")}")

            val detections = mutableListOf<Detection>()
            
            // Format 1x300x6: [x1, y1, x2, y2, score, class_id]
            for (i in 0 until 300) {
                val offset = i * 6
                if (offset + 5 >= data.size) break
                
                val score = data[offset + 4]
                
                // Very low threshold for debugging to see IF anything is detected
                if (score > 0.15f) { 
                    var x1 = data[offset + 0]
                    var y1 = data[offset + 1]
                    var x2 = data[offset + 2]
                    var y2 = data[offset + 3]
                    val classIdx = data[offset + 5].toInt()

                    // Crucial: Handle both normalized (0-1) and pixel (0-640) coordinates
                    // If the max value is small, it's likely normalized.
                    if (x1 <= 1.1f && x2 <= 1.1f && y1 <= 1.1f && y2 <= 1.1f) {
                        x1 *= 640f
                        y1 *= 640f
                        x2 *= 640f
                        y2 *= 640f
                    }
                    
                    Log.d("ModelRunner", "Detected: class=$classIdx, score=$score, box=[$x1, $y1, $x2, $y2]")
                    
                    detections.add(
                        Detection(
                            boundingBox = android.graphics.RectF(x1, y1, x2, y2),
                            score = score,
                            classIndex = classIdx
                        )
                    )
                }
            }
            return detections
        } catch (e: Exception) {
            Log.e("ModelRunner", "Inference error", e)
            return emptyList()
        }
    }

    private fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) return file.absolutePath
        context.assets.open(assetName).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
            }
            return file.absolutePath
        }
    }
}
