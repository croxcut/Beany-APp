package com.example.myapplication.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.Beige
import com.example.myapplication.ui.theme.Etna
import com.example.myapplication.ui.theme.KareFont

@Composable
fun TitleCard() {
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .fillMaxHeight(0.2f)
            .padding(
                start = 30.dp
            ),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Beany",
            fontSize = 60.sp,
            color = Color.White,
            fontFamily = KareFont
        )
        Text(
            text = "Discover the World of Precision Farming",
            fontSize = 15.sp,
            color = Beige,
            fontFamily = Etna,
            modifier = Modifier.offset(y = (-10).dp)
        )
    }
}