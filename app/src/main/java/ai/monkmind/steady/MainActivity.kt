package ai.monkmind.steady

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ai.monkmind.steady.core.designsystem.theme.SteadyTheme
import ai.monkmind.steady.feature.today.TodayRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SteadyTheme {
                TodayRoute()
            }
        }
    }
}

