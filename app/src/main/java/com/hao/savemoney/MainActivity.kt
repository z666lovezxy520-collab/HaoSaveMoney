package com.hao.savemoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hao.savemoney.navigation.AppNavigation
import com.hao.savemoney.ui.theme.HaoSaveMoneyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HaoSaveMoneyTheme {
                AppNavigation()
            }
        }
    }
}
