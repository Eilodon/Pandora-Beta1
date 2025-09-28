package com.pandora.core.ai.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TensorFlow Lite Model Manager
 * Manages AI models for text analysis and pattern recognition
 */
@Singleton
class ModelManager @Inject constructor(
    private val context: Context
) {
    private var interpreter: Interpreter? = null
    private var isModelLoaded = false
    
    companion object {
        private const val TAG = "ModelManager"
        private const val MODEL_FILE = "pandora_model.tflite"
        private const val INPUT_SIZE = 128 // Token sequence length
        private const val OUTPUT_SIZE = 10 // Number of action categories
    }
    
    /**
     * Initialize and load the TensorFlow Lite model
     */
    suspend fun initializeModel() {
        try {
            val modelBuffer = loadModelFile()
            val options = Interpreter.Options().apply {
                setNumThreads(4) // Use 4 threads for inference
                setUseNNAPI(true) // Enable Neural Networks API if available
            }
            
            interpreter = Interpreter(modelBuffer, options)
            isModelLoaded = true
            
            Log.d(TAG, "Model loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model", e)
            isModelLoaded = false
        }
    }
    
    /**
     * Load model file from assets
     */
    private fun loadModelFile(): ByteBuffer {
        return try {
            FileUtil.loadMappedFile(context, MODEL_FILE)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model file", e)
            // Return empty buffer as fallback
            ByteBuffer.allocateDirect(0)
        }
    }
    
    /**
     * Analyze text and return action suggestions
     */
    fun analyzeText(text: String): List<ActionSuggestion> {
        if (!isModelLoaded || interpreter == null) {
            Log.w(TAG, "Model not loaded, returning empty suggestions")
            return emptyList()
        }
        
        return try {
            val input = preprocessText(text)
            val output = Array(1) { FloatArray(OUTPUT_SIZE) }
            
            interpreter?.run(input, output)
            parseSuggestions(output[0])
        } catch (e: Exception) {
            Log.e(TAG, "Error during inference", e)
            emptyList()
        }
    }
    
    /**
     * Preprocess text for model input
     */
    private fun preprocessText(text: String): ByteBuffer {
        // Simple tokenization - in production, use proper NLP preprocessing
        val tokens = text.lowercase()
            .replace(Regex("[^a-zA-Z0-9\\s]"), "")
            .split("\\s+")
            .take(INPUT_SIZE)
        
        val input = ByteBuffer.allocateDirect(INPUT_SIZE * 4) // 4 bytes per float
        input.order(ByteOrder.nativeOrder())
        
        // Simple embedding - in production, use proper word embeddings
        tokens.forEachIndexed { index, token ->
            if (index < INPUT_SIZE) {
                val embedding = token.hashCode().toFloat() / Int.MAX_VALUE
                input.putFloat(embedding)
            }
        }
        
        // Pad with zeros if needed
        while (input.position() < INPUT_SIZE * 4) {
            input.putFloat(0f)
        }
        
        input.rewind()
        return input
    }
    
    /**
     * Parse model output to action suggestions
     */
    private fun parseSuggestions(scores: FloatArray): List<ActionSuggestion> {
        val actions = ActionType.values()
        return scores.mapIndexed { index, score ->
            if (index < actions.size) {
                ActionSuggestion(actions[index], score)
            } else {
                ActionSuggestion(ActionType.UNKNOWN, 0f)
            }
        }.sortedByDescending { it.confidence }
            .take(4) // Return top 4 suggestions
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        interpreter?.close()
        interpreter = null
        isModelLoaded = false
    }
}

/**
 * Action types that the model can predict
 */
enum class ActionType {
    CALENDAR,      // "họp", "meeting", "lịch"
    REMIND,        // "nhắc", "remind", "nhớ"
    SEND,          // "gửi", "send", "share"
    NOTE,          // "ghi chú", "note", "ghi"
    MATH,          // "tính", "math", "calculator"
    CONVERT,       // "đổi", "convert", "chuyển"
    SEARCH,        // "tìm", "search", "google"
    TEMPLATE,      // "mẫu", "template", "form"
    EXTRACT,       // "trích xuất", "extract", "lấy"
    UNKNOWN        // Unknown action
}

/**
 * Action suggestion with confidence score
 */
data class ActionSuggestion(
    val action: ActionType,
    val confidence: Float
)
