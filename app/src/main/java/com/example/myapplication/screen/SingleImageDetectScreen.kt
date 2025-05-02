package com.example.myapplication.screen

// Compose
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp

// Lifecycle

// CameraX
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat

// Other
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import com.example.myapplication.info.classInfoMap
import com.example.myapplication.info.loadClassInfoMap
import com.example.myapplication.ui.camera.DetectionOverlay
import com.example.myapplication.model.AABB
import com.example.myapplication.model.ModelConstants.LABELS_PATH
import com.example.myapplication.model.ModelConstants.MODEL_PATH
import com.example.myapplication.model.ModelRunner
import com.example.myapplication.ui.theme.Beige
import com.example.myapplication.ui.theme.Brown
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.provider.MediaStore
import android.content.ContentValues
import android.os.Build
import android.os.Environment

@Composable
fun SingleImageDetection() {
    val context = LocalContext.current
    var classInfoMap by remember { mutableStateOf<Map<String, Pair<String, String>>>(emptyMap()) }

    LaunchedEffect(Unit) {
        classInfoMap = loadClassInfoMap(context)
    }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var boxes by remember { mutableStateOf<List<AABB>>(emptyList()) }
    var detectedLabels by remember { mutableStateOf<List<String>>(emptyList()) }

    val photoUri = remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val tempBitmap = BitmapFactory.decodeStream(inputStream)
            bitmap = tempBitmap
            inputStream?.close()

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
            model.detect(tempBitmap)
            model.clear()
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri.value?.let { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempBitmap = BitmapFactory.decodeStream(inputStream)
                bitmap = tempBitmap
                inputStream?.close()

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
                model.detect(tempBitmap)
                model.clear()
            }
        }
    }

    fun createImageFile(context: Context): Uri {
        val imageFile = java.io.File(
            context.getExternalFilesDir(null),
            "Pictures/photo_${System.currentTimeMillis()}.jpg"
        )
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, fileName: String = "detected_image_${System.currentTimeMillis()}.jpg") {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val contentResolver = context.contentResolver
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(it, contentValues, null, null)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Beige),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { launcher.launch("image/*") }) {
                Text("Pick Image from Gallery")
            }

            Button(onClick = {
                val uri = createImageFile(context)
                photoUri.value = uri
                takePictureLauncher.launch(uri)
            }) {
                Text("Take Photo")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        bitmap?.let { bmp ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(width = 2.dp, color = Brown, shape = RoundedCornerShape(16.dp))
            ) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                DetectionOverlay(
                    boxes = boxes,
                    modifier = Modifier.matchParentSize()
                )
            }


            Spacer(modifier = Modifier.height(12.dp))

            if (boxes.isEmpty()) {
                Text(
                    text = "No cacao/coffee detected",
                    color = Color.Red,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            } else {
                Text(
                    text = "Detected: ${detectedLabels.joinToString()}",
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            detectedLabels.forEach { label ->
                val (description, actionPlan) = classInfoMap[label] ?: ("No description available" to "No action defined")

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brown)
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = "Condition: $label",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.padding(1.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Description: $description",
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.padding(1.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Action Plan: $actionPlan",
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.padding(1.dp)
                    )
                }
            }
        }
    }
}