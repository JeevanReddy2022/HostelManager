package com.example.hostelmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.hostelmanager.ui.hostels.HostelListScreen
import com.example.hostelmanager.ui.theme.HostelManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HostelManagerTheme {
                HostelListScreen()
            }
        }
    }
}