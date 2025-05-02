package com.example.myapplication.ui.camera

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

import androidx.camera.view.PreviewView
import androidx.compose.ui.unit.dp


@Composable
fun CameraPreviewView(
    modifier: Modifier = Modifier,
    onUseCaseReady: (PreviewView) -> Unit
) {
    AndroidView(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 100.dp),
        factory = { context ->
            PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FIT_CENTER
                onUseCaseReady(this)
            }
        }
    )
}
