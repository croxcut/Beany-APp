package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
//import com.example.myapplication.screen.CameraDetectionScreen
import com.example.myapplication.screen.HomeScreen
import com.example.myapplication.screen.LoginScreen
import com.example.myapplication.screen.RealtimeDetectionScreen
import com.example.myapplication.screen.SingleImageDetection

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "login") {
        composable("home") {
            HomeScreen(
                onStartDetection = {
                    navController.navigate("detection")
                },
                onSingleImageDetection = {
                    navController.navigate("single-detection")
                },
                cameraImageDetection = {
                    navController.navigate("camera-detection")
                }
            )
        }

        composable("detection") {
            RealtimeDetectionScreen()
        }

        composable("single-detection") {
            SingleImageDetection()
        }

//        composable("camera-detection") {
//            CameraDetectionScreen()
//        }

        composable("login") {
            LoginScreen(
                navController
            )
        }

    }
}
