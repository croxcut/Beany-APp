package com.example.myapplication.model

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.AABB
import com.example.myapplication.model.ModelConstants.LABELS_PATH
import com.example.myapplication.model.ModelConstants.MODEL_PATH
import com.example.myapplication.model.ModelRunner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetectionViewModel(application: Application) : AndroidViewModel(application) {

    private val _boxes = MutableStateFlow<List<AABB>>(emptyList())
    val boxes: StateFlow<List<AABB>> get() = _boxes

    private val _inferenceTime = MutableStateFlow(0L)
    val inferenceTime: StateFlow<Long> get() = _inferenceTime

    private val context = application.applicationContext

    fun updateDetection(boundingBoxes: List<AABB>, time: Long) {
        _boxes.value = boundingBoxes
        _inferenceTime.value = time
    }

    private val modelRunner = ModelRunner(
        context = context,
        modelPath = MODEL_PATH,
        labelPath = LABELS_PATH,
        detectorListener = object : ModelRunner.DetectorListener {
            override fun onDetect(boundingBoxes: List<AABB>, time: Long) {
                _boxes.value = boundingBoxes
                _inferenceTime.value = time
            }

            override fun onEmptyDetect() {
                _boxes.value = emptyList()
                _inferenceTime.value = 0L
            }
        }
    ).apply {
        setup()
    }

    fun detect(bitmap: Bitmap) {
        viewModelScope.launch {
            modelRunner.detect(bitmap)
        }
    }

    override fun onCleared() {
        super.onCleared()
        modelRunner.clear()
    }
}
