package com.example.myapplication.screen

import android.R.attr.bitmap
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.Beige
import com.example.myapplication.ui.theme.Brown
import com.example.myapplication.model.AABB
import com.example.myapplication.model.ModelRunner
import com.example.myapplication.model.ModelConstants.LABELS_PATH
import com.example.myapplication.model.ModelConstants.MODEL_PATH
import com.example.myapplication.ui.camera.DetectionOverlay
import com.example.myapplication.info.classInfoMap
import com.example.myapplication.info.loadClassInfoMap
import com.example.myapplication.model.DetectionViewModel
import androidx.compose.runtime.*
import androidx.lifecycle.LifecycleOwner
import androidx.core.content.ContextCompat
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import coil.size.Size
import java.nio.ByteBuffer
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

@Composable
fun CameraImageDetection() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }

    val previewView = remember { PreviewView(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var boxes by remember { mutableStateOf<List<AABB>>(emptyList()) }
    var detectedLabels by remember { mutableStateOf<List<String>>(emptyList()) }

    var classInfoMap by remember { mutableStateOf<Map<String, Pair<String, String>>>(emptyMap()) }
    val viewModel: DetectionViewModel = viewModel()

    var detectionDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        classInfoMap = loadClassInfoMap(context)

        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetRotation(previewView.display.rotation)
            .build()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Beige)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (bitmap == null) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Brown)
            ) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                DetectionOverlay(
                    boxes = boxes,
                    modifier = Modifier.matchParentSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!detectionDone) {
            Button(
                onClick = {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}")
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/MyApp")
                    }

                    val outputOptions = ImageCapture.OutputFileOptions.Builder(
                        context.contentResolver,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    ).build()

                    imageCapture?.takePicture(
                        outputOptions,
                        executor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                val savedUri = output.savedUri
                                val mainExecutor = ContextCompat.getMainExecutor(context)
                                mainExecutor.execute {
                                    imageUri = savedUri

                                    try {
                                        val inputStream = context.contentResolver.openInputStream(savedUri!!)
                                        val capturedBitmap = BitmapFactory.decodeStream(inputStream)
                                        inputStream?.close()

                                        bitmap = capturedBitmap

                                        val model = ModelRunner(
                                            context = context,
                                            modelPath = MODEL_PATH,
                                            labelPath = LABELS_PATH,
                                            detectorListener = object : ModelRunner.DetectorListener {
                                                override fun onEmptyDetect() {
                                                    boxes = emptyList()
                                                    detectedLabels = emptyList()
                                                }

                                                override fun onDetect(boundingBoxes: List<AABB>, inferenceTime: Long) {
                                                    boxes = boundingBoxes
                                                    detectedLabels = boundingBoxes.map { it.clsName }.distinct()
                                                }
                                            }
                                        )

                                        model.setup()
                                        model.detect(capturedBitmap)
                                        model.clear()

                                        detectionDone = true

                                        Toast.makeText(context, "Image captured and analyzed!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Log.e("DetectionError", "Failed to analyze image", e)
                                    }
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                val mainExecutor = ContextCompat.getMainExecutor(context)
                                mainExecutor.execute {
                                    Toast.makeText(
                                        context,
                                        "Capture failed: ${exception.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                Log.e("CameraCapture", "Image capture failed", exception)
                            }
                        }
                    )
                }
            ) {
                Icon(Icons.Default.Camera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Capture Image")
            }
        } else {
            Button(
                onClick = {
                    imageUri = null
                    bitmap = null
                    boxes = emptyList()
                    detectedLabels = emptyList()
                    detectionDone = false
                }
            ) {
                Icon(Icons.Default.Camera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Capture Image Again")
            }
        }

        if (detectedLabels.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brown)
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                detectedLabels.forEach { label ->
                    val (description, actionPlan) = classInfoMap[label] ?: ("No description available" to "No action defined")

                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Condition: $label", color = Color.White, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Description: - $description", color = Color.White, fontSize = 14.sp)
                        Text("Action Plan: - $actionPlan", color = Color.White, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        if (detectionDone && detectedLabels.isEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No disease detected.",
                fontSize = 16.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}



//@Composable
//fun CameraImageDetection() {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val executor = remember { Executors.newSingleThreadExecutor() }
//
//    val previewView = remember { PreviewView(context) }
//    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
//    var imageUri by remember { mutableStateOf<Uri?>(null) }
//
//    LaunchedEffect(Unit) {
//        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
//
//        val preview = Preview.Builder().build().also {
//            it.setSurfaceProvider(previewView.surfaceProvider)
//        }
//
//        imageCapture = ImageCapture.Builder()
//            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
//            .setTargetRotation(previewView.display.rotation)
//            .build()
//
//        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//        cameraProvider.unbindAll()
//        cameraProvider.bindToLifecycle(
//            lifecycleOwner,
//            cameraSelector,
//            preview,
//            imageCapture
//        )
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Beige)
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        if (imageUri == null) {
//            AndroidView(
//                factory = { previewView },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(400.dp)
//                    .clip(RoundedCornerShape(12.dp))
//            )
//        } else {
//            val bitmap = remember(imageUri) {
//                imageUri?.let {
//                    BitmapFactory.decodeStream(context.contentResolver.openInputStream(it))
//                }
//            }
//            bitmap?.let {
//                Image(
//                    bitmap = it.asImageBitmap(),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(400.dp)
//                        .clip(RoundedCornerShape(12.dp)),
//                    contentScale = ContentScale.Crop
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(
//            onClick = {
//                val contentValues = ContentValues().apply {
//                    put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}")
//                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//                    put(
//                        MediaStore.MediaColumns.RELATIVE_PATH,
//                        Environment.DIRECTORY_DCIM + "/MyApp"
//                    )
//                }
//
//                val outputOptions = ImageCapture.OutputFileOptions.Builder(
//                    context.contentResolver,
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    contentValues
//                ).build()
//
//                imageCapture?.takePicture(
//                    outputOptions,
//                    executor,
//                    object : ImageCapture.OnImageSavedCallback {
//                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                            val savedUri = output.savedUri
//                            val mainExecutor = ContextCompat.getMainExecutor(context)
//                            mainExecutor.execute {
//                                imageUri = savedUri
//                                Toast.makeText(
//                                    context,
//                                    "Image saved to gallery!",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        }
//
//                        override fun onError(exception: ImageCaptureException) {
//                            val mainExecutor = ContextCompat.getMainExecutor(context)
//                            mainExecutor.execute {
//                                Toast.makeText(
//                                    context,
//                                    "Capture failed: ${exception.message}",
//                                    Toast.LENGTH_LONG
//                                ).show()
//                            }
//                            Log.e("CameraCapture", "Image capture failed", exception)
//                        }
//                    }
//                )
//            }
//        ) {
//            Icon(Icons.Default.Camera, contentDescription = null)
//            Spacer(modifier = Modifier.width(8.dp))
//            Text("Capture Image")
//        }
//    }
//}
