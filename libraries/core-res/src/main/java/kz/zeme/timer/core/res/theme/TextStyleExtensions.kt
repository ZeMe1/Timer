package kz.zeme.timer.core.res.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

fun TextStyle.align(textAlign: TextAlign): TextStyle = copy(textAlign = textAlign)

@Composable
fun TextStyle.primary(): TextStyle = copy(color = TimerTheme.colors.textPrimary)

@Composable
fun TextStyle.secondary(): TextStyle = copy(color = TimerTheme.colors.textSecondary)

@Composable
fun TextStyle.error(): TextStyle = copy(color = TimerTheme.colors.basicError)

@Composable
fun TextStyle.enabled(enabled: Boolean): TextStyle = copy(color = color.enabled(enabled))

fun TextStyle.bold(): TextStyle = copy(fontWeight = FontWeight.Bold)

fun TextStyle.medium(): TextStyle = copy(fontWeight = FontWeight.Medium)
