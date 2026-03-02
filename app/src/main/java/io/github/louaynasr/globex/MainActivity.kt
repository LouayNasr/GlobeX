package io.github.louaynasr.globex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.github.louaynasr.globex.core.presentation.navigtion.NavigationRoot
import io.github.louaynasr.globex.ui.theme.GlobeXTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlobeXTheme {
                NavigationRoot()
            }
        }
    }
}
