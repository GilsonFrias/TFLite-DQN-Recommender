package com.gilsonfrias.recommenderpoc

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.jvm.Throws
import kotlin.math.exp

class Recommender(private val context: Context){
    private var interpreter: Interpreter? = null

    private val executorService: ExecutorService = Executors.newCachedThreadPool()

    fun initialize(): Task<Void?> {
        val task = TaskCompletionSource<Void>()
        executorService.execute {
            try {
                initializeInterpreter()
                task.setResult(null)
            }catch (e: IOException) {
                task.setException(e)
            }
        }
        return task.task
    }

    @Throws(IOException::class)
    private fun initializeInterpreter() {
        val assetManager = context.assets
        val model = loadModelFile(assetManager, "sig_dqn_model.tflite")
        val interpreter = Interpreter(model)
        val inputShape = interpreter.getInputTensor(0).shape()
        val inputDtype = interpreter.getInputTensor(0).dataType()
        val outputShape = interpreter.getOutputTensor(0).shape()
        val outputDtype = interpreter.getOutputTensor(0).dataType()

        this.interpreter = interpreter
        Log.d("[Recommender:initializeInterpreter]", "Input tensor shape: ${inputShape.joinToString()}")
        Log.d("[Recommender:initializeInterpreter]", "Input tensor dtype: $inputDtype")
        Log.d("[Recommender:initializeInterpreter]", "Output tensor shape: ${outputShape.joinToString()}")
        Log.d("[Recommender:initializeInterpreter]", "Output tensor dtype: $outputDtype")
        Log.d("[Recommender:initializeInterpreter]", "Initialized TF interpreter")
    }

    fun predict(input: Array<Float>, normalizeSoftmax: Boolean = false): FloatArray {
        return predict(input.toFloatArray(), normalizeSoftmax = normalizeSoftmax)
    }

    fun predict(input: FloatArray, normalizeSoftmax: Boolean = false): FloatArray {
        val interpreter = this.interpreter
        if (interpreter == null) {
            Log.e("[Recommender:predict]", "Interpreter is not initialized")
            return floatArrayOf()
        }
        // Tensor shape for sig_dqn_model is [1, 6] FLOAT32 for input and [1, 9] FLOAT32 for output
        val input2D = arrayOf(input)
        val output = Array(1) { FloatArray(9) }
        //Log.d("[Recommender:predict]", "Running inference on input: ${input.contentToString()}")
        interpreter.run(input2D, output)
        Log.d("[Recommender:predict]", "Input features: ${input.contentToString()}")
        Log.d("[Recommender:predict]", "Result Q-values: ${output[0].contentToString()}")
        if (normalizeSoftmax) {
            return softmax(output[0])
        }
        return output[0]
    }

    fun createStringByteBuffer(text: String): ByteBuffer {
        val bytes = text.toByteArray(Charsets.UTF_8)
        // Allocate space for byte length header + string capacity
        val buffer = ByteBuffer.allocateDirect(bytes.size + 4)
        buffer.order(ByteOrder.nativeOrder())
        buffer.putInt(bytes.size)
        buffer.put(bytes)
        buffer.rewind()
        return buffer
    }

    fun close() {
        executorService.execute {
            interpreter?.close()
            Log.d("[Recommender:close]", "Closed TF interpreter")
        }
    }

    @Throws(IOException::class)
    private fun loadModelFile(assetManager: AssetManager, filename: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.maxOrNull() ?: 0.0f
        val exponentials = logits.map { exp(it - maxLogit) }
        val sumExponentials = exponentials.sum()
        return exponentials.map { it / sumExponentials }.toFloatArray()
    }

}