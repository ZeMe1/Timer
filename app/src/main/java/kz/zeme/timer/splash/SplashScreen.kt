package kz.zeme.timer.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kz.zeme.timer.R
import kz.zeme.timer.core.res.R as CoreResR
import kz.zeme.timer.core.res.theme.TimerTheme
import kz.zeme.timer.core.res.theme.primary

private const val SPLASH_DURATION_MS = 1500L

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TimerTheme.colors.mainBg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                modifier = Modifier.size(96.dp),
                painter = painterResource(R.drawable.ic_splash_logo),
                contentDescription = null,
                colorFilter = ColorFilter.tint(TimerTheme.colors.textPrimary),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(CoreResR.string.app_name),
                style = TimerTheme.typography.titleLarge.primary(),
            )
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF010101,
)
@Composable
private fun SplashScreenPreview() {
    SplashScreen(
        onSplashFinished = {},
    )
}
