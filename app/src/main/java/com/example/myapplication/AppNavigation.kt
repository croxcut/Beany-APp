package com.example.myapplication.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.Beige
import com.example.myapplication.ui.theme.Brown

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {

        composable("login") {
            LoginScreen(navController)
        }

        composable("signup") {
            SignUpScreen(navController)
        }

        composable("home") {
            HomeScreen(
                onStartDetection = {
                    navController.navigate("realtimeDetection")
                                   },
                onSingleImageDetection = {
                    navController.navigate("singleImageDetection")
                                         },
                cameraImageDetection = {
                    navController.navigate("cameraImageDetection")
                }
            )
        }

        composable("realtimeDetection") {
            RealtimeDetectionScreen()
        }

        composable("singleImageDetection") {
            SingleImageDetection()
        }

         composable("cameraImageDetection") {
             CameraImageDetection()
         }
    }
}