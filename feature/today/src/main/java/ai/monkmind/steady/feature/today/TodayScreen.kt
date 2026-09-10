package ai.monkmind.steady.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.monkmind.steady.core.designsystem.theme.SteadyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayRoute() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Steady") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        TodayContent(padding)
    }
}

@Composable
private fun TodayContent(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Build your rhythm.",
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Your daily habits will appear here.",
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodayScreenPreview() {
    SteadyTheme(dynamicColor = false) {
        TodayRoute()
    }
}

