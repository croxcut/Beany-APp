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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.model.DetectionViewModel

@Composable
fun SingleImageDetection() {
    val context = LocalContext.current
    var classInfoMap by remember { mutableStateOf<Map<String, Pair<String, String>>>(emptyMap()) }

    val viewModel: DetectionViewModel = viewModel()

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Beige),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.weight(1f)) {
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
            }
        }

        if (boxes.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brown)
            ) {
                detectedLabels.distinct().forEach { label ->
                    val (description, actionPlan) = classInfoMap[label] ?: ("No description available" to "No action defined")

                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text(
                            text = "Condition: $label",
                            fontSize = 16.sp,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Description:",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "    - $description",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Action Plan:",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "    - $actionPlan",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            Button(onClick = { launcher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Select Image", modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Image")
            }
        }
    }
}
