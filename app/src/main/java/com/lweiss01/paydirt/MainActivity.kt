package com.lweiss01.paydirt

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.lweiss01.paydirt.ui.navigation.PayDirtNavGraph
import com.lweiss01.paydirt.ui.theme.PayDirtTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PayDirtApplication : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PayDirtTheme {
                PayDirtNavGraph(navController = rememberNavController())
            }
        }
    }
}
