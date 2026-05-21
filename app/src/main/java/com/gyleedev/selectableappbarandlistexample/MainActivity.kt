package com.gyleedev.selectableappbarandlistexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gyleedev.selectableappbarandlistexample.ui.theme.SelectableAppBarAndListExampleTheme
import dagger.hilt.android.AndroidEntryPoint

// Hilt 의존성 주입을 위한 진입점임을 명시합니다.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SelectableAppBarAndListExampleTheme {
                // 통합된 메인 화면인 MainScreen을 호출합니다.
                MainScreen()
            }
        }
    }
}

