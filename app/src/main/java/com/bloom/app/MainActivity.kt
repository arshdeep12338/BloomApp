package com.bloom.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bloom.app.ui.navigation.BloomNavHost
import com.bloom.app.ui.theme.BloomTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as BloomApplication).container
        setContent {
            BloomTheme {
                BloomNavHost(container = container)
            }
        }
    }
}
