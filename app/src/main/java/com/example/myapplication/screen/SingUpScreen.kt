package com.example.myapplication.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.ui.theme.Beige
import com.example.myapplication.ui.theme.Brown
import com.example.myapplication.R
import com.example.myapplication.composables.TitleCard
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults

@Composable
fun SignUpScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Brown)
    ) {
        TitleCard()
        SignUpArea(navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpArea(navController: NavController) {
    val clip_size = 100.dp
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = clip_size))
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Card(
            shape = RoundedCornerShape(topStart = clip_size)
        ) {
            Image(
                painter = painterResource(id = R.drawable.signin),
                contentDescription = "Background Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .alpha(0.9f)
                .background(Beige),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))
            SignupFields(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupFields(navController: NavController) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var selectedProvince by remember { mutableStateOf("") }
    var selectedFarm by remember { mutableStateOf("") }

    val provinces = listOf("Province A", "Province B", "Province C")
    val farms = listOf("Farm 1", "Farm 2", "Farm 3")

    var expandedProvince by remember { mutableStateOf(false) }
    var expandedFarm by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                TextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(2.dp, Color(114, 48, 21), RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        containerColor = Color.Transparent
                    )
                )
            }

            item {
                TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(2.dp, Color(114, 48, 21), RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        containerColor = Color.Transparent
                    )
                )
            }

            item {
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(2.dp, Color(114, 48, 21), RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        containerColor = Color.Transparent
                    )
                )
            }

            item {
                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(2.dp, Color(114, 48, 21), RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        containerColor = Color.Transparent
                    )
                )
            }

            item {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedProvince,
                        onExpandedChange = { expandedProvince = !expandedProvince }
                    ) {
                        TextField(
                            value = selectedProvince,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Province") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProvince) },
                            modifier = Modifier.menuAnchor()
                                .border(2.dp, Color(114, 48, 21), RoundedCornerShape(8.dp)),
                            colors = TextFieldDefaults.textFieldColors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                containerColor = Color.Transparent
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expandedProvince,
                            onDismissRequest = { expandedProvince = false }
                        ) {
                            provinces.forEach { province ->
                                DropdownMenuItem(
                                    text = { Text(province) },
                                    onClick = {
                                        selectedProvince = province
                                        expandedProvince = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedFarm,
                        onExpandedChange = { expandedFarm = !expandedFarm }
                    ) {
                        TextField(
                            value = selectedFarm,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Farm") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFarm) },
                            modifier = Modifier.menuAnchor()
                                .border(2.dp, Color(114, 48, 21), RoundedCornerShape(8.dp)),
                            colors = TextFieldDefaults.textFieldColors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                containerColor = Color.Transparent
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expandedFarm,
                            onDismissRequest = { expandedFarm = false }
                        ) {
                            farms.forEach { farm ->
                                DropdownMenuItem(
                                    text = { Text(farm) },
                                    onClick = {
                                        selectedFarm = farm
                                        expandedFarm = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (firstName.isBlank() || lastName.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        } else if (password != confirmPassword) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Sign-Up Success (Mock)", Toast.LENGTH_SHORT).show()
                            navController.navigate("home")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(50.dp)
                ) {
                    Text("Sign Up", fontSize = 20.sp)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Text(text = "Already have an account? ")
                    Text(
                        text = "Login",
                        color = Color.Blue,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clickable {
                                navController.navigate("login")
                            }
                    )
                }
            }
        }
    }
}


