package com.example.myapplication.ui.camera

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.myapplication.model.AABB
import kotlin.collections.forEach

@Composable
fun DetectionOverlay(boxes: List<AABB>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val modelInputSize = 640f
        val scale: Float
        val offsetX: Float
        val offsetY: Float

        if (canvasWidth / canvasHeight > 1f) {
            scale = canvasHeight / modelInputSize
            offsetX = (canvasWidth - modelInputSize * scale) / 2f
            offsetY = 0f
        } else {
            scale = canvasWidth / modelInputSize
            offsetX = 0f
            offsetY = (canvasHeight - modelInputSize * scale) / 2f
        }

        val textPaint = Paint().apply {
            color = Color.White.toArgb()
            textSize = 40f
        }

        boxes.forEach { box ->
            val left = box.x1 * modelInputSize * scale + offsetX
            val top = box.y1 * modelInputSize * scale + offsetY
            val right = box.x2 * modelInputSize * scale + offsetX
            val bottom = box.y2 * modelInputSize * scale + offsetY

            drawRect(
                color = Color.Red,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                style = Stroke(width = 4f)
            )

            val label = box.clsName
            val textBounds = Rect()
            textPaint.getTextBounds(label, 0, label.length, textBounds)
            val textWidth = textBounds.width().toFloat()
            val textHeight = textBounds.height().toFloat()

            drawRect(
                color = Color.Black.copy(alpha = 0.7f),
                topLeft = Offset(left, top - textHeight - 10),
                size = Size(textWidth + 16f, textHeight + 16f)
            )

            drawContext.canvas.nativeCanvas.drawText(
                label,
                left + 8f,
                top - 10f,
                textPaint
            )
        }
    }
}

