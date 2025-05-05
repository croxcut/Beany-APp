package com.example.myapplication.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.Surface
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.info.loadInstructions
import com.example.myapplication.model.AABB
import com.example.myapplication.model.DetectionViewModel
import com.example.myapplication.model.ModelConstants.LABELS_PATH
import com.example.myapplication.model.ModelConstants.MODEL_PATH
import com.example.myapplication.model.ModelRunner
import com.example.myapplication.ui.camera.DetectionOverlay
import com.example.myapplication.ui.theme.Beige
import com.example.myapplication.ui.theme.Brown
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Composable
fun RealtimeDetectionScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel: DetectionViewModel = viewModel()
    val overlayBoxes by viewModel.boxes.collectAsState()
    val inferenceTime by viewModel.inferenceTime.collectAsState()

    val detector = remember {
        ModelRunner(
            context = context,
            modelPath = MODEL_PATH,
            labelPath = LABELS_PATH,
            detectorListener = object : ModelRunner.DetectorListener {
                override fun onDetect(boundingBoxes: List<AABB>, time: Long) {
                    viewModel.updateDetection(boundingBoxes, time)
                }
                override fun onEmptyDetect() {
                    viewModel.updateDetection(emptyList(), 0L)
                }
            }
        ).apply { setup() }
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var analyzerActive by remember { mutableStateOf(true) }
    val currentContext = rememberUpdatedState(context)

    DisposableEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var isDisposed = false

        onDispose {
            analyzerActive = false
            isDisposed = true

            cameraExecutor.shutdown()
            try {
                if (!cameraExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    cameraExecutor.shutdownNow()
                }
            } catch (e: InterruptedException) {
                cameraExecutor.shutdownNow()
            }

            cameraProviderFuture.addListener({
                try {
                    cameraProviderFuture.get().unbindAll()
                } catch (e: Exception) {
                    Log.e("Camera", "Unbinding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))

            detector.clear()
        }
    }

    Column(
        Modifier.fillMaxSize()
            .background(Beige)) {
        Box(Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 4.dp,
                        color = Brown,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                AndroidView(factory = { context ->
                    PreviewView(context).apply {
                        startCamera(
                            context = context,
                            lifecycleOwner = lifecycleOwner,
                            previewView = this,
                            detector = detector,
                            cameraExecutor = cameraExecutor,
                            analyzerActive = analyzerActive
                        )
                    }
                }, modifier = Modifier.fillMaxSize())
            }

            DetectionOverlay(overlayBoxes, Modifier.fillMaxSize())

            Text(
                text = "Inference: ${inferenceTime}ms",
                color = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.5f))
            )
        }

        InstructionsPanel()
    }
}

@Composable
private fun InstructionsPanel() {
    val context = LocalContext.current
    Box(Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .height(200.dp)) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Text("Instructions:", fontSize = 18.sp, color = Color.Black)
            Spacer(Modifier.height(8.dp))
            loadInstructions(context).forEachIndexed { i, text ->
                Text("${i + 1}. $text", fontSize = 14.sp, color = Color.Black)
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

private fun startCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    detector: ModelRunner,
    cameraExecutor: ExecutorService,
    analyzerActive: Boolean
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        try {
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val displayRotation = previewView.display?.rotation ?: Surface.ROTATION_0

            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(displayRotation)
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            imageAnalyzer.setAnalyzer(cameraExecutor) { proxy ->
                if (!analyzerActive || detector.isCleared()) {
                    proxy.close()
                    return@setAnalyzer
                }

                try {
                    val bitmap = Bitmap.createBitmap(
                        proxy.width,
                        proxy.height,
                        Bitmap.Config.ARGB_8888
                    ).apply {
                        copyPixelsFromBuffer(proxy.planes[0].buffer)
                    }

                    val matrix = Matrix().apply {
                        postRotate(proxy.imageInfo.rotationDegrees.toFloat())
                    }

                    val rotatedBitmap = Bitmap.createBitmap(
                        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                    )

                    detector.detect(rotatedBitmap)
                } catch (e: Exception) {
                    Log.e("Camera", "Processing error", e)
                } finally {
                    proxy.close()
                }
            }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )
            preview.setSurfaceProvider(previewView.surfaceProvider)
        } catch (e: Exception) {
            Log.e("Camera", "Binding failed", e)
        }
    }, ContextCompat.getMainExecutor(context))
}