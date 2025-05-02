package com.example.myapplication.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.myapplication.composables.TitleCard
import com.example.myapplication.ui.theme.Beige
import com.example.myapplication.ui.theme.Brown

@Composable
fun HomeScreen(
    onStartDetection: () -> Unit,
    onSingleImageDetection: () -> Unit,
    cameraImageDetection: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Brown)
    ) {
        TitleCard()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(all = 1.dp)
                .background(Beige),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to Beany Shet",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onStartDetection,
                colors = ButtonDefaults.buttonColors(containerColor = Brown),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(text = "Start Real-time Detection")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSingleImageDetection,
                colors = ButtonDefaults.buttonColors(containerColor = Brown),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(text = "Detect from Single Image")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = cameraImageDetection,
                colors = ButtonDefaults.buttonColors(containerColor = Brown),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(text = "Take Photo")
            }
        }
    }
}

