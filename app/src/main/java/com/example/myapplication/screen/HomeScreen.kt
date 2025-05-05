package com.example.myapplication.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            .background(Color(0xFF6B3F23))
    ) {
        TitleCard()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
                .padding(all = 1.dp)
                .background(Beige, shape = RoundedCornerShape(12.dp)),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Welcome to Beany Shet",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onSingleImageDetection,
                    modifier = Modifier
                        .size(50.dp) // Smaller size for left icon
                        .background(Color(0xFF6B3F23), shape = RoundedCornerShape(12.dp))
                        .border(2.dp, Color(0xFF6B3F23), shape = RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Folder",
                        modifier = Modifier.size(35.dp),
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = onStartDetection,
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFFF5F5DC), shape = RoundedCornerShape(50.dp))
                        .border(4.dp, Color(0xFF6B3F23), shape = RoundedCornerShape(50.dp))
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.RadioButtonChecked,
                        contentDescription = "Record",
                        modifier = Modifier.size(70.dp),
                        tint = Color(0xFF6B3F23)
                    )
                }

                IconButton(
                    onClick = cameraImageDetection,
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color(0xFF6B3F23), shape = RoundedCornerShape(12.dp))
                        .border(2.dp, Color(0xFF6B3F23), shape = RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        modifier = Modifier.size(35.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

