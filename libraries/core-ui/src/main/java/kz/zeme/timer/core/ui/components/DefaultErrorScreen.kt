package kz.zeme.timer.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kz.zeme.timer.core.res.R
import kz.zeme.timer.core.res.theme.TimerTheme
import kz.zeme.timer.core.res.theme.bold
import kz.zeme.timer.core.res.theme.medium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultErrorScreen(
    modifier: Modifier = Modifier,
    title: String? = null,
    errorMessage: String = stringResource(R.string.shared_something_went_wrong),
    buttonText: String = stringResource(R.string.shared_retry),
    onRefresh: (() -> Unit)? = null,
    backgroundColor: Color = TimerTheme.colors.mainBg,
    content: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (title != null) {
            Text(
                text = title,
                style = TimerTheme.typography.titleMedium.bold(),
                color = TimerTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = errorMessage,
            style = TimerTheme.typography.bodyLarge,
            color = TimerTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )

        if (content != null) {
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }

        if (onRefresh != null) {
            Spacer(modifier = Modifier.height(24.dp))
            CompositionLocalProvider(LocalRippleConfiguration provides null) {
                Button(
                    onClick = onRefresh,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TimerTheme.colors.buttonPrimary,
                        contentColor = TimerTheme.colors.textPrimary,
                    ),
                ) {
                    Text(
                        text = buttonText,
                        style = TimerTheme.typography.labelLarge.medium(),
                    )
                }
            }
        }
    }
}
