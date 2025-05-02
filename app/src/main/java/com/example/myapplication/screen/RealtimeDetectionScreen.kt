package com.example.myapplication.screen

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp

import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat


import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.Surface
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import com.example.myapplication.ui.camera.DetectionOverlay
import com.example.myapplication.model.AABB
import com.example.myapplication.model.ModelConstants.LABELS_PATH
import com.example.myapplication.model.ModelConstants.MODEL_PATH
import com.example.myapplication.model.ModelRunner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun RealtimeDetectionScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { mutableStateOf<PreviewView?>(null) }
    val isFrontCamera = false

    val overlayBoxes = remember { mutableStateOf<List<AABB>>(emptyList()) }
    val inferenceTime = remember { mutableStateOf(0L) }

    val detector = remember {
        ModelRunner(context, MODEL_PATH, LABELS_PATH, object : ModelRunner.DetectorListener {
            override fun onDetect(boundingBoxes: List<AABB>, time: Long) {
                overlayBoxes.value = boundingBoxes
                inferenceTime.value = time
            }

            override fun onEmptyDetect() {
                overlayBoxes.value = emptyList()
            }
        }).apply { setup() }
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            detector.clear()
            cameraExecutor.shutdown()
        }
    }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            AndroidView(factory = {
                PreviewView(it).also { view ->
                    previewView.value = view
                    startCamera(
                        context = context,
                        lifecycleOwner = lifecycleOwner,
                        previewView = view,
                        detector = detector,
                        overlayBoxes = overlayBoxes,
                        inferenceTime = inferenceTime,
                        cameraExecutor = cameraExecutor,
                        isFrontCamera = isFrontCamera
                    )
                }
            }, modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp, bottom = 20.dp))

            DetectionOverlay(overlayBoxes.value, Modifier.fillMaxSize())

            Text(
                text = "[Debug] Inference -> ${inferenceTime.value} ms",
                color = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.5f))
            )
        }

        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(200.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Instructions:",
                    color = Color.Black,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = List(12) { i -> "${i + 1}. This is an additional instruction." }.joinToString("\n"),
                    color = Color.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}

private fun startCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    detector: ModelRunner,
    overlayBoxes: MutableState<List<AABB>>,
    inferenceTime: MutableState<Long>,
    cameraExecutor: ExecutorService,
    isFrontCamera: Boolean
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val displayRotation = previewView.display?.rotation ?: Surface.ROTATION_0
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(displayRotation)
            .build()

        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .setTargetRotation(displayRotation)
            .build()

        imageAnalyzer.setAnalyzer(cameraExecutor) { imageProxy ->
            val bitmap = Bitmap.createBitmap(
                imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
            ).apply {
                copyPixelsFromBuffer(imageProxy.planes[0].buffer)
            }

            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val matrix = Matrix().apply {
                postRotate(rotationDegrees.toFloat())
                if (isFrontCamera) {
                    postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
                }
            }

            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            imageProxy.close()

            val inputSize = 640
            val scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap, inputSize, inputSize, true)

            detector.detect(scaledBitmap)
        }

        try {
            cameraProvider.unbindAll()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalyzer)
        } catch (e: Exception) {
            Log.e("Camera", "Binding failed", e)
        }
    }, ContextCompat.getMainExecutor(context))
}